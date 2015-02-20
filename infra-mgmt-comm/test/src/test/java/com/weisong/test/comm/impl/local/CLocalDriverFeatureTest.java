package com.weisong.test.comm.impl.local;

import com.weisong.test.comm.CBaseDriverFeatureTest;
import com.weisong.test.comm.impl.CBaseAgent;
import com.weisong.test.comm.impl.CBaseEndpoint;
import com.weisong.test.comm.impl.CBaseProxy;
import com.weisong.test.comm.impl.CDefaultDriver;
import com.weisong.test.comm.impl.CDefaultRequestHandler;

public class CLocalDriverFeatureTest extends CBaseDriverFeatureTest {

	@Override
	protected void createObjects() throws Exception {
		System.out.println("==");
		proxyHub = new CLocalHub("proxy-hub");
		proxies = new CBaseProxy[] {
			new CLocalProxy(proxyHub)
		  ,	new CLocalProxy(proxyHub)
		};
		drivers = new CDefaultDriver[] {
			new CDefaultDriver(proxyHub)
		  ,	new CDefaultDriver(proxyHub)
		};
		CLocalHub agentHubs[] = new CLocalHub[] {
			new CLocalHub("agent-hub-1")
		  ,	new CLocalHub("agent-hub-2")
		};
		agents = new CBaseAgent[] {
			new CLocalAgent((CLocalProxy) proxies[0], agentHubs[0], new CDefaultRequestHandler())
		  ,	new CLocalAgent((CLocalProxy) proxies[1], agentHubs[1], new CDefaultRequestHandler())
		};
		endpoints = new CBaseEndpoint[] {
			new CLocalEndpoint(agentHubs[0], new CDefaultRequestHandler())
		  ,	new CLocalEndpoint(agentHubs[0], new CDefaultRequestHandler())
		  ,	new CLocalEndpoint(agentHubs[1], new CDefaultRequestHandler())
		  ,	new CLocalEndpoint(agentHubs[1], new CDefaultRequestHandler())
		};
		
		epAddrs = new String[endpoints.length];
		for(int i = 0; i < endpoints.length; i++) {
			epAddrs[i] = endpoints[i].getAddress();
		}
	}
}
