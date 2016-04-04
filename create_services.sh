#!/bin/bash
kubectl create -f kube/frontend-service.yaml
kubectl create -f kube/read-service.yaml
kubectl create -f kube/write-service.yaml
