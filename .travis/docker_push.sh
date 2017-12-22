#!/usr/bin/env bash
set -e

docker login -u $DOCKER_HUB_USER -p $DOCKER_HUB_PASSWORD

# ----- build and push docker image -----

#IMAGE_NAME="${DOCKER_IMAGE_NAME}:${TRAVIS_TAG}"
LATEST_IMAGE_NAME="${DOCKER_IMAGE_NAME}:latest"

docker build -t $LATEST_IMAGE_NAME ./sts-example
#docker build -t $IMAGE_NAME ./sts-example
#docker tag $IMAGE_NAME $LATEST_IMAGE_NAME

#docker push $IMAGE_NAME
docker push $LATEST_IMAGE_NAME
