#!/bin/bash
kubectl create -f kube/mongo-service.yaml
kubectl create -f kube/mongo-controller.yaml
