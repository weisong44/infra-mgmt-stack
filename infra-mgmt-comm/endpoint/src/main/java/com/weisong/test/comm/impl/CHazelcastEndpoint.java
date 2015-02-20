package com.weisong.test.comm.impl;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CMessageHandler;

public class CHazelcastEndpoint extends CBaseEndpoint {

	public CHazelcastEndpoint(CMessageHandler ... handlers)
			throws Exception {
		super(new CHazelcastHub(CComponentType.endpoint), handlers);
	}
}
