#!/bin/bash
PROJECT_NAME=kubernetestests
IMAGE_VERSION=v01
DEPLOY=false


for i in "$@"
do
case $i in
    -v=*|--version=*)
    IMAGE_VERSION="${i#*=}"

    ;;
    -p=*|--projectName=*)
    PROJECT_NAME="${i#*=}"
    ;;
    -d=*|--deploy=*)
    DEPLOY="${i#*=}"
    ;;
    *)
    ;;
esac
done

docker build -t gcr.io/$PROJECT_NAME/read-verticle-dns:$IMAGE_VERSION read-verticle/

if $DEPLOY; then
    gcloud docker push gcr.io/$PROJECT_NAME/read-verticle-dns:$IMAGE_VERSION
fi
