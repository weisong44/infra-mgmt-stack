package com.weisong.infra.mgmt.agent.handler;

import java.util.List;

import com.avaje.ebean.Ebean;
import com.weisong.infra.mgmt.agent.message.CAgentMsgs;
import com.weisong.infra.mgmt.agent.model.CPackage;
import com.weisong.test.comm.impl.CDefaultRequestHandler;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;

public class CAgentPackageHandler extends CDefaultRequestHandler {

	@Override
	public CResponse onRequest(CRequest request) {
		if(request instanceof CAgentMsgs.AddPackage.Request) {
			return addPackage((CAgentMsgs.AddPackage.Request) request);
		}
		else if(request instanceof CAgentMsgs.ListPackages.Request) {
			return listPackages((CAgentMsgs.ListPackages.Request) request);
		}
		else if(request instanceof CAgentMsgs.RemovePackage.Request) {
			return removePackage((CAgentMsgs.RemovePackage.Request) request);
		}
		else {
			throw new RuntimeException("Should not reach here!");
		}
	}
	
	private CAgentMsgs.AddPackage.Response addPackage(CAgentMsgs.AddPackage.Request request) {
		Ebean.save(request.getPkg());
		return new CAgentMsgs.AddPackage.Response(request, request.getPkg());
	}

	private CAgentMsgs.ListPackages.Response listPackages(CAgentMsgs.ListPackages.Request request) {
		List<CPackage> list = Ebean.find(CPackage.class).findList();
		return new CAgentMsgs.ListPackages.Response(request, list);
	}

	private CAgentMsgs.RemovePackage.Response removePackage(CAgentMsgs.RemovePackage.Request request) {
		Ebean.delete(CPackage.class, request.getId());
		return new CAgentMsgs.RemovePackage.Response(request);
	}

}
