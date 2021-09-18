#!/usr/bin/env bash

mvn exec:java -Dexec.mainClass="com.doitintl.blaster.deleter.Deleter" -Dexec.args="$1 $2 $3 $4 $5 $6"
