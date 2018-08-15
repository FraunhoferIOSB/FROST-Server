git config --global user.email "noReply@local"
git config --global user.name "Travis Build"

git clone --quiet --branch master https://github.com/FraunhoferIOSB/helm-charts.git

helm lint ./frost-server
helm package ./frost-server -d ./helm-charts
helm repo index --url https://fraunhoferiosb.github.io/helm-charts/ ./helm-charts

echo "Helm chart build"
