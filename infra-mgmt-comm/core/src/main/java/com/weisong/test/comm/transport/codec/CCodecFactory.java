package com.weisong.test.comm.transport.codec;

import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.model.CObject;
import com.weisong.test.comm.transport.pdu.CPdu;

@SuppressWarnings("unused")
public class CCodecFactory {
	
	static private CJsonCodec jsonCodec;
	static private CKryoCodec kryoCodec;
	
	final static private CCodec codec;

	static {
		codec = createKryoCodec();
		//codec = createJsonCodec();
	}
	
	static public CCodec getOrCreate() {
		return codec;
	}
	
	static private CCodec createJsonCodec() {
		return jsonCodec = new CJsonCodec();
	}
	
	static private CCodec createKryoCodec() {
		Reflections reflections = new Reflections("com.weisong.test");
		Set<Class<?>> set = new HashSet<>();
		set.addAll(reflections.getSubTypesOf(CObject.class));
		set.addAll(reflections.getSubTypesOf(CMessage.class));
		set.addAll(reflections.getSubTypesOf(CPdu.class));
		/*
		for(Class<?> c : set) {
			System.out.println(" - " + c.getName());
		}
		*/
		return new CKryoCodec(false, set);
	}
}
