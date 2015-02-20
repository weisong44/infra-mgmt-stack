package com.weisong.test.comm.impl.websocket;

import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.impl.CDefaultRequestHandler;
import com.weisong.test.comm.impl.CHazelcastEndpoint;

public class CStartMoreHazelcastWebSocketEndpoints {
	
	static private void printUsageAndExit() {
		System.out.println("Usage:");
		System.out.println("    java CStartMoreHazelcastWebSocketEndpoints <number>");
		System.out.println();
		System.exit(0);
	}
	
	static public void main(String[] args) throws Exception {
		
		if(args.length != 1) {
			printUsageAndExit();
		}
		
		int n = Integer.valueOf(args[0]);
		CHazelcastEndpoint[] endpoints = new CHazelcastEndpoint[n];
		CMessageHandler handler = new CDefaultRequestHandler();
		for(int i = 0; i < endpoints.length; i++) {
			endpoints[i] = new CHazelcastEndpoint(handler);
		}
		synchronized (endpoints) {
			endpoints.wait();
		}
	}
	
}
