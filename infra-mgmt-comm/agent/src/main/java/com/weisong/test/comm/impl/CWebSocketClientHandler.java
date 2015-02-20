package com.weisong.test.comm.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.test.comm.transport.codec.CCodec;
import com.weisong.test.comm.transport.codec.CCodecFactory;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CPduProcessor;

public class CWebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

	static private CCodec codec = CCodecFactory.getOrCreate();
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private final CWebSocketClient wsClient;
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

	private CPduProcessor messageProcessor;
    
    public CWebSocketClientHandler(CWebSocketClient wsClient, 
    		WebSocketClientHandshaker handshaker, CPduProcessor messageProcessor) {
    	this.wsClient = wsClient;
        this.handshaker = handshaker;
        this.messageProcessor = messageProcessor;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
			wsClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                "Unexpected FullHttpResponse (getStatus=" + response.getStatus() +
                        ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof BinaryWebSocketFrame) {
        	BinaryWebSocketFrame bf = (BinaryWebSocketFrame) frame;
        	if(bf.isFinalFragment()) {
            	byte[] bytes = null;
        		if(bf.content().hasArray()) {
                	bytes = bf.content().array();
        		}
        		else {
                	ByteBuf buf = bf.content();
                	bytes = new byte[buf.readableBytes()];
                	buf.readBytes(bytes);
        		}
            	CPdu pdu = codec.decodePdu(bytes);
                messageProcessor.publish(pdu);
        	}
        	else {
            	logger.warn("Skipping non-final fragment ...");
        	}
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
