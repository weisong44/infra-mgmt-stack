package com.weisong.test.comm.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.codec.CCodec;
import com.weisong.test.comm.transport.codec.CCodecFactory;
import com.weisong.test.comm.transport.pdu.CPdu;
import com.weisong.test.comm.transport.routing.CPduProcessor;

public final class CWebSocketClient {

	private class ConnectionKeeper extends Thread {
		private boolean shutdown;
		private void shutdown() {
			shutdown = true;
		}
		private void delay(long ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		public void run() {
			setName("CWebSocketClient.ConnectionKeeper");
			while(shutdown == false) {
				for(String url : urls) {
					try {
						if(isConnected() == false) {
							connect(url);
							logger.info(String.format("Connected to %s", url));
						}
					} catch (Exception e) {
						logger.warn(String.format(
								"Failed to connect to %s", url));
					}
				}
				delay(1000);
			}
		}
	}

	static private CCodec codec = CCodecFactory.getOrCreate();
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private String[] urls;
	private Channel channel;
	private EventLoopGroup group;
	private CPduProcessor messageProcessor;
	private ConnectionKeeper connKeeper;

	public CWebSocketClient(String url, CPduProcessor messageProcessor) 
			throws Exception {
		this(new String[] { url }, messageProcessor);
	}
	
	public CWebSocketClient(String[] urls, CPduProcessor messageProcessor) {
		this.urls = urls;
		this.messageProcessor = messageProcessor;
		connKeeper = new ConnectionKeeper();
		connKeeper.start();
	}
	
	public void connect(String url) throws Exception {

		if(isConnected()) {
			return;
		}
		
		URI uri = new URI(url);
		String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
		final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
		final int port;
		if (uri.getPort() == -1) {
			if ("http".equalsIgnoreCase(scheme)) {
				port = 80;
			} else if ("https".equalsIgnoreCase(scheme)) {
				port = 443;
			} else {
				port = -1;
			}
		} else {
			port = uri.getPort();
		}

		if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
			System.err.println("Only WS(S) is supported.");
			return;
		}

		final boolean ssl = "wss".equalsIgnoreCase(scheme);
		final SslContext sslCtx;
		if (ssl) {
			sslCtx = SslContext
					.newClientContext(InsecureTrustManagerFactory.INSTANCE);
		} else {
			sslCtx = null;
		}

		group = new NioEventLoopGroup();
		// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08
		// or V00.
		// If you change it to V00, ping is not supported and remember to
		// change
		// HttpResponseDecoder to WebSocketHttpResponseDecoder in the
		// pipeline.
		WebSocketClientHandshaker handshaker = 
				WebSocketClientHandshakerFactory.newHandshaker(
						uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
		final CWebSocketClientHandler handler = new CWebSocketClientHandler(
				this, handshaker, messageProcessor);

		Bootstrap b = new Bootstrap();
		b.group(group)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) {
				ChannelPipeline p = ch.pipeline();
				if (sslCtx != null) {
					p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
				}
				p.addLast(new HttpClientCodec(), new HttpObjectAggregator(65536), handler);
			}
		});

		channel = b.connect(uri.getHost(), port).sync().channel();
		handler.handshakeFuture().sync();
	}

	public void disconnect() throws Exception {
		if(isConnected()) {
			channel.writeAndFlush(new CloseWebSocketFrame());
			channel.closeFuture().sync();
			group.shutdownGracefully();
			channel = null;
		}
	}

	public boolean isConnected() {
		return channel != null;
	}

	public void shutdown() throws Exception {
		disconnect();
		connKeeper.shutdown();
	}
	
	public void publish(CPdu pdu) throws CException {
		if(isConnected() == false) {
			throw new CException("WebSocket not connected");
		}
		ByteBuf bytes = Unpooled.wrappedBuffer(codec.encode(pdu));
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(bytes);
		channel.writeAndFlush(frame);
	}
}
