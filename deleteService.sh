kubectl delete service/frontend-verticle-dns --namespace=$1
kubectl delete service/read-verticle-dns --namespace=$1
kubectl delete service/write-verticle-dns --namespace=$1
