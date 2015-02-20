package com.weisong.test.comm.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CProxy;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CCommonMsgs.Status.StatusValue;
import com.weisong.test.comm.message.builtin.CDriverMsgs;
import com.weisong.test.comm.message.builtin.CProxyMsgs;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CHubRouter;
import com.weisong.test.comm.transport.routing.CRouteSelector;
import com.weisong.test.comm.transport.routing.CToHubRoute;
import com.weisong.test.comm.util.AddrUtil;
import com.weisong.test.comm.util.EvictableList;
import com.weisong.test.comm.util.EvictableMap;

abstract public class CBaseProxy extends CHubRouter implements CProxy {

	static abstract public class BaseAgentRemote implements AgentRemote {
		@Getter protected String epAddr;
		protected BaseAgentRemote(String epAddr) {
			this.epAddr = epAddr;
		}
	}

	@Getter @Setter 
	protected int reportingInterval = 1000; // ms
	
	protected ReportingWorker reportingWorker;
	
	/* key: epAddr, value: agent remote stub */
	protected EvictableMap<String, AgentRemote> agentMap = new EvictableMap<>();
	/* key: class name, value: list of driver addresses*/
	protected Map<String, EvictableList<String>> driverMap = new ConcurrentHashMap<>();
	
	public CBaseProxy(final CHub hub) {
		super(CComponentType.proxy, hub);
		register(AddrUtil.getAllProxies());
		(reportingWorker = new ReportingWorker()).start();
	}

	@Override
	protected void setupRouting() {
		addRoute(new CToHubRoute(hub,
		  	CRouteSelector.driverSelector));
		addRoute(new CToAgentRoute(this, 
			CRouteSelector.agentSelector
		  ,	CRouteSelector.endpointSelector));
	}

	@Override
	protected CPdu handleOwnPdu(PduContext ctx) throws CException {
		CMessage message = codec.decodeMessage(ctx.getPdu().payload);
		if(message instanceof CCommonMsgs.Reset) {
			driverMap.clear();
			agentMap.clear();
		}
		else if(message instanceof CDriverMsgs.Profile) {
			CDriverMsgs.Profile dfn = (CDriverMsgs.Profile) message;
			for(String clazz : dfn.getSupportedNotifications()) {
				EvictableList<String> list = driverMap.get(clazz);
				if(list == null) {
					list = new EvictableList<String>();
					driverMap.put(clazz, list);
				}
				if(list.add(dfn.srcAddr)) {
					logger.info(String.format("%s adding %s -> %s [total=%d]", 
							id, clazz, dfn.srcAddr, list.size()));
				}
			}
		}
		else if(message instanceof CProxyMsgs.GetDetails.Request) {
			CProxyMsgs.GetDetails.ProxyDetails details = 
					new CProxyMsgs.GetDetails.ProxyDetails(address);
			CProxyMsgs.GetDetails.Response response = new CProxyMsgs.GetDetails.Response(
					(CProxyMsgs.GetDetails.Request) message, details);
			return new CPdu(response);
		}
		else if(message instanceof CProxyMsgs.ListAgents.Request) {
			Set<String> agentAddrSet = new HashSet<>();
			for(AgentRemote remote : agentMap.values()) {
				agentAddrSet.add(remote.getAgentAddr());
			}
			List<CProxyMsgs.ListAgents.AgentProfile> list = new LinkedList<>();
			for(String addr : agentAddrSet) {
				CProxyMsgs.ListAgents.AgentProfile profile = 
						new CProxyMsgs.ListAgents.AgentProfile(addr);
				list.add(profile);
			}
			CProxyMsgs.ListAgents.Response response = new CProxyMsgs.ListAgents.Response(
					(CProxyMsgs.ListAgents.Request) message, list);
			return new CPdu(response);
		}
		return super.handleOwnPdu(ctx);
	}

	@Override
	protected CPdu handleOtherPdu(PduContext ctx) {
		if(AddrUtil.isDriver(ctx.getPdu().destAddr)) {
			// Rewrite message destination
			if(AddrUtil.isAny(ctx.getPdu().destAddr)) {
				String newAddr = null;
				EvictableList<String> list = driverMap.get(ctx.getPdu().type);
				if(list != null && (newAddr = list.getOne()) != null) {
					ctx.getPdu().destAddr = newAddr;
				}
				else {
					drop(ctx.getPdu(), "no driver found");
					return null;
				}
			}
			else if(AddrUtil.isAll(ctx.getPdu().destAddr)) {
				EvictableList<String> list = driverMap.get(ctx.getPdu().type);
				if(list == null) {
					drop(ctx.getPdu(), "no driver found");
					return null;
				}
				for(Object addr : list.getAll()) {
					CPdu pdu = new CPdu(ctx.getPdu());
					pdu.destAddr = addr.toString();
					publish(pdu);
				}
				return null;
			}
		}
		return super.handleOtherPdu(ctx);
	}

	@Override
	public void register(String epAddr, AgentRemote remote) {
		if(agentMap.containsKey(epAddr) == false) {
			hub.register(epAddr, new CHub.Listener() {
				@Override
				public void onHubPdu(CPdu pdu) {
					publish(pdu);
				}
			});
			agentMap.put(epAddr, remote);
			logger.info(String.format("%s proxying for %s", id, epAddr));
		}
		else {
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("%s refreshing %s", id, epAddr));
			}
			agentMap.refresh(epAddr);
		}
	}
	
	public AgentRemote getAgentRemote(String epAddr) {
		return agentMap.get(epAddr);
	}

	@Override
	public void shutdown() {
		super.shutdown();
		reportingWorker.shutdown = true;
		try {
			reportingWorker.interrupt();
			reportingWorker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class ReportingWorker extends Thread {
		private boolean shutdown;
		public void run() {
			setName(CBaseProxy.this.getId() + ".ReportingWorker");
			while(shutdown == false) {
				try {
					Thread.sleep(reportingInterval);
					CCommonMsgs.Status okStatus = 
						new CCommonMsgs.Status(address, AddrUtil.getAnyDriver(), StatusValue.Ok);
					publish(new CPdu(okStatus));
				} catch (Throwable t) {
					// Do nothing
				}
			}
		}
	}
}
