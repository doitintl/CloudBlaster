#!/bin/sh

set -e
set -u

# This script is meant to run when run.sh, lister.sh and deleter.sh are in the root dir.
# It was built designed for use in a Docker containmer, but can run elsewhere.

SCRIPT=$(basename $0)
pwd
mkdir /asset-list || true
USAGE="Usage
      ./${SCRIPT} Lister <ARGS>
        or
      ./${SCRIPT} Deleter <ARGS>
        where <ARGS> are the command-line args for the relevant program.

      To learn the command-line args, use ./run.sh Lister -h or ./run.sh Deleter -h"

if [ $# -eq 0 ]; then
  echo "No arguments supplied:\n $USAGE"
  exit 1
fi

FIRST=$1

shift 1

case $FIRST in

Lister)
  ./lister.sh "$@"
  ;;

Deleter)
  ./deleter.sh "$@"
  ;;
*)
  echo "Invalid first command-line arg $FIRST:\n$USAGE"
  exit 1
  ;;
esac
