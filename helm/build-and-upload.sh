git config --global user.email "noReply@local"
git config --global user.name "Travis Build"

git clone --quiet --branch master https://github.com/FraunhoferIOSB/helm-charts.git

helm lint ./frost-server
helm package ./frost-server -d ./helm-charts
helm repo index --url https://fraunhoferiosb.github.io/helm-charts/ ./helm-charts

cd helm-charts
git add .
git remote rm origin
git remote add origin https://phertweck:$GITHUB_API_KEY@github.com/FraunhoferIOSB/helm-charts
git commit -m "Travis build $ TRAVIS_BUILD_NUMBER pushed"
# git push origin master -fq
cd ../
rm -r ./helm-charts
echo "Helm chart build and pushed"
