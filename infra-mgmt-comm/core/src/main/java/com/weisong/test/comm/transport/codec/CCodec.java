package com.weisong.test.comm.transport.codec;

import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.transport.pdu.CPdu;

public interface CCodec {
	
	byte[] encode(CMessage message);
	CMessage decodeMessage(byte[] bytes);
	<T extends CMessage> T decodeMessage(byte[] bytes, Class<T> clazz);
	
	byte[] encode(CPdu pdu);
	CPdu decodePdu(byte[] bytes);
	
}
