#!/usr/bin/env bash

set -e
set -u

# This script builds the Docker container.

if [[ $(pwd) != */scripts ]]; then
  echo "Run in /scripts dir"
  exit 1
fi
pushd ..

docker build -t cloudblaster:v1 .
popd || exit
