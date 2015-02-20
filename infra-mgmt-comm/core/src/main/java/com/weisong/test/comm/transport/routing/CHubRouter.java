package com.weisong.test.comm.transport.routing;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.transport.pdu.CPdu;

abstract public class CHubRouter extends CBaseRouter implements CRouter {
	
	protected CHub hub;

	private CHub.Listener listener = new CHub.Listener() {
		@Override
		public void onHubPdu(CPdu pdu) {
			publish(pdu);
		}
	};
	
	public CHubRouter(CComponentType type, CHub hub, CMessageHandler ... messageHandlers) {
		super(type, messageHandlers);
		this.hub = hub;
		register(address);
		setupRouting();
	}

	protected void register(String topic) {
		hub.register(topic, listener);
	}
}
