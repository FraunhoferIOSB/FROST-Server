#!/bin/bash
set -e

# Setup SSH for GIT push
mkdir -p ~/.ssh
ssh-keyscan -H github.com >> ~/.ssh/known_hosts

echo "Running for branch: ${GITHUB_REF} / ${GITHUB_BASE_REF}"

# release version
if [[ "${GITHUB_BASE_REF}" == "" ]] && [[ "${GITHUB_REF}" == "refs/tags"* ]]; then
  echo -e "${HELM_SSH_KEY}" > ~/.ssh/id_rsa
  chmod 600 ~/.ssh/id_rsa
  cd helm-charts
  git add .
  git remote rm origin
  git remote add origin git@github.com:kernblick/helm-charts.git
  git commit -m "Github build ${GITHUB_RUN_NUMBER} pushed"
  git push origin master -fq
  cd ../
  rm -rf ./helm-charts
  echo "Helm chart pushed to release repository"
fi

# Only deploy master branch and tagged builds to snapshot repository
if [[ "${GITHUB_BASE_REF}" == "" ]] && ([[ "${GITHUB_REF}" == "${DEFAULT_BRANCH}" ]] || [[ "${GITHUB_REF}" == "refs/tags"* ]] || [[ "${GITHUB_REF}" == "refs/heads/develop-2.x" ]]); then

    echo -e "${HELM_SSH_KEY_SNAPSHOT}" > ~/.ssh/id_rsa
    chmod 600 ~/.ssh/id_rsa

    cd helm-charts-snapshot
    git add .
    git remote rm origin
    git remote add origin git@github.com:kernblick/helm-charts-snapshot.git
    git commit -m "Github build ${GITHUB_RUN_NUMBER} pushed"
    git push origin master -fq
    cd ../
    rm -rf ./helm-charts
    echo "Helm chart pushed to snapshot repository"
fi


