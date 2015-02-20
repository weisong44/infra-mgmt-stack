package com.weisong.infra.mgmt.agent.data;

import org.avaje.agentloader.AgentLoader;

public class CInitEbean {
	static public void init() {
        boolean loaded = AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent","debug=1");
        if(loaded == false) {
        	System.err.println("ebeanorm agent not found!");
        	System.exit(-1);
        }
	}
}
