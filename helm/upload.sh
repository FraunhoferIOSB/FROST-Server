#!/bin/bash
set -e

./build.sh

cd helm-charts
git add .
git remote rm origin
git remote add origin https://phertweck:$GITHUB_API_KEY@github.com/FraunhoferIOSB/helm-charts
git commit -m "Travis build $ TRAVIS_BUILD_NUMBER pushed"
# git push origin master -fq
cd ../
rm -r ./helm-charts
echo "Helm chart build and pushed"
