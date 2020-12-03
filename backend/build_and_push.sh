#!/bin/sh
#
# ./build_and_push.sh

GIT_SHA=$(git rev-parse --short HEAD)
ACR_TAG="simplereportacr.azurecr.io/api/simple-report-api-build:$GIT_SHA"

echo "Building backend images"
docker-compose build

docker tag "simple-report-api-build:latest" $ACR_TAG
echo "Tagged $ACR_TAG"

# Log in to ACR
az acr login --name simplereportacr
docker push $ACR_TAG
