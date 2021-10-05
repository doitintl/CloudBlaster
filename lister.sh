#!/usr/bin/env bash

mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="com.doitintl.blaster.lister.ListerKt" -Dexec.args="$1 $2 $3 $4 $5 $6"
