#!/usr/bin/env bash
kubectl delete rc/frontend-verticle-dns-controller --namespace=$1
kubectl delete rc/read-verticle-dns-controller --namespace=$1
kubectl delete rc/write-verticle-dns-controller --namespace=$1
