package com.weisong.infra.mgmt.agent.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import com.weisong.test.comm.model.CDatabaseObject;

@Entity  
@Table(name="agent_package")
@Getter @Setter
public class CPackage extends CDatabaseObject {

	public static final long serialVersionUID = -3707128362118583068L;

	static public enum Status {
		installed, running, error, unknown
	}
	
	private String name;
	private String version;
	private String location;
	private Status status;

}
