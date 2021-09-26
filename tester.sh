#!/usr/bin/env bash
export MAVEN_OPTS=-ea
mvn install exec:java -Dexec.mainClass="com.doitintl.blaster.test.TestRunnerKt" -Dexec.args="$1"
