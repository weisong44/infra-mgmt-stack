package com.weisong.test.comm.impl;

import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;

abstract public class CDefaultNotificationHandler implements CMessageHandler {

	@Override
	public CResponse onRequest(CRequest request) {
		throw new RuntimeException("Not supported");
	}
}
