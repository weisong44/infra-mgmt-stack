package com.weisong.test.comm.message.builtin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;

public class CCommonMsgs {
	static public class Ping {
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
			protected Response() {
			}
			public Response(CRequest request) {
				super(request);
			}
		}
	}
	
	@Getter @Setter @ToString(callSuper = true)
	static public class Reset extends CNotification {
		private static final long serialVersionUID = 1L;
		protected Reset() {
		}	
		public Reset(String srcAddr, String destAddr) {
			super(srcAddr, destAddr);
		}
	}

	@Getter @Setter @ToString(callSuper = true)
	static public class Status extends CNotification {
		private static final long serialVersionUID = 1L;
		public enum StatusValue {
			Ok, Error
		}
		private StatusValue status;
		protected Status() {
		}	
		public Status(String srcAddr, String destAddr, StatusValue status) {
			super(srcAddr, destAddr);
			this.status = status;
		}
	}
}
