#!/usr/bin/env bash

set -e

if [[ $(pwd) != */scripts ]]; then
  echo "Run in /scripts dir"
  exit 1
fi

pushd ..

export MAVEN_OPTS=-ea
mvn install exec:java -Dexec.mainClass="com.doitintl.blaster.test.TestRunnerKt" -Dexec.cleanupDaemonThreads=false -Dexec.args="$1"

popd || exit
