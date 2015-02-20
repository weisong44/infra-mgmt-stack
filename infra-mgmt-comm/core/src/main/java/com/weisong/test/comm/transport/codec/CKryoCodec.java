package com.weisong.test.comm.transport.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.weisong.test.comm.message.CMessage;
import com.weisong.test.comm.transport.pdu.CPdu;

public class CKryoCodec implements CCodec {

	final static public int BUFFER_SIZE = 32 * 1024;
	
	static private ThreadLocal<Kryo> kryoThreadLocal;
	
	private boolean compress;

	public CKryoCodec(final boolean compress, final Collection<Class<?>> classes) {
		this.compress = compress;
		synchronized (CKryoCodec.class) {
			if(kryoThreadLocal == null) {
				kryoThreadLocal = new ThreadLocal<Kryo>() {
					@Override protected Kryo initialValue() {
						Kryo kryo = new Kryo();
						for(Class<?> c : classes) {
							// The order of Id's must be identical across JVM's!
							int id = Math.abs(c.getName().hashCode());
							kryo.register(c, id);
						}
						return kryo;
					}
				};
			}
		}
	}

	@Override
	public byte[] encode(CMessage message) {
		return write(message);
	}

	@Override
	public CMessage decodeMessage(byte[] bytes) {
		return decodeMessage(bytes, CMessage.class);
	}

	@Override
	public <T extends CMessage> T decodeMessage(byte[] bytes, Class<T> clazz) {
		return (T) read(bytes, clazz);
	}

	@Override
	public byte[] encode(CPdu pdu) {
		return write(pdu);
	}

	@Override
	public CPdu decodePdu(byte[] bytes) {
		return (CPdu) read(bytes);
	}

	@SuppressWarnings("unchecked")
	private <T> T read(byte[] bytes, Class<T> clazz) {
		return (T) read(bytes);
	}

	private Object read(byte[] bytes) {
		InputStream is = new ByteArrayInputStream(bytes);
		if (compress) {
			is = new InflaterInputStream(is);
		}
		Input input = new Input(is);
		Kryo kryo = kryoThreadLocal.get();
		return kryo.readClassAndObject(input);
	}

	private byte[] write(Object o) {
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		OutputStream os = compress ? 
				new DeflaterOutputStream(byteArrayOutputStream)
			  : byteArrayOutputStream;
		try(Output output = new Output(os)) {
			kryo.writeClassAndObject(output, o);
		}
		return byteArrayOutputStream.toByteArray();
	}
}
