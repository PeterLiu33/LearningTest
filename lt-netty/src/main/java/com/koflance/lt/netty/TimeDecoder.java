package com.koflance.lt.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 处理拆包粘包问题
 *
 * Created by liujun on 2019/2/15.
 */
public class TimeDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes() < 4){
            // 判断缓存可读个数
            return;
        }
        list.add(byteBuf.readBytes(4));
    }
}
