package com.weisong.test.comm.message;

import org.codehaus.jackson.annotate.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
abstract public class CRequest extends CMessage {
	
	private static final long serialVersionUID = 1L;

	public Long createdAt = System.currentTimeMillis();
	public Long timeout = 5000L;
	
	protected CRequest() {
	}
	
	public CRequest(String srcAddr, String destAddr) {
		super(srcAddr, destAddr);
	}
	
	@JsonIgnore
	public boolean isTimedOut() {
		return System.currentTimeMillis() > createdAt + timeout;
	}
}
