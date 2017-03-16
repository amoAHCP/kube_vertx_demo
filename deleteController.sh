#!/usr/bin/env bash
kubectl delete rc/gateway-verticle-controller  --namespace=$1
kubectl delete rc/read-verticle-controller  --namespace=$1
kubectl delete rc/write-verticle-controller  --namespace=$1
