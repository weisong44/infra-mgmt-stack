package com.weisong.test.comm;

import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;

public interface CDriver extends CAddressable {

	public interface Callback<RSP extends CResponse> {
		void onSuccess(RSP response);
		void onFailure(Throwable t);
	}

	void setMessageHandler(CMessageHandler handler);

	/**
	 * Send a notification to a destination
	 * 
	 * @param notification the notification 
	 * @throws CException
	 */
	<N extends CNotification> 
			void send(N notification) throws CException;

	/**
	 * Send a request to a destination, and wait for response synchronously.
	 * It may time out.
	 * 
	 * @param request the request
	 * @return response
	 * @throws CException
	 */
	<REQ extends CRequest, RSP extends CResponse> 
			RSP send(REQ request) throws CException;

	/**
	 * Send a request to a destination, and wait for response asynchronously.
	 * It may time out.
	 * 
	 * @param request the request
	 * @param callback the callback
	 * @throws CException
	 */
	<REQ extends CRequest, RSP extends CResponse> 
			void sendAsync(REQ request, Callback<RSP> callback) 
			throws CException;

	void shutdown();
}
