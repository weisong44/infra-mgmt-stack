package com.weisong.test.comm.impl;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CBaseRoute;
import com.weisong.test.comm.transport.routing.CRouteSelector;

public class CHazelcastWebSocketAgent extends CBaseAgent {

	public class CToProxyWebSocketRoute extends CBaseRoute {

		private CWebSocketClient wsClient;
		
		public CToProxyWebSocketRoute(String[] urls, CRouteSelector ... array) {
			super(array);
			wsClient = new CWebSocketClient(urls, CHazelcastWebSocketAgent.this);
		}

		@Override
		public void forward(CPdu pdu) throws CException {
			wsClient.publish(pdu);
		}

	}
	
	public CHazelcastWebSocketAgent(String[] urls, CMessageHandler ... handlers) 
			throws Exception {
		super(new CHazelcastHub(CComponentType.agent), handlers);
		addRoute(new CToProxyWebSocketRoute(urls)
			.addRouteSelector(CRouteSelector.driverSelector)
			.addRouteSelector(CRouteSelector.proxySelector));
	}

	static public void main(String[] args) throws Exception {
		if(args.length == 1) {
			if("-h".equals(args[0]) || "-help".equals(args[0])) {
				printUsageAndExit(CHazelcastWebSocketAgent.class);
			}
		}

		if(args.length > 1) {
			printUsageAndExit(CHazelcastWebSocketAgent.class);
		}
		
		String[] urls = getUrls(args);
		CMessageHandler handler = new CDefaultRequestHandler();
		CHazelcastWebSocketAgent agent = new CHazelcastWebSocketAgent(urls, handler);
		synchronized (agent) {
			agent.wait();
		}
	}

}
