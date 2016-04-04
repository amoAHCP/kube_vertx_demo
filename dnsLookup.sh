#!/bin/bash
kubectl exec busybox -- nslookup $1
