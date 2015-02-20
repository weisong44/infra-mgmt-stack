package com.weisong.test.comm.impl;

import java.net.BindException;

import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CProxy;
import com.weisong.test.comm.impl.CHazelcastHub;

public class CHazelcastWebSocketProxy extends CBaseWebSocketProxy {

	public CHazelcastWebSocketProxy(CHub hub) throws Exception {
		super(hub);
	}
	
	static public void main(String[] args) throws Exception {
		CHub hub = new CHazelcastHub(CComponentType.proxy);
		CHazelcastWebSocketProxy proxy = new CHazelcastWebSocketProxy(hub);

		int port = CProxy.DEFAULT_PORT;
		while(true) {
			try {
				new CWebSocketServer(proxy, port, false);
			} catch (BindException e) {
				System.out.println(String.format("Port %d in use, try %d ...", port, ++port));
			}
		}
	}
}
