#!/bin/bash
kubectl scale rc write-verticle-dns-controller --replicas=$1 --namespace=$2
