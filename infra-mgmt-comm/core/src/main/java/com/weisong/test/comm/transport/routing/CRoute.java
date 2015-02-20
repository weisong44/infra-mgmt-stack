package com.weisong.test.comm.transport.routing;

import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.pdu.CPdu;

public interface CRoute {
	boolean isSelected(String address);
	CRoute addRouteSelector(CRouteSelector s);
	void forward(CPdu pdu) throws CException;
}
