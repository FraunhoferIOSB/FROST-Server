#!/bin/bash
set -e

if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_TAG}" != "" ]; then
  mvn deploy -DskipTests --settings travis-settings.xml
fi
