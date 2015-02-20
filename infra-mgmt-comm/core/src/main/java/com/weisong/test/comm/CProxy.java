package com.weisong.test.comm;

import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.pdu.CPdu;

public interface CProxy extends CAddressable {
	
	final static public int DEFAULT_PORT = 5988;
	final static public String BASE_PATH = "/infra-comm-proxy";
	
	public interface AgentRemote {
		String getEpAddr();
		String getAgentAddr();
		void publish(CPdu pdu) throws CException;
	}
	
	AgentRemote getAgentRemote(String epAddr);
	void register(String epAddr, AgentRemote agentRemote) throws CException;
	void publish(CPdu pdu) throws CException;

}
