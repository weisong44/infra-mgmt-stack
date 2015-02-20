package com.weisong.test.comm.impl;

import com.weisong.test.comm.CProxy;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CBaseRoute;
import com.weisong.test.comm.transport.routing.CRouteSelector;

public class CToAgentRoute extends CBaseRoute {
	
	private CProxy proxy;

	public CToAgentRoute(CProxy proxy, CRouteSelector ... array) {
		super(array);
		this.proxy = proxy;
	}
	
	@Override
	public void forward(CPdu pdu) throws CException {
		proxy.getAgentRemote(pdu.destAddr).publish(pdu);
	}
}
