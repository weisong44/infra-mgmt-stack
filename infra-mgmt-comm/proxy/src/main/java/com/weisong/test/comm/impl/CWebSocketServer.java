package com.weisong.test.comm.impl;

import com.weisong.test.comm.CProxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class CWebSocketServer {

	private class WebSocketInitializer extends ChannelInitializer<SocketChannel> {

	    private final SslContext sslCtx;

	    public WebSocketInitializer(SslContext sslCtx) {
	        this.sslCtx = sslCtx;
	    }

	    @Override
	    public void initChannel(SocketChannel ch) throws Exception {
	        ChannelPipeline pipeline = ch.pipeline();
	        if (sslCtx != null) {
	            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
	        }
	        pipeline.addLast(new HttpServerCodec());
	        pipeline.addLast(new HttpObjectAggregator(65536));
	        pipeline.addLast(new CWebSocketServerHandler(proxy));
	    }
	}
	
	static public boolean useSsl;
	
    private Channel channel;
    private CBaseWebSocketProxy proxy;
    
	public CWebSocketServer(CBaseWebSocketProxy proxy) 
			throws Exception {
		this(proxy, false);
	}
		
	public CWebSocketServer(CBaseWebSocketProxy proxy, boolean useSsl) 
			throws Exception {
		this(proxy, CProxy.DEFAULT_PORT, useSsl);
	}
		
	public CWebSocketServer(CBaseWebSocketProxy proxy, int port, boolean useSsl) 
			throws Exception {
		this.proxy = proxy;

		CWebSocketServer.useSsl = useSsl; 
		
        // Configure SSL.
        final SslContext sslCtx;
        if (useSsl) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new WebSocketInitializer(sslCtx));

            channel = b.bind(port).sync().channel();

            System.out.println(String.format("Listening at %s://127.0.0.1:%d",
                    useSsl? "https" : "http", port));

            channel.closeFuture().sync();
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
	
	public void shutdown() {
		if(channel != null) {
			channel.close();
			channel = null;
		}
	}

}
