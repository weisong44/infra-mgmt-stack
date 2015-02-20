package com.weisong.test.comm.impl.websocket;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.impl.CHazelcastWebSocketProxy;
import com.weisong.test.comm.impl.CWebSocketClient;
import com.weisong.test.comm.impl.CWebSocketServer;
import com.weisong.test.comm.impl.local.CLocalHub;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CPduProcessor;

public class CWebSocketClientTest {
	
	final static public String[] urls = new String[] {
			"ws://faked-url-going-nowhere:8080"
		 ,	"ws://localhost:8080"
	};
	
	public class DummyMessageProcessor extends CPduProcessor {
		public DummyMessageProcessor() {
			super(CComponentType.endpoint);
		}
		@Override
		protected void handlePdu(CPdu pdu) {
			System.out.println(pdu);
		}
	}
	
	private class Server extends Thread {
		public void run() {
			try {
				logger.info("WebSocketServer started!");
				CHub hub = new CLocalHub();
				CHazelcastWebSocketProxy proxy = new CHazelcastWebSocketProxy(hub);
				new CWebSocketServer(proxy);
			} catch (InterruptedException e) {
				// Ignore
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private void shutdown() {
			logger.info("WebSocketServer shutdown!");
			interrupt();
		}
	}

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Test
	public void testConnect() throws Exception {
		CWebSocketClient wsClient = new CWebSocketClient(
				urls, new DummyMessageProcessor());
		// Not connected
		Assert.assertEquals(false, wsClient.isConnected());
		for(int i = 0; i < 3; i++) {
			Server server = new Server();
			// Connected
			server.start(); delay(2000);
			Assert.assertEquals(true, wsClient.isConnected());
			// Disconnected
			server.shutdown();; delay(2000);
			Assert.assertEquals(false, wsClient.isConnected());
		}
	}
	
	private void delay(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
