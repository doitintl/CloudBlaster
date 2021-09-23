#!/usr/bin/env bash

mvn exec:java -Dexec.mainClass="com.doitintl.blaster.test.TestRunnerKt" -Dexec.args="$1"
