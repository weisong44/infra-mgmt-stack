package com.weisong.infra.mgmt.agent.message;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.weisong.infra.mgmt.agent.model.CPackage;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;
import com.weisong.test.comm.message.builtin.CCommonMsgs;

public class CAgentMsgs {
	static public class ListPackages {
		@Getter @Setter @ToString(callSuper = true)
		static public class Request extends CRequest {
			private static final long serialVersionUID = 1L;
			protected Request() {
			}
			public Request(String srcAddr, String destAddr) {
				super(srcAddr, destAddr);
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class Response extends CResponse {
			private static final long serialVersionUID = 1L;
			private List<CPackage> pkgList; 
			protected Response() {
			}
			public Response(CRequest request, List<CPackage> pkgList) {
				super(request);
				this.pkgList = pkgList;
			}
		}
	}
	
	static public class AddPackage {
		@Getter @Setter @ToString(callSuper = true)
		static public class Request extends CRequest {
			private static final long serialVersionUID = 1L;
			private CPackage pkg; 
			protected Request() {
			}
			public Request(String srcAddr, String destAddr, CPackage pkg) {
				super(srcAddr, destAddr);
				this.pkg = pkg;
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class Response extends CResponse {
			private static final long serialVersionUID = 1L;
			private CPackage pkg; 
			protected Response() {
			}
			public Response(CRequest request, CPackage pkg) {
				super(request);
				this.pkg = pkg;
			}
		}
	}
	
	static public class RemovePackage {
		@Getter @Setter @ToString(callSuper = true)
		static public class Request extends CRequest {
			private static final long serialVersionUID = 1L;
			private String id; 
			protected Request() {
			}
			public Request(String srcAddr, String destAddr, String id) {
				super(srcAddr, destAddr);
				this.id = id;
			}
		}
		@Getter @Setter @ToString(callSuper = true)
		static public class Response extends CResponse {
			private static final long serialVersionUID = 1L;
			protected Response() {
			}
			public Response(CRequest request) {
				super(request);
			}
		}
	}
	
	@Getter @Setter @ToString(callSuper = true)
	static public class AgentStatus extends CCommonMsgs.Status {
		private static final long serialVersionUID = 1L;
		protected AgentStatus() {
		}	
		public AgentStatus(String srcAddr, String destAddr, StatusValue status) {
			super(srcAddr, destAddr, status);
		}
	}
}
