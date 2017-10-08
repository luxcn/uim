package com.uleaf.client.helper

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.uleaf.client.helper.ChannelHelper.ctx
import io.netty.handler.codec.mqtt.*
import java.util.*

/**
 * Created by IntelliJ IDEA
 * User: chenfeilong
 * Date: 2017/10/8 16:12
 * Describe:
 */
object MessageHelper {
     var qos: MqttQoS? = null

    fun login(userName: String, password: String) {
        val mqttConnectMessage = MqttConnectMessage(MqttFixedHeader(MqttMessageType.CONNECT, false, qos, false, 0)
                , MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(), MqttVersion.MQTT_3_1_1.protocolLevel().toInt()
                , true, true, false, qos!!.value(), true, false, 30)
                , MqttConnectPayload("", "", "".toByteArray(), userName, password.toByteArray()))
        ctx!!.channel().writeAndFlush(mqttConnectMessage)
    }

    fun sendMsg(msg: String, from: String, to: String, topic: String) {
        val context = ctx!!.alloc().buffer()
        val jsonObj = JSONObject()
        jsonObj.put("from", from)
        jsonObj.put("to", to)
        jsonObj.put("msg", msg)
        context.writeBytes(JSON.toJSONBytes(jsonObj))
        val msgPacket = MqttPublishMessage(MqttFixedHeader(MqttMessageType.PUBLISH, true, qos
                , false, 0)
                , MqttPublishVariableHeader(topic, getMessageId()), context)
        ctx!!.channel().writeAndFlush(msgPacket)
    }

    private fun getMessageId(): Int {
        val max = 65535
        val min = 1
        val random = Random()
        return random.nextInt(max) % (max - min + 1) + min
    }
}