package com.weisong.test.comm.impl.local;

import lombok.Getter;

import com.weisong.test.comm.CHub;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.impl.CBaseProxy;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CPduProcessor;

public class CLocalProxy extends CBaseProxy {

	static public class LocalAgentRemote implements AgentRemote {
		@Getter protected String epAddr;
		@Getter protected String agentAddr;
		protected CPduProcessor agent;
		protected LocalAgentRemote(String epAddr, String agentAddr, CPduProcessor agent) {
			this.epAddr = epAddr;
			this.agentAddr = agentAddr;
			this.agent = agent;
		}
		@Override
		public void publish(CPdu pdu) throws CException {
			agent.publish(pdu);
		}
	}
	
	protected void registerAndPublish(CPdu pdu, CPduProcessor agent) {
		LocalAgentRemote agentRemote = new LocalAgentRemote(pdu.srcAddr, pdu.lastSrcAddr, agent);
		register(agentRemote.epAddr, agentRemote);
		publish(pdu);
	}
	
	public CLocalProxy(CHub hub) {
		super(hub);
	}
}
