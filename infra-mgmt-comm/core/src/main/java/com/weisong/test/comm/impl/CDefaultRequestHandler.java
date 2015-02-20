package com.weisong.test.comm.impl;

import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CErrorResponse;
import com.weisong.test.comm.message.builtin.CErrorResponse.Type;

public class CDefaultRequestHandler implements CMessageHandler {

	@Override
	public CResponse onRequest(CRequest request) {
		return request instanceof CCommonMsgs.Ping.Request ?
				new CCommonMsgs.Ping.Response(request)
			  :	new CErrorResponse(request, Type.requestNotSupported);
	}

	@Override
	public void onNotification(CNotification notification) {
		// Do nothing
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends CMessage>[] getSupportedMessages() {
		return new Class[0];
	}

}
