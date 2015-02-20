package com.weisong.test.comm.impl.websocket;

import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.impl.CDefaultRequestHandler;
import com.weisong.test.comm.impl.CHazelcastEndpoint;

public class CStartOneHazelcastWebSocketEndpoint {
		
	static public void main(String[] args) throws Exception {
		CMessageHandler handler = new CDefaultRequestHandler();
		CHazelcastEndpoint endpoint = new CHazelcastEndpoint(handler);
		synchronized (endpoint) {
			endpoint.wait();
		}
	}
	
}
