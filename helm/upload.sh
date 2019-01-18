#!/bin/bash
set -e

./helm/build.sh

# release version
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_TAG}" != "" ]; then
  cd helm-charts
  git add .
  git remote rm origin
  git remote add origin https://phertweck:$GITHUB_API_KEY@github.com/FraunhoferIOSB/helm-charts
  git commit -m "Travis build ${TRAVIS_BUILD_NUMBER} pushed"
  git push origin master -fq
  cd ../
  rm -rf ./helm-charts
fi

# Only deploy master branch and tagged builds to snapshot repository
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && ([ "${TRAVIS_BRANCH}" == "master" ] || [ "${TRAVIS_TAG}" != "" ]); then
    cd helm-charts-snapshot
    git add .
    git remote rm origin
    git remote add origin https://phertweck:$GITHUB_API_KEY@github.com/FraunhoferIOSB/helm-charts-snapshot
    git commit -m "Travis build ${TRAVIS_BUILD_NUMBER} pushed"
    git push origin master -fq
    cd ../
    rm -rf ./helm-charts
fi

echo "Helm chart build and pushed"
