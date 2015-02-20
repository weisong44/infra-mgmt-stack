package com.weisong.test.comm.message.builtin;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.model.CObject;

public class CProxyMsgs {
	static public class ListAgents {
		@Getter @Setter @ToString(callSuper = true)
		static public class Request extends CRequest {
			private static final long serialVersionUID = 1L;
			protected Request() {}
			public Request(String srcAddr, String destAddr) {
				super(srcAddr, destAddr);
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class AgentProfile extends CObject {
			private static final long serialVersionUID = 1L;
			private String address;
			protected AgentProfile() {}
			public AgentProfile(String address) {
				this.address = address;
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class Response extends CResponse {
			private static final long serialVersionUID = 1L;
			private List<AgentProfile> agentList;
			protected Response() {}
			public Response(CRequest request, List<AgentProfile> agentList) {
				super(request);
				this.agentList = agentList;
			}
		}
	}
	
	static public class GetDetails {
		@Getter @Setter @ToString(callSuper = true)
		static public class Request extends CRequest {
			private static final long serialVersionUID = 1L;
			protected Request() {
			}
			public Request(String srcAddr, String destAddr) {
				super(srcAddr, destAddr);
			}
		}
		@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
		static public class ProxyDetails extends CObject {
			private static final long serialVersionUID = 1L;
			private String address;
			protected ProxyDetails() {}
			public ProxyDetails(String address) {
				this.address = address;
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class Response extends CResponse {
			private static final long serialVersionUID = 1L;
			private ProxyDetails details;
			protected Response() {
			}
			public Response(CRequest request, ProxyDetails details) {
				super(request);
				this.details = details;
			}
		}
	}

}
