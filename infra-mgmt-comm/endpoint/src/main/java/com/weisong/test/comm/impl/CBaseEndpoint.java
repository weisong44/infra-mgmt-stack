package com.weisong.test.comm.impl;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CEndpoint;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CCommonMsgs.Status.StatusValue;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CHubRouter;
import com.weisong.test.comm.transport.routing.CRoute;
import com.weisong.test.comm.transport.routing.CRouteSelector;
import com.weisong.test.comm.transport.routing.CToHubRoute;
import com.weisong.test.comm.util.AddrUtil;

abstract public class CBaseEndpoint extends CHubRouter implements CEndpoint {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Getter @Setter 
	protected int reportingInterval = 1000; // ms
	
	protected ReportingWorker reportingWorker;
	
	public CBaseEndpoint(CHub hub, CMessageHandler ... messageHandlers)
			throws CException {
		super(CComponentType.endpoint, hub, messageHandlers);
		this.hub = hub;
		reportingWorker = new ReportingWorker();
		reportingWorker.start();
	}


	@Override
	protected void setupRouting() {
		CRoute route = new CToHubRoute(hub) {
			@Override
			public void forward(CPdu pdu) throws CException {
				hub.publish(AddrUtil.getAllAgents(), pdu);
			}
		};
		addRoute(route
			.addRouteSelector(CRouteSelector.agentSelector)
			.addRouteSelector(CRouteSelector.proxySelector)
			.addRouteSelector(CRouteSelector.driverSelector));
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
			setName(CBaseEndpoint.this.getId() + ".ReportingWorker");
			while(shutdown == false) {
				try {
					Thread.sleep(reportingInterval);
					CCommonMsgs.Status okStatus = 
						new CCommonMsgs.Status(address, AddrUtil.getAnyDriver(), StatusValue.Ok);
					hub.publish(new CPdu(okStatus));
				} catch (Throwable t) {
					// Do nothing
				}
			}
		}
	}
}
