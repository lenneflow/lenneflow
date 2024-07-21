cd ..

cd ..

kubectl create namespace lenneflow

kubectl apply -f k8s/ -n lenneflow

kubectl get pods -n lenneflow