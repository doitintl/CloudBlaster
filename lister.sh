#!/usr/bin/env bash

mvn  exec:java -Dexec.mainClass="com.doitintl.blaster.Lister" -Dexec.args="$1 $2 $3"
