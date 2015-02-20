package com.weisong.test.comm.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.test.comm.CMessageHandler;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.message.CNotification;
import com.weisong.test.comm.message.CRequest;
import com.weisong.test.comm.message.CResponse;

public class CMessageHandlerDelegate implements CMessageHandler {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Class<? extends CMessage>[] supportedClasses;
	
	private Map<Class<? extends CMessage>, CMessageHandler> handlerMap = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public CMessageHandlerDelegate(Class<? extends CMessageHandler>[] handlerClasses) throws Exception {
		final Set<Class<? extends CMessage>> supportedClassSet = new HashSet<>();
		for(Class<? extends CMessageHandler> hClass : handlerClasses) {
			CMessageHandler handler = hClass.newInstance();
			for(Class<? extends CMessage> msgClass : handler.getSupportedMessages()) {
				if(supportedClassSet.contains(msgClass)) {
					logger.warn(String.format("Class %s is already handled by %s, it will be overwritten", 
						msgClass.getName(), handlerMap.get(msgClass).getClass().getName()));
				}
				supportedClassSet.add(msgClass);
				handlerMap.put(msgClass, handler);
			}
		}
		supportedClasses = supportedClassSet.toArray(new Class[supportedClassSet.size()]);
	}

	@Override
	public CResponse onRequest(CRequest request) {
		CMessageHandler handler = handlerMap.get(request.getClass());
		if(handler != null) {
			return handler.onRequest(request);
		}
		throw new RuntimeException("no handler found for " + request.getClass().getName());
	}
	
	@Override
	public void onNotification(CNotification notification) {
		CMessageHandler handler = handlerMap.get(notification.getClass());
		if(handler != null) {
			handler.onNotification(notification);
		}
		else {
			throw new RuntimeException("no handler found for " + notification.getClass().getName());
		}
	}

	@Override
	public Class<? extends CMessage>[] getSupportedMessages() {
		return supportedClasses;
	}
}
