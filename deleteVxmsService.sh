kubectl delete service/gateway-vxms   --namespace=$1
kubectl delete service/read-vxms   --namespace=$1
kubectl delete service/write-vxms   --namespace=$1
