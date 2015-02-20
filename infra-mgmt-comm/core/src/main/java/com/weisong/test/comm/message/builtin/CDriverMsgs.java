package com.weisong.test.comm.message.builtin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.weisong.test.comm.message.CNotification;

public class CDriverMsgs {

	@Getter @Setter @ToString(callSuper = true)
	static public class Profile extends CNotification {
		private static final long serialVersionUID = 1L;
		private String[] supportedNotifications;
		protected Profile() {
		}	
		public Profile(String srcAddr, String destAddr, String[] supportedNotifications) {
			super(srcAddr, destAddr);
			this.supportedNotifications = supportedNotifications;
		}
	}

}
