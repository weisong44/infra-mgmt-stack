package com.weisong.test.comm.transport.address;

import lombok.Getter;

import com.weisong.test.comm.CAddressable;
import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.util.AddrUtil;
import com.weisong.test.comm.util.IdGen;

public class CDefaultAddressable implements CAddressable {

	static public String hostname = AddrUtil.getHostIpAddress();
	
	@Getter protected String id;
	@Getter protected String address;
	@Getter protected CComponentType type;
	
	public CDefaultAddressable(CComponentType type) {
		this.type = type;
		id = IdGen.next(type.toString());
		address = String.format("ccp:%s://%s/%s", type, hostname, id);
	}
}
