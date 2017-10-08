package com.uleaf.hub.helper

import io.netty.channel.ChannelHandlerContext
import java.util.concurrent.ConcurrentHashMap

object ClientChannelHelper {
    val client2ChannelMap: ConcurrentHashMap<String, ChannelHandlerContext> = ConcurrentHashMap()
    val channel2ClientMap: ConcurrentHashMap<String, String> = ConcurrentHashMap()
}