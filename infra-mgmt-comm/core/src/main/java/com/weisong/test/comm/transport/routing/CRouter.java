package com.weisong.test.comm.transport.routing;

import com.weisong.test.comm.CMessageHandler;


public interface CRouter {
	void addMessageHandler(CMessageHandler handler);
	void addRoute(CRoute route);
}
