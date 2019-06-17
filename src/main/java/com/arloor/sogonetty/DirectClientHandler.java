/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.arloor.sogonetty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

public final class DirectClientHandler extends ChannelInboundHandlerAdapter {

    private final Promise<Channel> promise;
    private final String dstAddr;
    private final int dstPort;
    private final String basicAuth;

    public DirectClientHandler(Promise<Channel> promise, String targetAddr, int targetPort,String basicAuth) {
        this.promise = promise;
        this.dstAddr=targetAddr;
        this.dstPort=targetPort;
        this.basicAuth=basicAuth;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //连接完毕后，增加handler：所有写操作包裹http请求
        ctx.pipeline().addLast(new RelayOverHttpRequestHandler(dstAddr,dstPort,basicAuth));
        //连接完毕后，增加handler：去除读到的http响应包裹
        ctx.pipeline().addLast(new HttpResponseDecoder());
        ctx.pipeline().remove(this);
        promise.setSuccess(ctx.channel());
    }

    private static class CheckConnectedHandler extends ChannelInboundHandlerAdapter{
//todo:检测是否连接完毕。
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        promise.setFailure(throwable);
    }
}
