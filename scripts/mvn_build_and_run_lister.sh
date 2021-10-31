#!/usr/bin/env bash

if [[ $(pwd) != */scripts ]]; then
  echo "Run in /scripts dir"
  exit 1
fi

pushd ..
mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="com.doitintl.blaster.lister.ListerKt" -Dexec.args="$1 $2 $3 $4 $5 $6"
popd || exit
