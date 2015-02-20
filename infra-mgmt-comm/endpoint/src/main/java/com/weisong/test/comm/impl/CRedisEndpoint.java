package com.weisong.test.comm.impl;

import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.exception.CException;

public class CRedisEndpoint extends CBaseEndpoint {

	public CRedisEndpoint(CMessageHandler handler, String hostAndPort)
			throws CException {
		super(new CRedisHub(hostAndPort), handler);
	}
}
