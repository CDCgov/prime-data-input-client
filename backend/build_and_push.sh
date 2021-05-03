#!/bin/sh
#
# ./build_and_push.sh

GIT_SHA=$(git rev-parse --short HEAD)
ACR_TAG="simplereportacr.azurecr.io/api/simple-report-api-build:$GIT_SHA"

export DOCKER_CLI_EXPERIMENTAL=enabled # to get "manifest inspect"

if docker manifest inspect $ACR_TAG > /dev/null 2>&1; then
    echo "Built image for ${GIT_SHA} already exists in the repository"
    exit 0
fi

echo "Building backend images"
docker-compose -f docker-compose.prod.yml build

docker tag "simple-report-api-build:latest" $ACR_TAG
echo "Tagged $ACR_TAG"

# Log in to ACR
# Uncomment for user login
# az acr login --name simplereportacr
docker push $ACR_TAG
