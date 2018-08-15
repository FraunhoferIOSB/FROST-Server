#!/bin/bash
set -e

echo $TRAVIS_PULL_REQUEST
echo $TRAVIS_TAG
test "${TRAVIS_PULL_REQUEST}" == "false" && test "${TRAVIS_TAG}" != "" && mvn deploy --settings travis-settings.xml
