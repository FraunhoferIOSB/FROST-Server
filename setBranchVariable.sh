#!/bin/bash
version=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)
if [ "${TRAVIS_BRANCH}" = "master" ]; then
    export TAG=$version
else
    export TAG="${TRAVIS_BRANCH}-${version}"
fi

