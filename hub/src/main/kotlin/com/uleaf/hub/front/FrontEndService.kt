package com.uleaf.hub.front

import com.uleaf.hub.helper.AutowiredHelper
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.mqtt.MqttDecoder
import io.netty.handler.codec.mqtt.MqttEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import kotlin.concurrent.thread

object FrontEndService {

    fun boot(autowiredHelper: AutowiredHelper) {
        thread(start = true, name = "FrontService") {
            println("running FrontService on ${autowiredHelper.serverConfig!!.port}  from thread: ${Thread.currentThread()}")
            // Configure the server.
            val bossGroup = NioEventLoopGroup(1)
            val workerGroup = NioEventLoopGroup()
            try {
                val b = ServerBootstrap()
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel::class.java)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .handler(LoggingHandler(LogLevel.INFO))
                        .childHandler(object : ChannelInitializer<SocketChannel>() {
                            @Throws(Exception::class)
                            public override fun initChannel(ch: SocketChannel) {
                                ch.pipeline().addLast(MqttDecoder())
                                ch.pipeline().addLast(MqttEncoder.INSTANCE)
                                ch.pipeline().addLast(CmdHandler(autowiredHelper.template!!))
                            }
                        })

                // Start the server.
                val f = b.bind(autowiredHelper.serverConfig!!.port!!).sync()

                // Wait until the server socket is closed.
                f.channel().closeFuture().sync()
            } finally {
                // Shut down all event loops to terminate all threads.
                bossGroup.shutdownGracefully()
                workerGroup.shutdownGracefully()
            }
        }
    }
}