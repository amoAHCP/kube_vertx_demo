#!/bin/bash
kubectl create -f kube_vxms/frontend-service.yaml --namespace=$1
kubectl create -f kube_vxms/read-service.yaml --namespace=$1
kubectl create -f kube_vxms/write-service.yaml --namespace=$1
