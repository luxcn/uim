package com.uleaf.client.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.mqtt.MqttDecoder
import io.netty.handler.codec.mqtt.MqttEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import kotlin.concurrent.thread

object ClientService {
    fun boot(host: String, port: Int) {
        thread(start = true, name = "ClientService") {
            println("running FrontService on $host:$port  from thread: ${Thread.currentThread()}")
            val bootstrap = Bootstrap()
            val worker = NioEventLoopGroup()
            try {
                bootstrap.group(worker)
                        .channel(NioSocketChannel::class.java)
                        .handler(object : ChannelInitializer<SocketChannel>() {
                            @Throws(Exception::class)
                            override fun initChannel(ch: SocketChannel) {
                                ch.pipeline().addLast(LoggingHandler(LogLevel.TRACE))
                                ch.pipeline().addLast(MqttDecoder())
                                ch.pipeline().addLast(MqttEncoder.INSTANCE)
                                ch.pipeline().addLast(CmdHandler())
                            }
                        })
                val future = bootstrap.connect(host, port).sync()
                future.channel().closeFuture().sync()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                worker.shutdownGracefully()
            }
        }
    }
}