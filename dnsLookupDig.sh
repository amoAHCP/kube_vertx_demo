#!/bin/bash
kubectl exec busybox -- dig srv $1
