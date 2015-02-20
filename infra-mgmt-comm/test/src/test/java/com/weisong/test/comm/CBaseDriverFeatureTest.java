package com.weisong.test.comm;

import junit.framework.Assert;

import org.junit.Test;

import com.weisong.test.comm.exception.CTimeoutException;
import com.weisong.test.comm.message.builtin.CAgentMsgs;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CCommonMsgs.Status.StatusValue;
import com.weisong.test.comm.message.builtin.CProxyMsgs;

abstract public class CBaseDriverFeatureTest extends CBaseDriverTest {

	static private class AsyncContext {
		private CCommonMsgs.Ping.Response response;
		private Throwable error;
	}
	
	@Test
	public void testGetProxyDetails() throws Exception {
		CDriver driver = drivers[0];
		for(String addr : proxyAddrs) {
			CProxyMsgs.GetDetails.Request request = new CProxyMsgs.GetDetails.Request(
					driver.getAddress(), addr);
			CProxyMsgs.GetDetails.Response response = driver.send(request);
			Assert.assertNotNull(response);
			System.out.println("Details of proxy " + addr + ":");
			System.out.println("  address: " + response.getDetails().getAddress());
		}
	}
	
	@Test
	public void testListAgents() throws Exception {
		CDriver driver = drivers[0];
		System.out.println("Overall structure");
		for(String proxyAddr : proxyAddrs) {
			System.out.println("  " + proxyAddr);
			CProxyMsgs.ListAgents.Request request = new CProxyMsgs.ListAgents.Request(
					driver.getAddress(), proxyAddr);
			CProxyMsgs.ListAgents.Response response = driver.send(request);
			Assert.assertNotNull(response);
			for(CProxyMsgs.ListAgents.AgentProfile profile : response.getAgentList()) {
				System.out.println("    " + profile.getAddress());
				CAgentMsgs.ListEndpoints.Request epReq = new CAgentMsgs.ListEndpoints.Request(
						driver.getAddress(), profile.getAddress());
				CAgentMsgs.ListEndpoints.Response epResponse = driver.send(epReq);
				for(CAgentMsgs.ListEndpoints.EndpointProfile epProfile : epResponse.getEpList()) {
					System.out.println("      " + epProfile.getAddress());
				}
			}
		}
	}
	
	@Test
	public void testSend() throws Exception {
		for(final CDriver driver : drivers) {
			for(int i = 0; i < epAddrs.length; i++) {
				final CCommonMsgs.Ping.Request request = new CCommonMsgs.Ping.Request(driver.getAddress(), epAddrs[i]);
				CCommonMsgs.Ping.Response response = driver.send(request);
				Assert.assertNotNull(response);
			}
		}
	}
	
	@Test
	public void testSendAsync() throws Exception {
		for(final CDriver driver : drivers) {
			for(int i = 0; i < epAddrs.length; i++) {
				final AsyncContext ctx = new AsyncContext();
				final CCommonMsgs.Ping.Request request = new CCommonMsgs.Ping.Request(driver.getAddress(), epAddrs[i]);
				driver.sendAsync(request, new CDriver.Callback<CCommonMsgs.Ping.Response>() {
					@Override
					public void onSuccess(CCommonMsgs.Ping.Response response) {
						Assert.assertNotNull(response);
						ctx.response = response;
					}

					@Override
					public void onFailure(Throwable ex) {
						ex.printStackTrace();
						Assert.fail("Async invocation failed.");
					}
				});
				
				for(int n = 0; n < 10; n++) {
					if(ctx.response != null) {
						break;
					}
					Thread.sleep(100);
				}
				
				if(ctx.response == null) {
					Assert.fail("Async request timeout!");
				}
				
			}
		}
	}
	
	@Test
	public void testOneway() throws Exception {
		for(CDriver driver : drivers) {
			CCommonMsgs.Status message = new CCommonMsgs.Status(
					epAddrs[0], driver.getAddress(), StatusValue.Ok);
			driver.send(message);
		}
		
		Thread.sleep(2000);
	}

	@Test(expected = CTimeoutException.class)
	public void testSendFailed() throws Exception {
		final CCommonMsgs.Ping.Request request = new CCommonMsgs.Ping.Request("fake", "fake");
		CCommonMsgs.Ping.Response response = drivers[0].send(request);
		Assert.assertNotNull(response);
	}
	
	@Test(expected = CTimeoutException.class)
	public void testSendAsyncFailed() throws Throwable {
		final AsyncContext ctx = new AsyncContext();
		final CCommonMsgs.Ping.Request request = new CCommonMsgs.Ping.Request("fake", "fake");
		drivers[0].sendAsync(request, new CDriver.Callback<CCommonMsgs.Ping.Response>() {
			@Override
			public void onSuccess(CCommonMsgs.Ping.Response response) {
				Assert.fail("Async invocation should fail!");
			}

			@Override
			public void onFailure(Throwable error) {
				ctx.error = error;
			}
		});

		for(int i = 0; i < 8; i++) {
			Thread.sleep(1000);
			if(ctx.error != null) {
				throw ctx.error;
			}
		}
		
		Assert.fail("Async request didn't timeout!");
		
	}
	
}
