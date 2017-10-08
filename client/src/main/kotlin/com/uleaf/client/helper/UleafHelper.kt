package com.uleaf.client.helper

import com.uleaf.client.client.ClientService
import com.uleaf.client.config.MqttConfig
import com.uleaf.hub.config.ServerConfig

/**
 * Created by IntelliJ IDEA
 * User: chenfeilong
 * Date: 2017/10/8 16:04
 * Describe:
 */
interface UleafSDK {
    fun start(serverConfig: ServerConfig, mqttConfig: MqttConfig)
    fun stop()
}

object UleafSDKHelper : UleafSDK {
    override fun stop() {
        TODO("not implemented")
    }

    override fun start(serverConfig: ServerConfig, mqttConfig: MqttConfig) {
        MessageHelper.qos = mqttConfig.qoS
        ClientService.boot(serverConfig.host, serverConfig.port)
    }


}