#!/usr/bin/env bash

set -x
set -e
set -u

if [[ $(pwd) != */scripts ]]; then
  echo "Run in /scripts dir"
  exit 1
fi
pushd ..

docker build -t cloudblaster:v2 .
popd || exit
