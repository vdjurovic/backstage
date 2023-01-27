
SHELL := /bin/bash # Use bash syntax

init-dirs:
	mkdir -p target/docker/jdk

install-jdk: init-dirs
	$(shell ./docker/install-java.sh)

build-docker-image: install-jdk


