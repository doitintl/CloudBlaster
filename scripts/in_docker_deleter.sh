#!/usr/bin/env sh

set -e
# This launches a Docker container and runs Deleter in it.
# Run it from the scripts directory.
#  Pass the optional command-line optiojns to Deleter (-h for help).
./in_docker_run.sh \
  Deleter \
  "$@"
