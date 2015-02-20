package com.weisong.test.comm.message.builtin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;

@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
public class CErrorResponse extends CResponse {
	
	private static final long serialVersionUID = 1L;

	public enum Type {
		requestNotSupported,
		requestHandlerNotFound,
		other
	}
	
	@Getter @Setter
	protected Type type;
	
	@Getter @Setter
	protected String message;
	
	@Getter @Setter
	protected Throwable error;
	
	protected CErrorResponse() {
	}
	
	public CErrorResponse(CRequest request, Type type) {
		super(request);
		requestId = request.id;
		this.type = type;
	}
	
	public CErrorResponse(CRequest request, String message) {
		super(request);
		requestId = request.id;
		this.type = Type.other;
		this.message = message;
	}
	
	public CErrorResponse(CRequest request, String message, Throwable error) {
		super(request);
		requestId = request.id;
		this.type = Type.other;
		this.message = message;
		this.error = error;
	}
}
