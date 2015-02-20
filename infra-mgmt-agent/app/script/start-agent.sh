#!/bin/sh

mvn exec:java -Dexec.mainClass="com.weisong.test.comm.impl.CHazelcastWebSocketAgent" -Dexec.args="$*"

