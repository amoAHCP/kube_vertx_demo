#!/bin/bash
kubectl create -f kube/mongo-service.yaml --namespace=$1
kubectl create -f kube/mongo-controller-gcloud-store.yaml --namespace=$1
