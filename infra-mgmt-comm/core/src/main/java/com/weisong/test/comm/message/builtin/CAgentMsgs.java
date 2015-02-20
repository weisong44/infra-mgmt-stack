package com.weisong.test.comm.message.builtin;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.model.CObject;

public class CAgentMsgs {
	static public class ListEndpoints {
		@Getter @Setter @ToString(callSuper = true)
		static public class Request extends CRequest {
			private static final long serialVersionUID = 1L;
			protected Request() {}
			public Request(String srcAddr, String destAddr) {
				super(srcAddr, destAddr);
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class EndpointProfile extends CObject {
			private static final long serialVersionUID = 1L;
			private String address;
			protected EndpointProfile() {}
			public EndpointProfile(String address) {
				this.address = address;
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class Response extends CResponse {
			private static final long serialVersionUID = 1L;
			private List<EndpointProfile> epList;
			protected Response() {}
			public Response(CRequest request, List<EndpointProfile> epList) {
				super(request);
				this.epList = epList;
			}
		}
	}
}
