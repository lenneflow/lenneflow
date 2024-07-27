cd ..

cd ..

sudo kubectl create namespace lenneflow

sudo kubectl apply -f k8s/ -n lenneflow

sudo kubectl get pods -n lenneflow