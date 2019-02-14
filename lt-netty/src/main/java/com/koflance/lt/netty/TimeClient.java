package com.koflance.lt.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by liujun on 2019/2/15.
 */
public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup linkLoop = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client.channel(NioSocketChannel.class)
                    .group(linkLoop)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            socketChannel.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
                            socketChannel.pipeline().addLast(new UnixTimeDecoder(), new UnixTimeClientHandler());
                        }
                    });

//            ChannelFuture sync = client.connect(args[0], Integer.valueOf(args[1])).sync();
            ChannelFuture sync = client.connect("localhost",8080).sync();
            sync.channel().closeFuture().sync();
        } finally {
            linkLoop.shutdownGracefully();
        }
    }
}
