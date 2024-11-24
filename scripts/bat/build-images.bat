cd..

cd..

cd admin-service
docker build -t lenneflow/admin-service .
cd..

cd orchestration-service
docker build -t lenneflow/orchestration-service .
cd..

cd function-service
docker build -t lenneflow/function-service .
cd..

cd worker-service
docker build -t lenneflow/worker-service .
cd..

cd workflow-service
docker build -t lenneflow/workflow-service .
cd..

cd callback-service
docker build -t lenneflow/callback-service .
cd..

cd account-service
docker build -t lenneflow/account-service .
cd..

docker images