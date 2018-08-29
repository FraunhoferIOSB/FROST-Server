#!/bin/bash
set -e

./helm/build.sh

# release version
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_TAG}" != "" ]; then
  cd helm-charts
  git add .
  git remote rm origin
  git remote add origin https://phertweck:$GITHUB_API_KEY@github.com/FraunhoferIOSB/helm-charts
else
  cd helm-charts-snapshot
  git add .
  git remote rm origin
  git remote add origin https://phertweck:$GITHUB_API_KEY@github.com/FraunhoferIOSB/helm-charts-snapshot
fi

git commit -m "Travis build ${TRAVIS_BUILD_NUMBER} pushed"
git push origin master -fq
cd ../
rm -rf ./helm-charts
echo "Helm chart build and pushed"
