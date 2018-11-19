#!/bin/bash
echo "Environment variable TRAVIS_BRANCH is"
echo $TRAVIS_BRANCH
version=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)
echo "Recognized project verion:"
echo $version
if [ "${TRAVIS_BRANCH}" == "master" ]; then
    export TAG=$version
    echo "Recognized master branch. Tag is:"
    echo $TAG
else
    export TAG="${TRAVIS_BRANCH}-${version}"
    echo "Recognized other branch than master. Tag is:"
    echo $TAG
fi