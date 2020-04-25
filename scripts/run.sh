#!/usr/bin/env bash

mvn clean install

docker-compose up --build
