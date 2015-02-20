package com.weisong.test.comm.impl.local;

import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.impl.CBaseAgent;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CBaseRoute;
import com.weisong.test.comm.transport.routing.CRoute;
import com.weisong.test.comm.transport.routing.CRouteSelector;

public class CLocalAgent extends CBaseAgent {
	
	protected class LocalProxyRemote implements ProxyRemote {
		private CLocalProxy proxy;
		public LocalProxyRemote(CLocalProxy proxy) {
			this.proxy = proxy;
		}
		public void publish(CPdu pdu) throws CException {
			proxy.registerAndPublish(pdu, CLocalAgent.this);
		}
	}
	
	public CLocalAgent(CLocalProxy proxy, CHub hub, CMessageHandler ... handlers)
			throws CException {
		super(hub, handlers);
		final ProxyRemote proxyRemote = new LocalProxyRemote(proxy);
		CRoute toProxyRoute = new CBaseRoute() {
			@Override
			public void forward(CPdu pdu) throws CException {
				proxyRemote.publish(pdu);
			}
		};
		addRoute(toProxyRoute
			.addRouteSelector(CRouteSelector.driverSelector)
			.addRouteSelector(CRouteSelector.proxySelector));
	}
}
