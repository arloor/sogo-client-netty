package com.arloor.sogonetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ByteProcessor;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpResponseDecoder extends ByteToMessageDecoder {
    private int contentLength=0;
    private State state= State.START;

    private enum State{
        START,CONTENTLENGTH,CRLFCRLF,CONTENT
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        switch (state){
            case START:
                if(in.readableBytes()<=74){
                    return;
                }else{
                    in.readerIndex(in.readerIndex()+74);
                    state= State.CONTENTLENGTH;
                }
            case CONTENTLENGTH:
                int index=in.forEachByte(ByteProcessor.FIND_CRLF);
                if(index==-1){
                    return;
                }else {
                    CharSequence cs=in.readCharSequence(index-in.readerIndex(), StandardCharsets.UTF_8);
                    contentLength=Integer.parseInt(cs.toString());

                    state= State.CRLFCRLF;
                }
            case CRLFCRLF:
                if(in.readableBytes()<4){
                    return;
                }else {
                    in.readerIndex(in.readerIndex()+4);
                    state= State.CONTENT;
                }
            case CONTENT:
                if(in.readableBytes()<contentLength){
                    return;
                }else {
                    ByteBuf buf=in.readSlice(contentLength);
                    ByteBuf content = PooledByteBufAllocator.DEFAULT.buffer();
                    buf.forEachByte(value -> {
                        content.writeByte(~value);
                        return true;
                    });
                    out.add(content);
                    state= State.START;
                }
        }
    }
}
