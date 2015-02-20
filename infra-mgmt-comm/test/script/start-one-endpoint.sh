#!/bin/sh

mvn exec:java \
	-Dexec.mainClass="com.weisong.test.comm.impl.websocket.CStartOneHazelcastWebSocketEndpoint" \
	-Dexec.args="$*"

