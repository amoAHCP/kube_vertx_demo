#!/bin/bash
kubectl create -f kube_vxms/frontend-controller.yaml --namespace=$1
kubectl create -f kube_vxms/read-controller.yaml --namespace=$1
kubectl create -f kube_vxms/write-controller.yaml --namespace=$1
