package com.uleaf.hub.helper

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelId

object ClientChannelHelper {
    var client2ChannelMap: HashMap<String, ChannelHandlerContext> = HashMap()
    var channel2ClientMap: HashMap<String, String> = HashMap()
}