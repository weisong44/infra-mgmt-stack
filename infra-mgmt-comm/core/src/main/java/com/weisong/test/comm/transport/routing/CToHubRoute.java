package com.weisong.test.comm.transport.routing;

import com.weisong.test.comm.CHub;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.pdu.CPdu;

public class CToHubRoute extends CBaseRoute {

	private CHub hub;
	
	public CToHubRoute(CHub hub) {
		this.hub = hub;
	}
	
	public CToHubRoute(CHub hub, CRouteSelector ... array) {
		super(array);
		this.hub = hub;
	}
	
	@Override
	public void forward(CPdu pdu) throws CException {
		hub.publish(pdu);
	}

}
