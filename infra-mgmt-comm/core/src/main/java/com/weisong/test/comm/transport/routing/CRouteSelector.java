package com.weisong.test.comm.transport.routing;

import com.weisong.test.comm.util.AddrUtil;

public interface CRouteSelector {
	boolean isSelected(String address);
	
	static CRouteSelector allSelector = new CRouteSelector() {
		@Override
		public boolean isSelected(String address) {
			return true;
		}
	};

	static CRouteSelector endpointSelector = new CRouteSelector() {
		@Override
		public boolean isSelected(String address) {
			return AddrUtil.isEndpoint(address);
		}
	};

	static CRouteSelector agentSelector = new CRouteSelector() {
		@Override
		public boolean isSelected(String address) {
			return AddrUtil.isAgent(address);
		}
	};

	static CRouteSelector proxySelector = new CRouteSelector() {
		@Override
		public boolean isSelected(String address) {
			return AddrUtil.isProxy(address);
		}
	};

	static CRouteSelector driverSelector = new CRouteSelector() {
		@Override
		public boolean isSelected(String address) {
			return AddrUtil.isDriver(address);
		}
	};

}
