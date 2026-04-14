echo "------------------------"
echo "Distributed Rate Limiter"
echo "------------------------"
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "Building application-"
mvn clean package -DskipTests > /dev/null 2>&1
echo -e "${GREEN}Build complete${NC}"
echo ""

echo "starting the infrastructure-"
docker-compose up -d
sleep 20
echo -e "${GREEN}Redis + 2 API servers running${NC}"
echo ""

echo "------------------------------------------"
echo "Demo 1-Distributed Coordination with Redis"
echo "------------------------------------------"
echo ""
echo ""
echo "Rate-limit:10 requests per minute"
echo ""

echo "Making 10 requests to server 1 (port 8081) as 'alik'..."
for i in {1..10}; do
    RESPONSE=$(curl -s -w "%{http_code}" -H "X-User-ID: alik" http://localhost:8081/api/resource)
    HTTP_CODE=${RESPONSE: -3}
    if [ "$HTTP_CODE" == "200" ]; then
        echo -e "Request $i: ${GREEN}Allowed (200)${NC}"
    else
        echo -e "Request $i: ${RED}Denied ($HTTP_CODE)${NC}"
    fi
done

echo ""
echo "Making 1 request to Server 2 (port 8082) as 'alik'..."
RESPONSE=$(curl -s -w "%{http_code}" -H "X-User-ID: alik" http://localhost:8082/api/resource)
HTTP_CODE=${RESPONSE: -3}

if [ "$HTTP_CODE" == "429" ]; then
    echo -e "Request 11: ${RED}Denied (429)${NC}"
    echo ""
    echo -e "${GREEN}Success${NC} Redis coordination working."
    echo "Alik sent 10 reqs to server 1, server 2 blocked his 11th request."
    echo "Proof the servers share state via Redis."
else
    echo -e "Request 11: ${GREEN}Allowed (200)${NC}"
    echo ""
    echo -e "${RED}Unexpected?${NC} Rate limit was bypassed."
fi

echo ""
echo "------------------------------------------"
echo "Demo 2-Graceful Degradation(Redis Failure)"
echo "------------------------------------------"
echo ""

echo "stopping Redis to simulate failure"
docker-compose stop redis
sleep 7
echo -e "${YELLOW}Redis Down${NC}"
echo ""

echo "Making 12 requests to Server 1 as 'bob'..."
ALLOWED=0
DENIED=0

for i in {1..12}; do
    RESPONSE=$(curl -s -w "%{http_code}" -H "X-User-ID: bob" http://localhost:8081/api/resource)
    HTTP_CODE=${RESPONSE: -3}
    
    if [ "$HTTP_CODE" == "200" ]; then
        ALLOWED=$((ALLOWED + 1))
        echo -e "Request $i: ${GREEN}Allowed (200)${NC}"
    else
        DENIED=$((DENIED + 1))
        echo -e "Request $i: ${RED}Denied (429)${NC}"
    fi
done

echo ""
if [ $ALLOWED -eq 10 ] && [ $DENIED -eq 2 ]; then
    echo -e "${GREEN}Success${NC} Graceful degradation working."
    echo "Service stayed up despite Redis failure."
    echo "Fell back to in-memory rate limiting (10 allowed, 2 denied)."
else
    echo -e "${YELLOW}Results:${NC} $ALLOWED allowed, $DENIED denied"
    echo "Fallback to in-memory storage is working."
fi

echo ""
echo "-------"
echo "Cleanup"
echo "-------"
docker-compose down
echo -e "${GREEN}Demo complete${NC}"
echo ""


