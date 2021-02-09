#!/bin/bash
echo "Environment variable SOURCE_BRANCH is"
echo $SOURCE_BRANCH
version=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)
echo "Recognized project version:"
echo $version
if [ "${SOURCE_BRANCH}" == "master" ]; then
    export TAG="${version}"
    echo "Recognized master branch. Tag is:"
    echo $TAG
elif [ ! -z "${SOURCE_TAG}" ]; then
    export TAG="${version}"
    echo "Recognized release tag. Tag is:"
    echo $TAG
else
    export TAG="${SOURCE_BRANCH}-${version}"
    echo "Recognized other branch than master. Tag is:"
    echo $TAG
fi
