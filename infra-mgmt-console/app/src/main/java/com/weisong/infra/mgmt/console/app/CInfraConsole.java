package com.weisong.infra.mgmt.console.app;

import java.util.Iterator;
import java.util.Set;

import org.reflections.Reflections;

import com.weisong.infra.mgmt.console.data.CInitEbean;
import com.weisong.infra.mgmt.console.handler.CConsoleStatusHandler;
import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CDriver;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.impl.CDefaultDriver;
import com.weisong.test.comm.impl.CHazelcastHub;
import com.weisong.test.comm.impl.CMessageHandlerDelegate;

public class CInfraConsole {
	
	static {
		CInitEbean.init();
	}

	private CHub hub;
	private CDriver driver; 
	private CEntityPurger purger;

	public CInfraConsole() throws Exception {
		CMessageHandlerDelegate delegate = new CMessageHandlerDelegate(findHandlerClasses());
		hub = new CHazelcastHub(CComponentType.driver);
		driver = new CDefaultDriver(hub); 
		driver.setMessageHandler(delegate);
		
		// Start purger
		(purger = new CEntityPurger()).start();
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends CMessageHandler>[] findHandlerClasses() {
		Reflections reflections = new Reflections("com.weisong");
		Set<Class<? extends CMessageHandler>> set = reflections.getSubTypesOf(CMessageHandler.class);
		String basePkgName = CConsoleStatusHandler.class.getPackage().getName();
		for(Iterator<Class<? extends CMessageHandler>> i = set.iterator(); i.hasNext();) {
			if(i.next().getName().contains(basePkgName) == false) {
				i.remove();
			}
		}
		return set.toArray(new Class[set.size()]);
	}
	
	public void shutdown() throws Exception {
		purger.shutdown();
		purger.join();
	}
	
	static private void printUsageAndExit() {
		System.out.println("Usage:");
		System.out.println("    java CInfraConsole");
		System.exit(-1);
	}
	
	static public void main(String[] args) throws Exception {
		if(args.length == 1) {
			if("-h".equals(args[0]) || "-help".equals(args[0])) {
				printUsageAndExit();
			}
		}

		if(args.length > 1) {
			printUsageAndExit();
		}
		
		CInfraConsole console = new CInfraConsole();
		
		synchronized (console) {
			console.wait();
		}
	}
}
