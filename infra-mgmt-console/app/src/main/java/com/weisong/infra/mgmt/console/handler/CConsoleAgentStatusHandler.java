package com.weisong.infra.mgmt.console.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.weisong.infra.mgmt.agent.message.CAgentMsgs;
import com.weisong.infra.mgmt.console.model.CcmEntity;
import com.weisong.test.comm.impl.CDefaultNotificationHandler;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;

public class CConsoleAgentStatusHandler extends CDefaultNotificationHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void onNotification(CNotification notification) {
		CAgentMsgs.AgentStatus agentStatus = (CAgentMsgs.AgentStatus) notification;
		CcmEntity e = Ebean.find(CcmEntity.class)
			.where()
				.eq("address", agentStatus.srcAddr)
				.findUnique();
		if(e == null) {
			logger.info(String.format("Discovered %s", notification.srcAddr));
			e = new CcmEntity();
		}
		e.setAddress(agentStatus.srcAddr);
		e.setStatus(agentStatus.getStatus());
		e.incNotificationCount();
		Ebean.save(e);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends CMessage>[] getSupportedMessages() {
		return new Class[] {
			CAgentMsgs.AgentStatus.class
		};
	}
}
