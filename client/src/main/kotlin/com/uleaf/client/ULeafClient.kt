package com.uleaf.client

import com.uleaf.client.config.MqttConfig
import com.uleaf.client.event.login.LoginEnum.*
import com.uleaf.client.event.login.LoginEvent
import com.uleaf.client.event.login.LoginEventManager
import com.uleaf.client.event.login.LoginListener
import com.uleaf.client.event.message.*
import com.uleaf.client.helper.MessageHelper
import com.uleaf.client.helper.UleafSDKHelper
import com.uleaf.hub.config.ServerConfig
import io.netty.handler.codec.mqtt.MqttQoS

class ULeafClient

fun main(args: Array<String>) {
    UleafSDKHelper.start(ServerConfig("127.0.0.1", 9999, false), MqttConfig(MqttQoS.AT_LEAST_ONCE))

    NetworkEventManager.addListener(object : NetworkListener {
        override fun onChange(event: NetworkEvent) {
            println(event.message)
            when (event.networkEnum) {
                NetworkEnum.CONNECT -> {
                    MessageHelper.login("test", "test")
                }
                NetworkEnum.DISCONNECT -> {
                }
            }
        }

    })


    LoginEventManager.addListener(object : LoginListener {
        override fun onLogin(event: LoginEvent) {
            println(event.message)
            when (event.loginEnum) {
                SUCCESS -> {
                    MessageHelper.sendMsg("hello", "test", "test", "singleChat")
                }
                FAIL -> {

                }
            }
        }

    })
    MessageEventManager.addListener(object : MessageListener {
        override fun onMessage(event: MessageEvent) {
            println("收到消息:${event.msgId} -> ${event.message}")
        }
    })
}

