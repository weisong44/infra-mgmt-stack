package com.weisong.test.comm.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
abstract public class CResponse extends CMessage {
	
	private static final long serialVersionUID = 1L;

	/** Request message Id */
	public String requestId;
	
	/** Request timestamp */
	public Long requestTimestamp;
	
	public CResponse() {
	}

	public CResponse(CRequest request) {
		super(request.destAddr, request.srcAddr);
		requestId = request.id;
	}
}
