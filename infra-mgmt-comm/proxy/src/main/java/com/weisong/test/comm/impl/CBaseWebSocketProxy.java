package com.weisong.test.comm.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import com.weisong.test.comm.CHub;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.codec.CCodec;
import com.weisong.test.comm.transport.codec.CCodecFactory;
import com.weisong.test.comm.transport.pdu.CPdu;

abstract public class CBaseWebSocketProxy extends CBaseProxy {

	static protected CCodec codec = CCodecFactory.getOrCreate();
	
	static public class WebSocketAgentRemote implements AgentRemote {
		protected String epAddr;
		protected String agentAddr;
		protected ChannelHandlerContext ctx;
		protected WebSocketAgentRemote(String epAddr, String agentAddr, ChannelHandlerContext ctx) {
			this.epAddr = epAddr;
			this.agentAddr = agentAddr;
			this.ctx = ctx;
		}
		@Override
		public String getEpAddr() {
			return epAddr;
		}
		@Override
		public String getAgentAddr() {
			return agentAddr;
		}
		@Override
		public void publish(CPdu pdu) throws CException {
			ByteBuf bytes = Unpooled.wrappedBuffer(codec.encode(pdu));
			BinaryWebSocketFrame frame = new BinaryWebSocketFrame(bytes);
			ctx.channel().writeAndFlush(frame);
		}
	}
		
	public CBaseWebSocketProxy(CHub hub) throws Exception {
		super(hub);
	}
}
