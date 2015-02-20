package com.weisong.test.comm.impl;

import java.net.BindException;

import com.weisong.test.comm.CHub;

public class CRedisWebSocketProxy extends CBaseWebSocketProxy {

	public CRedisWebSocketProxy(CHub hub) throws Exception {
		super(hub);
	}
	
	static private void printUsageAndExit() {
		System.out.println("Usage:");
		System.out.println("    java CRedisWebSocketProxy [redis-host:[port]]");
		System.exit(-1);
	}
	
	static public void main(String[] args) throws Exception {
		if(args.length == 1) {
			if("-h".equals(args[0]) || "-help".equals(args[0])) {
				printUsageAndExit();
			}
		}

		if(args.length > 1) {
			printUsageAndExit();
		}
		
		String hostAndPort = args.length == 0 ?
			"localhost:6379" : args[0];
		
		System.out.println(String.format("Using Redis at %s", hostAndPort));
		
		CHub hub = new CRedisHub(hostAndPort);
		CRedisWebSocketProxy proxy = new CRedisWebSocketProxy(hub);
		
		int port = 8080;
		while(true) {
			try {
				new CWebSocketServer(proxy, port, false);
			} catch (BindException e) {
				System.out.println(String.format("Port %d in use, try %d", port, ++port));
			}
		}
	}
}
