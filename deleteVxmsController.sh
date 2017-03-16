#!/usr/bin/env bash
kubectl delete rc/gateway-vxms-controller  --namespace=$1
kubectl delete rc/read-vxms-controller  --namespace=$1
kubectl delete rc/write-vxms-controller  --namespace=$1
