#!/bin/bash
kubectl scale rc read-verticle-dns-controller --replicas=$1 --namespace=$2
