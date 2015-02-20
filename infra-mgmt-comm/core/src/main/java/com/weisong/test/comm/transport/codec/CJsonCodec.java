package com.weisong.test.comm.transport.codec;

import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.util.JsonUtil;

public class CJsonCodec implements CCodec {

	@Override
	public byte[] encode(CMessage message) {
		return JsonUtil.toJsonString(message).getBytes();
	}

	@Override
	public CMessage decodeMessage(byte[] bytes) {
		return decodeMessage(bytes, CMessage.class);
	}

	@Override
	public <T extends CMessage> T decodeMessage(byte[] bytes, Class<T> clazz) {
		return JsonUtil.toObject(new String(bytes), clazz);
	}

	@Override
	public byte[] encode(CPdu pdu) {
		return JsonUtil.toJsonString(pdu).getBytes();
	}

	@Override
	public CPdu decodePdu(byte[] bytes) {
		return JsonUtil.toObject(new String(bytes), CPdu.class);
	}

}
