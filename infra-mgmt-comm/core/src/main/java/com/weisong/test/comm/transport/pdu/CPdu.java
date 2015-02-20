package com.weisong.test.comm.transport.pdu;

import java.io.Serializable;

import lombok.ToString;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.transport.codec.CCodec;
import com.weisong.test.comm.transport.codec.CCodecFactory;

@ToString
public class CPdu implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final CCodec codec = CCodecFactory.getOrCreate();
	
	public enum Category {
		request, response, notification, unknown
	}
	
	public Category category;
	public String type;
	public String srcAddr;
	public String lastSrcAddr;
	public String destAddr;
	public byte[] payload;

	protected CPdu() {
	}
	
	public CPdu(CPdu pdu) {
		this.category = pdu.category;
		this.type = pdu.type;
		this.srcAddr = pdu.srcAddr;
		this.destAddr = pdu.destAddr;
		this.payload = pdu.payload;
	}
	
	public CPdu(CMessage message) {
		if(message instanceof CRequest) {
			category = Category.request;
		}
		else if(message instanceof CResponse) {
			category = Category.response;
		}
		else if(message instanceof CNotification) {
			category = Category.notification;
		}
		else {
			category = Category.unknown;
		}
		this.type = message.getClass().getName();
		this.srcAddr = message.srcAddr;
		this.destAddr = message.destAddr;
		this.payload = codec.encode(message);
	}

	public CMessage toMessage() {
		return codec.decodeMessage(payload);
	}
	
	public <T extends CMessage> T toMessage(Class<T> clazz) {
		return codec.decodeMessage(payload, clazz);
	}
	
	public CRequest toRequest() {
		return toMessage(CRequest.class); 
	}
	
	public CResponse toResponse() {
		return toMessage(CResponse.class); 
	}
	
	public CNotification toNotification() {
		return toMessage(CNotification.class); 
	}
	
	@JsonIgnore
	public boolean isRequest() {
		return category == Category.request;
	}
	
	@JsonIgnore
	public boolean isResponse() {
		return category == Category.response;
	}
	
	@JsonIgnore
	public boolean isNotification() {
		return category == Category.notification;
	}
}
