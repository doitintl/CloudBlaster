#!/usr/bin/env bash
set -u
set -e
set -x
if [[ $(pwd) != */scripts ]]; then
  echo "Run in /scripts dir"
  exit 1
fi
pushd ..

mkdir asset-list || true

docker run -ti -v=$HOME/.config/gcloud:/root/.config/gcloud \
  --mount type=bind,source="$(pwd)"/asset-list,target=/asset-list \
  --mount type=bind,source="$(pwd)"/config,target=/config \
  cloudblaster:v2 \
  "$@"

popd || exit
