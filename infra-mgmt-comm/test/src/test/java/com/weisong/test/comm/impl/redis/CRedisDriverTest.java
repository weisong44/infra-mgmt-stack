package com.weisong.test.comm.impl.redis;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import com.weisong.test.comm.CBaseDriverTest;
import com.weisong.test.comm.CDriver;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.impl.CDefaultDriver;
import com.weisong.test.comm.impl.CDefaultNotificationHandler;
import com.weisong.test.comm.impl.CRedisHub;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.builtin.CCommonMsgs;

public class CRedisDriverTest extends CBaseDriverTest {

	@Override
	protected void createObjects() throws Exception {
		proxyHub = new CRedisHub("localhost:6379");
		drivers = new CDefaultDriver[] {
				new CDefaultDriver(proxyHub)
			,	new CDefaultDriver(proxyHub)
		};

		// Wait for endpoints to register
		final Set<String> epAddrSet = new HashSet<>(); 
		CMessageHandler handler = new CDefaultNotificationHandler() {
			@Override
			public void onNotification(CNotification n) {
				if(epAddrSet.contains(n.srcAddr) == false) {
					epAddrSet.add(n.srcAddr);
					System.out.println("Discovered " + n.srcAddr);
				}
			}
			@Override
			@SuppressWarnings("unchecked")
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
			if(n > 3 && epAddrSet.size() > 0) {
				break;
			}
		}
		
		if(n < max) {
			epAddrs = epAddrSet.toArray(new String[epAddrSet.size()]);
		}
		else {
			Assert.fail("Failed to discover any endpoints");
		}
		
	}
}
