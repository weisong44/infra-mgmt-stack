package com.weisong.test.comm;

import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;


public interface CMessageHandler {
	CResponse onRequest(CRequest request);
	void onNotification(CNotification notification);
	Class<? extends CMessage>[] getSupportedMessages();
}
