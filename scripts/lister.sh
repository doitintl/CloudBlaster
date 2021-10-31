#!/usr/bin/env sh

# This script simply runs the Lister (after building the classpath.)
#
# This script is meant to run when run.sh, lister.sh and deleter.sh are in the root dir.
# It was created for use in a Docker container but can be run outside one.
#
# See USAGE below for usage

set -u
set -e

CP=.
CP=$CP:./target/cloudblaster-1.0-SNAPSHOT.jar

for FILE in ./target/dependency/*; do
  CP=$CP:$FILE
done

java -cp $CP com.doitintl.blaster.lister.ListerKt "$@"
