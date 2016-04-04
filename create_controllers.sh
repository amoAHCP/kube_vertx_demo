#!/bin/bash
kubectl create -f kube/frontend-controller.yaml
sleep 30
kubectl create -f kube/read-controller.yaml
kubectl create -f kube/write-controller.yaml
