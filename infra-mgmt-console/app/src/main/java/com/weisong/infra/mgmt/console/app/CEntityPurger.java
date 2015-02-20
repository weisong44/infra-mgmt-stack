package com.weisong.infra.mgmt.console.app;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.weisong.infra.mgmt.console.model.CcmEntity;

public class CEntityPurger extends Thread {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Getter @Setter private long entityTimeout = 10000;
	@Getter private boolean shutdown = false;
	
	public void shutdown() {
		shutdown = true;
	}
	
	public void run() {
		while(shutdown == false) {
			long now = System.currentTimeMillis();
			List<CcmEntity> agents = Ebean.find(CcmEntity.class).findList();
			logger.debug("EntityPurger = {");
			for(CcmEntity e : agents) {
				logger.debug("  " + e);
				if(now - e.getUpdatedAt().getTime() > entityTimeout) {
					Ebean.delete(e);
					logger.info(String.format("Removed %s (last update: %s)", e.getAddress(), e.getUpdatedAt()));
				}
			}
			logger.debug("}");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
