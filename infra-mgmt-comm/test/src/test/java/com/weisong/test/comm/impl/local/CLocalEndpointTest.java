package com.weisong.test.comm.impl.local;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.impl.CDefaultRequestHandler;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CCommonMsgs.Status.StatusValue;
import com.weisong.test.comm.transport.pdu.CPdu;

public class CLocalEndpointTest {

	final private boolean debug = false;
	
	private enum Result {
		notificationReceived, unknown
	}
	
	private Result result = Result.unknown;
	private CLocalHub hub;
	private CLocalEndpoint src, dest;

	@Before
	public void prepare() throws CException {
		if(debug) {
			System.out.println("==");
		}
		hub= new CLocalHub("hub");
		src = new CLocalEndpoint(hub, new CDefaultRequestHandler() {
			@Override
			public void onNotification(CNotification notification) {
				result = Result.notificationReceived;
				if(debug) {
					System.out.println(Thread.currentThread().getName() + 
							" received " + notification.getClass().getSimpleName());
				}
			}
		});
		
		dest = new CLocalEndpoint(hub);
		if(debug) {
			System.out.println("Source:      " + src.getAddress());
			System.out.println("Destination: " + dest.getAddress());
		}
	}

	@Test
	public void testSend() throws Exception {
		CCommonMsgs.Status status = new CCommonMsgs.Status(
				dest.getAddress(), src.getAddress(), StatusValue.Ok);
		src.publish(new CPdu(status));
		
		int n = 0;
		while(n < 5000 && result == Result.unknown) {
			Thread.sleep(1);
			++n;
		}

		Assert.assertEquals(Result.notificationReceived, result);
	}
}
