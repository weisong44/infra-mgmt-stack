package com.weisong.test.comm.impl;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.weisong.test.comm.CAgent;
import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.CProxy;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.builtin.CAgentMsgs;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CCommonMsgs.Status.StatusValue;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CHubRouter;
import com.weisong.test.comm.transport.routing.CRouteSelector;
import com.weisong.test.comm.transport.routing.CToHubRoute;
import com.weisong.test.comm.util.AddrUtil;
import com.weisong.test.comm.util.EvictableList;
import com.weisong.test.comm.util.HostAndPortResolver;
import com.weisong.test.comm.util.HostAndPortResolver.HostAndPort;

abstract public class CBaseAgent extends CHubRouter implements CAgent {

	static protected interface ProxyRemote {
		public void publish(CPdu pdu) throws CException;
	}
	
	@Getter @Setter 
	protected int reportingInterval = 1000; // ms
	
	protected ReportingWorker reportingWorker;
	
	protected EvictableList<String> epList = new EvictableList<>();
	
	public CBaseAgent(CHub hub, CMessageHandler ... messageHandlers) 
			throws CException {
		super(CComponentType.agent, hub, messageHandlers);
		register(AddrUtil.getAnyDriver());
		register(AddrUtil.getAllAgents());
		(reportingWorker = new ReportingWorker()).start();
	}

	@Override
	protected void setupRouting() {
		addRoute(new CToHubRoute(hub)
			.addRouteSelector(CRouteSelector.endpointSelector));
	}

	@Override
	protected CPdu handleOwnPdu(PduContext ctx) throws CException {
		CMessage message = ctx.getMessage();
		if(message instanceof CAgentMsgs.ListEndpoints.Request) {
			List<CAgentMsgs.ListEndpoints.EndpointProfile> list = new LinkedList<>();
			for(String epAddr : epList.getAll()) {
				CAgentMsgs.ListEndpoints.EndpointProfile profile = 
						new CAgentMsgs.ListEndpoints.EndpointProfile(epAddr);
				list.add(profile);
			}
			return new CPdu(new CAgentMsgs.ListEndpoints.Response(
					(CAgentMsgs.ListEndpoints.Request) message, list));
		}
		return super.handleOwnPdu(ctx);
	}

	@Override
	protected CPdu handleOtherPdu(PduContext ctx) {
		if(AddrUtil.isEndpoint(ctx.getPdu().srcAddr)) {
			boolean isNew = epList.add(ctx.getPdu().srcAddr);
			if(isNew) {
				logger.info(String.format("%s manages [%s]", getId(), ctx.getPdu().srcAddr));
			}
		}
		return super.handleOtherPdu(ctx);
	}
	
	protected CPdu createStatusPdu() {
		CCommonMsgs.Status okStatus = 
				new CCommonMsgs.Status(address, AddrUtil.getAnyDriver(), StatusValue.Ok);
		return new CPdu(okStatus);
	}

	static public void printUsageAndExit(Class<?> clazz) {
		System.out.println("Usage:");
		System.out.println(String.format("    java %s [host:port[,host:port]...]",
				clazz.getSimpleName()));
		System.out.println();
		System.exit(0);
	}

	static public String[] getUrls(String[] args) throws Exception {
		String hostAndPortString = args.length == 0 ?
			"localhost:" + CProxy.DEFAULT_PORT : args[0];

		HostAndPort[] hapArray = HostAndPortResolver.resolveMultiple(
				hostAndPortString, "localhost", CProxy.DEFAULT_PORT);
		String[] urls = new String[hapArray.length];
		System.out.println("Using proxies at");
		for(int i = 0; i < hapArray.length; i++) {
			urls[i] = String.format("ws://%s%s", hapArray[i].toString(), CProxy.BASE_PATH);
			System.out.println("    " + urls[i]);
		}
		return urls;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		reportingWorker.shutdown = true;
		try {
			reportingWorker.interrupt();
			reportingWorker.join();
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	private class ReportingWorker extends Thread {
		private boolean shutdown;
		public void run() {
			setName(CBaseAgent.this.getId() + ".ReportingWorker");
			while(shutdown == false) {
				try {
					Thread.sleep(reportingInterval);
					hub.publish(createStatusPdu());
				} catch (Throwable t) {
					// Do nothing
				}
			}
		}
	}
}
