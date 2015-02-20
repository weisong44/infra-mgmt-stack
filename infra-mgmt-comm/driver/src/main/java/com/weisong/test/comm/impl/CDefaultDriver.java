package com.weisong.test.comm.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CDriver;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.exception.CTimeoutException;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CDriverMsgs;
import com.weisong.test.comm.message.builtin.CErrorResponse;
import com.weisong.test.comm.transport.address.CDefaultAddressable;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.util.AddrUtil;

public class CDefaultDriver extends CDefaultAddressable implements CDriver, CHub.Listener {
	
	static private class Context {
		private CRequest request;
		private CResponse response;
		@SuppressWarnings("rawtypes")
		private CDriver.Callback callback;
		
		private Context(CRequest request) {
			this.request = request;
		}
		private Context(CRequest request, CDriver.Callback<?> callback) {
			this.request = request;
			this.callback = callback;
		}
	}
	
	private class Housekeeper extends Thread {
		private boolean shutdown;
		public void run() {

			setName(getId() + ".Housekeeper");

			LinkedList<Context> timedout = new LinkedList<>();
			
			while(shutdown == false) {
				try {
					Thread.sleep(1000);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
				synchronized (ctxMap) {
					for(String id : ctxMap.keySet()) {
						Context ctx = ctxMap.get(id);
						if(ctx.callback != null && ctx.request.isTimedOut()) {
							timedout.addLast(ctx);
						}
					}
					for(Context ctx : timedout) {
						ctxMap.remove(ctx.request.id);
					}
				}
				
				CTimeoutException ex = new CTimeoutException("Request timed out!");
				while(timedout.isEmpty() == false) {
					try {
						timedout.removeFirst().callback.onFailure(ex);
					}
					catch(Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}
	}
	
	private class ReportingWorker extends Thread {
		private boolean shutdown;
		public void run() {
			setName(getId() + ".ReportingWorker");
			while(shutdown == false) {
				try {
					hub.publish(driverProfileNotificationPdu);
					Thread.sleep(1000);
				} 
				catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private CMessageHandler messageHandler;
	
	private CHub hub;
	
	private Map<String, Context> ctxMap = new HashMap<>();
	
	private CPdu driverProfileNotificationPdu;
	
	private Housekeeper housekeeper;

	private ReportingWorker reportingWorker;
	
	public CDefaultDriver(CHub hub) {
		super(CComponentType.driver);
		this.hub = hub;
		hub.register(address, this);
		
		String[] supportedNotifications = new String[] {
			CCommonMsgs.Status.class.getName()	
		};
		driverProfileNotificationPdu = new CPdu(new CDriverMsgs.Profile(
				address, AddrUtil.getAllProxies(), supportedNotifications));

		(housekeeper = new Housekeeper()).start();
		(reportingWorker = new ReportingWorker()).start();
	}

	@Override
	public void setMessageHandler(CMessageHandler handler) {
		this.messageHandler = handler;
		
		String[] notifications = new String[0];
		Class<?>[] classes = handler.getSupportedMessages();
		if(classes != null && classes.length > 0) {
			notifications = new String[classes.length];
			for(int i = 0; i < classes.length; i++) {
				notifications[i] = classes[i].getName();
			}
		}
		driverProfileNotificationPdu = new CPdu(new CDriverMsgs.Profile(
				address, AddrUtil.getAllProxies(), notifications));
	}

	public void send(CNotification message) throws CException {
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("%s sends [%s]", getId(), message));
		}
		hub.publish(new CPdu(message));
	}

	@SuppressWarnings("unchecked")
	public <REQ extends CRequest, RSP extends CResponse> 
			RSP send(REQ request)
			throws CException {
		Context ctx = new Context(request);
		synchronized (ctxMap) {
			ctxMap.put(request.id, ctx);
		}
		
		//logger.info(String.format("%s sent [%s]", getId(), request));
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("%s sends [%s]", getId(), request));
		}
		
		hub.publish(new CPdu(request));
		
		synchronized (ctx) {
			try {
				ctx.wait(request.timeout);
			} catch (InterruptedException ex) {
				throw new CException(ex);
			}
		}
		
		if(ctx.response == null) {
			synchronized (ctxMap) {
				ctxMap.remove(request.id);
			}
			throw new CTimeoutException("Request timed out!");
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("%s received [%s]", getId(), ctx.response));
		}

		return (RSP) ctx.response;
	}

	public <REQ extends CRequest, RSP extends CResponse> 
			void sendAsync(REQ request, CDriver.Callback<RSP> callback) 
			throws CException {
		Context ctx = new Context(request, callback);
		synchronized (ctxMap) {
			ctxMap.put(request.id, ctx);
		}
		hub.publish(new CPdu(request));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onHubPdu(CPdu pdu) {
		if(pdu.isResponse()) {
			CResponse response = pdu.toResponse();
			Context ctx = null;
			synchronized (ctxMap) {
				ctx = ctxMap.remove(response.requestId);
			}
			if(ctx == null) {
				return; // expired
			}
			if(ctx.callback == null) {
				ctx.response = response;
				synchronized(ctx) {
					ctx.notifyAll();
				}
			}
			else {
				try {
					if(response instanceof CErrorResponse) {
						ctx.callback.onFailure(((CErrorResponse) response).getError());
					}
					else {
						ctx.callback.onSuccess(response);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		else if (pdu.isNotification()) {
			CNotification notification = pdu.toNotification();
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("%s received [%s]", getId(), notification));
			}
			if(messageHandler != null) {
				messageHandler.onNotification(notification);
			}
		}
	}

	@Override
	public void shutdown() {
		try {
			housekeeper.shutdown = true;
			housekeeper.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			reportingWorker.shutdown = true;
			reportingWorker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
