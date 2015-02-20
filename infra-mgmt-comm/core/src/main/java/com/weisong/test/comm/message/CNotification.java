package com.weisong.test.comm.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
abstract public class CNotification extends CMessage {
	
	private static final long serialVersionUID = 1L;
	
	protected CNotification() {
	}	
	
	public CNotification(String srcAddr, String destAddr) {
		super(srcAddr, destAddr);
	}
}
