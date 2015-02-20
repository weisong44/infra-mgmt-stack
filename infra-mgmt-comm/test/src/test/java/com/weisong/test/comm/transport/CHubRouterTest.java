package com.weisong.test.comm.transport;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.impl.CDefaultRequestHandler;
import com.weisong.test.comm.impl.local.CLocalHub;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CHubRouter;
import com.weisong.test.comm.transport.routing.CRouteSelector;
import com.weisong.test.comm.transport.routing.CToHubRoute;

public class CHubRouterTest {

	final static private boolean debug = false;
	
	final static private String sourceAddress = "ccp:agent://localhost/agent-123";
	
	private enum Result {
		sourceRouted, sourceDropped, destRouted, destDropped, unknown
	}
	
	private CHubRouter router;
	private Result result = Result.unknown;
		
	@Before
	public void prepare() {
		if(debug) {
			System.out.println("==");
		}
		
		CLocalHub hub = new CLocalHub();
		hub.register(sourceAddress, new CHub.Listener() {
			@Override
			public void onHubPdu(CPdu pdu) {
				setResult(Result.destRouted);
			};
		});
		
		router = new CHubRouter(CComponentType.proxy, hub) {
			@Override
			protected void setupRouting() {
				addRoute(new CToHubRoute(hub, 
					CRouteSelector.agentSelector
				));
			}
			@Override
			protected void drop(CPdu pdu, String reason) {
				super.drop(pdu, reason);
				setResult(
					pdu.type.contains("Req") ?
						Result.sourceDropped
					  :	Result.destDropped
				);
				
			}
		};
		router.addMessageHandler(new CDefaultRequestHandler());
	}
	
	@After
	public void cleanup() {
		router.shutdown();
	}

	@Test
	public void testSourceDropped() {
		testSend("bad-source", "bad-destination");
		Assert.assertEquals(Result.sourceDropped, result);
	}
	
	@Test
	public void testSourceRoutedAndDestDropped() {
		testSend("bad-source", router.getAddress());
		Assert.assertEquals(Result.destDropped, result);
	}

	@Test
	public void testDestRouted() {
		testSend(sourceAddress, router.getAddress());
		Assert.assertEquals(Result.destRouted, result);
	}
	
	private void testSend(String srcAddr, String destAddr) {
		
		if(debug) {
			System.out.println(String.format("Testing: %s ==> %s", srcAddr, destAddr));
		}
		
		CCommonMsgs.Ping.Request request = new CCommonMsgs.Ping.Request(
				srcAddr, destAddr);
		CPdu pdu = new CPdu(request);
		router.publish(pdu);
		
		try {
			int n = 0;
			while(result == Result.unknown && n < 5000) {
				Thread.sleep(1);
				++n;
			}
			Assert.assertNotSame(Result.unknown, result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void setResult(Result result) {
		this.result = result;
		if(debug) {
			System.out.println(String.format("Setting result to %s", result));
		}
	}
}
