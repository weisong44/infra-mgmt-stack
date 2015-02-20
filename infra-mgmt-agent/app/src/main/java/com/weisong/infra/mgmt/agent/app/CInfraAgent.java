package com.weisong.infra.mgmt.agent.app;

import java.util.HashMap;
import java.util.Map;

import com.weisong.infra.mgmt.agent.data.CInitEbean;
import com.weisong.infra.mgmt.agent.handler.CAgentPackageHandler;
import com.weisong.infra.mgmt.agent.message.CAgentMsgs;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.impl.CDefaultRequestHandler;
import com.weisong.test.comm.impl.CHazelcastWebSocketAgent;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.builtin.CCommonMsgs.Status.StatusValue;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.util.AddrUtil;

public class CInfraAgent extends CHazelcastWebSocketAgent {
	
	static {
		CInitEbean.init();
	}

	private Map<Class<?>, CMessageHandler> handlerMap = new HashMap<>();
	
	public CInfraAgent(String[] urls, CMessageHandler handler) throws Exception {
		super(urls, handler);
		addHandler(new CAgentPackageHandler(), new Class<?>[] {
				CAgentMsgs.AddPackage.Request.class
			  , CAgentMsgs.ListPackages.Request.class
			  , CAgentMsgs.RemovePackage.Request.class
		});
		
	}
	
	private void addHandler(CMessageHandler handler, Class<?>[] requestClasses) {
		for(Class<?> reqClass : requestClasses) {
			handlerMap.put(reqClass, handler);
		}
	}
	
	@Override
	protected CPdu createStatusPdu() {
		CAgentMsgs.AgentStatus okStatus = 
				new CAgentMsgs.AgentStatus(address, AddrUtil.getAnyDriver(), StatusValue.Ok);
		return new CPdu(okStatus);
	}

	@Override
	protected CPdu handleOwnPdu(PduContext ctx) throws CException {
		CMessage message = ctx.getMessage();
		CMessageHandler handler = handlerMap.get(message.getClass());
		if(handler != null) {
			if(message instanceof CRequest) {
				return new CPdu(handler.onRequest((CRequest) message));
			}
			else {
				handler.onNotification((CNotification) message);
				return null;
			}
		}
		else {
			return super.handleOwnPdu(ctx);
		}
	}
	
	static public void main(String[] args) throws Exception {
		if(args.length == 1) {
			if("-h".equals(args[0]) || "-help".equals(args[0])) {
				printUsageAndExit(CInfraAgent.class);
			}
		}

		if(args.length > 1) {
			printUsageAndExit(CInfraAgent.class);
		}
		
		String[] urls = getUrls(args);
		CMessageHandler handler = new CDefaultRequestHandler();
		CInfraAgent agent = new CInfraAgent(urls, handler);
		synchronized (agent) {
			agent.wait();
		}
	}

}
