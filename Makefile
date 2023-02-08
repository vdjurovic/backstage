
SHELL := /bin/bash # Use bash syntax
VERSION=0.0.0-SNAPSHOT
DOCKER_IMAGE_NAME=backstage-dev
DOCKER_TAG=latest

maven-build:
	mvn -Pautomatic clean install

build-docker: maven-build
	docker build . -t ${DOCKER_IMAGE_NAME}:${DOCKER_TAG} --build-arg BACKSTAGE_VERSION=${VERSION}


