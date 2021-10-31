#!/usr/bin/env sh

set -u
set -e

# This script is meant to run when run.sh, lister.sh and deleter.sh are in the root dir.
# It was built designed for use in a Docker containmer, but can run elsewhere.

CP=.
CP=$CP:./target/cloudblaster-1.0-SNAPSHOT.jar

for FILE in ./target/dependency/*; do
  CP=$CP:$FILE
done

java -cp $CP com.doitintl.blaster.lister.ListerKt "$@"
