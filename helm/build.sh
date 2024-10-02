#!/bin/bash
set -e

git config --global user.email "noReply@local"
git config --global user.name "Workflow Build"

# release version
if [[ "${GITHUB_BASE_REF}" == "" ]] && [[ "${GITHUB_REF}" == "refs/tags"* ]]; then
  echo "Building release helm chart"
  git clone --quiet --branch master https://github.com/kernblick/helm-charts.git
  /tmp/helm lint ./helm/frost-server
  /tmp/helm package ./helm/frost-server -d ./helm-charts
  /tmp/helm repo index --url https://kernblick.github.io/helm-charts/ ./helm-charts
fi

echo "Building snapshot helm chart"
git clone --quiet --branch master https://github.com/kernblick/helm-charts-snapshot.git
/tmp/helm lint ./helm/frost-server
/tmp/helm package ./helm/frost-server -d ./helm-charts-snapshot
/tmp/helm repo index --url https://kernblick.github.io/helm-charts-snapshot/ ./helm-charts-snapshot

echo "Helm chart build"
