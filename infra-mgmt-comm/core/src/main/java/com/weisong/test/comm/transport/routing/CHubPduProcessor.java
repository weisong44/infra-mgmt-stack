package com.weisong.test.comm.transport.routing;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.transport.pdu.CPdu;

abstract public class CHubPduProcessor extends CPduProcessor {

	protected CHub hub;
	
	protected CMessageHandler messageHandler;
	
	private CHub.Listener listener = new CHub.Listener() {
		@Override
		public void onHubPdu(CPdu pdu) {
			publish(pdu);
		}
	};
	
	public CHubPduProcessor(CComponentType type, CHub hub) {
		super(type);
		this.hub = hub;
	}

	public CHubPduProcessor(CComponentType type, CHub hub, CMessageHandler handler) {
		super(type);
		this.hub = hub;
		setMessageHandler(handler);
	}

	public void setMessageHandler(CMessageHandler handler) {
		this.messageHandler = handler;
		
		// Self
		register(address);
	}
	
	protected void register(String topic) {
		hub.register(topic, listener);
	}
}
