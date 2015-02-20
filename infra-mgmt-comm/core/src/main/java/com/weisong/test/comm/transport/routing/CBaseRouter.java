package com.weisong.test.comm.transport.routing;

import java.util.LinkedList;
import java.util.List;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.transport.codec.CCodec;
import com.weisong.test.comm.transport.codec.CCodecFactory;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.util.AddrUtil;

abstract public class CBaseRouter extends CPduProcessor implements CRouter {
	
	static protected class PduContext {
		private CCodec codec;
		private CMessage message;
		private CPdu pdu;
		protected PduContext(CCodec codec, CPdu pdu) {
			this.codec = codec;
			this.pdu = pdu;
		}
		public CPdu getPdu() {
			return pdu;
		}
		public CMessage getMessage() {
			if(message == null) {
				message = codec.decodeMessage(pdu.payload);
			}
			return message;
		}
	}
	
	protected CCodec codec = CCodecFactory.getOrCreate();

	protected List<CMessageHandler> messageHandlers = new LinkedList<>();

	protected List<CRoute> routes = new LinkedList<>();

	abstract protected void setupRouting();
	
	public CBaseRouter(CComponentType type, CMessageHandler ... messageHandlers) {
		super(type);
		for(CMessageHandler h : messageHandlers) {
			addMessageHandler(h);
		}
	}

	private boolean isAddressedAtMe(CPdu pdu) {
		String addr = pdu.destAddr;
		return address.equals(addr) ||
			(AddrUtil.isAll(addr) && getType() == AddrUtil.getDestType(addr));	
	}
	
	protected void handlePdu(CPdu pdu) {
		try {
			PduContext ctx = new PduContext(codec, pdu);
			CPdu result = isAddressedAtMe(pdu) ?
				handleOwnPdu(ctx)
			  : handleOtherPdu(ctx);
			if(result != null) {
				forward(result);
			}
		} catch (Exception t) {
			drop(pdu, t);
		}
	}
	
	/**
	 * Handles PDU addressed at self, to be implemented by subclass
	 * @param pdu the protocol data unit
	 */
	protected CPdu handleOwnPdu(PduContext ctx) throws CException {
		if(messageHandlers.isEmpty()) {
			return null;
		}
		if(CPdu.Category.request == ctx.getPdu().category) {
			for(CMessageHandler h : messageHandlers) {
				CResponse response = h.onRequest((CRequest) ctx.getMessage());
				if(response != null) {
					return new CPdu(response);
				}
			}
			throw new CException("no request handler found");
		} else if(CPdu.Category.notification == ctx.getPdu().category) {
			for(CMessageHandler h : messageHandlers) {
				h.onNotification((CNotification) ctx.getMessage());
			}
			return null;
		} else {
			drop(ctx.getPdu(), "unknown type " + ctx.getPdu().type);
			return null;
		}
	}
	
	/**
	 * Handles PDU addressed at other components, to be implemented by subclass
	 * @param pdu the protocol data unit
	 */
	protected CPdu handleOtherPdu(PduContext ctx) {
		return ctx.getPdu(); // No action taken here, to be overriden
	}
	
	protected void forward(CPdu pdu) {
		if(pdu == null) {
			return;
		}
		for(CRoute r : routes) {
			try {
				if(r.isSelected(pdu.destAddr)) {
					pdu.lastSrcAddr = address;
					r.forward(pdu);
					return;
				}
			} catch (Throwable t) {
				drop(pdu, t);
			}
		}
		drop(pdu, "no route");
	}
	
	protected void drop(CPdu pdu, String reason) {
		String shortType = pdu.type.substring(
				pdu.type.lastIndexOf(".") + 1, pdu.type.length());
		logger.info(String.format("%s dropped PDU [%s] [\"%s\" -> \"%s\"]: %s", 
				id, shortType, pdu.srcAddr, pdu.destAddr, reason));
	}

	protected void drop(CPdu pdu, Throwable t) {
		t.printStackTrace();
		while(t.getCause() != null && t.getCause().getMessage() != null) {
			t = t.getCause();
		}
		drop(pdu, t.getMessage());
	}

	public void addMessageHandler(CMessageHandler handler) {
		messageHandlers.add(handler);
	}
	
	public void addRoute(CRoute route) {
		routes.add(route);
	}
	
}
