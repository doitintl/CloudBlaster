#!/usr/bin/env sh

set -e
# This launches a Docker container and runs Lister in it.
# Run it from the scripts directory.
# Pass the command-line options for Lister (-h for help).

./in_docker_run.sh \
  Lister \
  "$@"
