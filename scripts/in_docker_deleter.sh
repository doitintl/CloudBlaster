#!/usr/bin/env sh

set -e

# This runs Deleter in Docker container. Pass the optional switches to Deleter (-h for help).
./in_docker_run.sh \
  Deleter \
  "$@"
