#!/bin/bash
kubectl scale rc frontend-verticle-dns-controller --replicas=$1 --namespace=$2
