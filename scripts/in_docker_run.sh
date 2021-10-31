#!/usr/bin/env bash
# This is meant to be invoked from in_docker_deleter.sh or in_docker.lister.sh,
# and not to be called directly.

set -u
set -e

if [[ $(pwd) != */scripts ]]; then
  echo "Run in /scripts dir"
  exit 1
fi
pushd ..

mkdir asset-list || true

docker run -ti -v=$HOME/.config/gcloud:/root/.config/gcloud \
  --mount type=bind,source="$(pwd)"/asset-list,target=/asset-list \
  --mount type=bind,source="$(pwd)"/config,target=/config \
  cloudblaster:v1 \
  "$@"

popd || exit
