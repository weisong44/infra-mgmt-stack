package com.weisong.infra.mgmt.console.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.weisong.test.comm.message.builtin.CCommonMsgs.Status.StatusValue;
import com.weisong.test.comm.model.CDatabaseObject;
import com.weisong.test.comm.util.AddrUtil;

@Entity
@Table(name = "ccm_entity")
@ToString(callSuper = true)
public class CcmEntity extends CDatabaseObject {

	private static final long serialVersionUID = 1L;
	
	static public enum Type {
		endpoint, agent, proxy, driver
	}
		
	@Getter private Type type;
	@Getter private String objectId;
	@Getter private String address;
	@Getter private Long notificationCount = 0L;
	@Getter @Setter private StatusValue status;
	
	public void incNotificationCount() {
		notificationCount += 1;
	}
	
	public void setAddress(String address) {
		
		this.address = address; 
		this.objectId = address.substring(address.lastIndexOf("/") + 1);
		
		if(AddrUtil.isEndpoint(address)) {
			type = Type.endpoint;
		}
		else if(AddrUtil.isAgent(address)) {
			type = Type.agent;
		}
		else if(AddrUtil.isProxy(address)) {
			type = Type.proxy;
		}
		else if(AddrUtil.isDriver(address)) {
			type = Type.driver;
		}
	}
	
}
