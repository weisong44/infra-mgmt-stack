package com.weisong.test.comm.transport.routing;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.transport.address.CDefaultAddressable;
import com.weisong.test.comm.transport.pdu.CPdu;

abstract public class CPduProcessor extends CDefaultAddressable {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private LinkedBlockingQueue<CPdu> PduQueue = new LinkedBlockingQueue<>();
	private PduHandler[] handlers;

	/**
	 * Handle the PDU, to be implemented by subclass
	 * @param pdu the protocol data unit
	 */
	abstract protected void handlePdu(CPdu pdu);
	
	private class PduHandler extends Thread {
		private boolean shutdown;
		@Override
		public void run() {
			setName(CPduProcessor.this.getId() + ".PduHandler");
			while(shutdown == false) {
				try {
					CPdu pdu = PduQueue.take();
					if(logger.isDebugEnabled()) {
						logger.debug(String.format("%s handles [%s]", CPduProcessor.this.getId(), pdu));
					}
					handlePdu(pdu);
				} 
				catch (InterruptedException ex) {
					// Ignore
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	public CPduProcessor(CComponentType type) {
		super(type);
		int concurrency = Math.min(4, Runtime.getRuntime().availableProcessors());
		handlers = new PduHandler[concurrency];
		for(int i = 0; i < concurrency; i++) {
			handlers[i] = new PduHandler();
			handlers[i].setName(getId() + "." + "MsgHandler-" + (i + 1));
			handlers[i].start();
		}
	}
	
	public void publish(CPdu pdu) {
		if(logger.isDebugEnabled()) {
			logger.debug(String.format("%s bufferred [%s]", getId(), pdu));
		}
		PduQueue.add(pdu);
	}
	
	public void shutdown() {
		for(PduHandler h : handlers) {
			h.shutdown = true;
			h.interrupt();
		}
		for(PduHandler h : handlers) {
			try {
				h.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
