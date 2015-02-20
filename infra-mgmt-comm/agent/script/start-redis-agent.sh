#!/bin/sh

mvn exec:java -Dexec.mainClass="com.weisong.test.comm.impl.websocket.CRedisWebSocketAgent" -Dexec.args="$*"

