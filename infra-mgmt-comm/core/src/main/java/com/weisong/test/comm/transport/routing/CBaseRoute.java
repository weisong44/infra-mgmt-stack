package com.weisong.test.comm.transport.routing;

import java.util.LinkedList;
import java.util.List;


abstract public class CBaseRoute implements CRoute {

	private List<CRouteSelector> selectors = new LinkedList<>();
	
	public CBaseRoute(CRouteSelector ... array) {
		for(CRouteSelector s : array) {
			selectors.add(s);
		}
	}
	
	public CRoute addRouteSelector(CRouteSelector s) {
		this.selectors.add(s);
		return this;
	}
	
	@Override
	public boolean isSelected(String address) {
		for(CRouteSelector s : selectors) {
			if(s.isSelected(address)) {
				return true;
			}
		}
		return false;
	}
}
