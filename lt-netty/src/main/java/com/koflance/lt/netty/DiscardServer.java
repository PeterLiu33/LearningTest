package com.koflance.lt.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

/**
 * Created by liujun on 2019/2/14.
 */
public class DiscardServer {
    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup linkLoop = new NioEventLoopGroup();
        EventLoopGroup dataLoop = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.channel(NioServerSocketChannel.class)
                    .group(linkLoop, dataLoop)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .attr(AttributeKey.valueOf("serverName"), "nettyServer")
                    .handler(new ChannelInitializer<NioServerSocketChannel>() {

                        @Override
                        protected void initChannel(NioServerSocketChannel serverSocketChannel) throws Exception {
                            System.out.println(serverSocketChannel.attr(AttributeKey.valueOf("serverName")));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture sync = server.bind(port).sync();
            sync.channel().closeFuture().sync();
        } finally {
            linkLoop.shutdownGracefully();
            dataLoop.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new DiscardServer(port).run();
    }
}
