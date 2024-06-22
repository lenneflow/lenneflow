cd..

cd..

cd account-service
docker build -t lenneflow/account-service .
cd..

cd admin-service
docker build -t lenneflow/admin-service .
cd..

cd orchestration-service
docker build -t lenneflow/orchestration-service .
cd..

cd task-service
docker build -t lenneflow/task-service .
cd..

cd worker-service
docker build -t lenneflow/worker-service .
cd..

cd workflow-service
docker build -t lenneflow/workflow-service .
cd..

cd gateway-service
docker build -t lenneflow/gateway-service .
cd..

docker images