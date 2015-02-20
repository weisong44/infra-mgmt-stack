package com.weisong.test.comm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import com.weisong.test.comm.impl.CBaseAgent;
import com.weisong.test.comm.impl.CBaseEndpoint;
import com.weisong.test.comm.impl.CBaseProxy;
import com.weisong.test.comm.impl.CDefaultDriver;
import com.weisong.test.comm.impl.CDefaultNotificationHandler;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.transport.routing.CPduProcessor;
import com.weisong.test.comm.util.AddrUtil;

abstract public class CBaseDriverTest {

	protected CHub proxyHub;
	protected CBaseProxy[] proxies;
	protected CDefaultDriver[] drivers;
	protected CBaseAgent[] agents;
	protected CBaseEndpoint[] endpoints;
	// Discovered
	protected String[] epAddrs;
	protected String[] proxyAddrs;
	
	abstract protected void createObjects() throws Exception;
	
	@Before
	public void prepare() throws Exception {
		createObjects();
		waitForEndpoints();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void waitForEndpoints() throws Exception {
		// Wait for endpoints to register
		final Set<String> epAddrSet = (Set) Collections.synchronizedSet(new HashSet<>()); 
		final Set<String> proxyAddrSet = (Set) Collections.synchronizedSet(new HashSet<>()); 
		CMessageHandler handler = new CDefaultNotificationHandler() {
			@Override
			public void onNotification(CNotification n) {
				if(AddrUtil.isEndpoint(n.srcAddr)) {
					if(epAddrSet.contains(n.srcAddr) == false) {
						epAddrSet.add(n.srcAddr);
						System.out.println(String.format("Discovered %s", n.srcAddr));
					}
				}
				else if(AddrUtil.isProxy(n.srcAddr)) {
					if(proxyAddrSet.contains(n.srcAddr) == false) {
						proxyAddrSet.add(n.srcAddr);
						System.out.println(String.format("Discovered %s", n.srcAddr));
					}
				}
			}
			@Override
			public Class<? extends CMessage>[] getSupportedMessages() {
				return new Class[] {
					CCommonMsgs.Status.class
				};
			}
		};
		for(CDriver d : drivers) {
			d.setMessageHandler(handler);
		}
		
		// Wait for 10 seconds
		int n = 0, max = 100;
		while(++n < max) {
			Thread.sleep(100);
			if(n > 50 && epAddrSet.size() > 0) {
				break;
			}
		}
		
		if(n < max) {
			epAddrs = epAddrSet.toArray(new String[epAddrSet.size()]);
			proxyAddrs = proxyAddrSet.toArray(new String[proxyAddrSet.size()]);
			System.out.println(String.format(
					"Discovered %d proxies, %d endpoints", 
					proxyAddrs.length, epAddrs.length));
		}
		else {
			Assert.fail("Failed to discover any endpoints");
		}
		
	}

	@After
	public void after() throws Exception {
		if(endpoints != null) {
			for(CBaseEndpoint e : endpoints) {
				e.shutdown();
			}
		}
		if(proxies != null) {
			for(CBaseProxy p : proxies) {
				p.shutdown();
			}
		}
		if(agents != null) {
			for(CBaseAgent a : agents) {
				a.shutdown();
			}
		}
		if(drivers != null) {
			for(CDefaultDriver d : drivers) {
				d.shutdown();
			}
		}
		if(proxyHub != null && proxyHub instanceof CPduProcessor) {
			((CPduProcessor) proxyHub).shutdown();
		}
	}
	
}
