package com.uleaf.client.client

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.uleaf.client.helper.ChannelHelper
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.mqtt.*
import java.util.Random


class CmdHandler : ChannelInboundHandlerAdapter() {
    private val mqttQoS: MqttQoS = MqttQoS.AT_LEAST_ONCE
    private val from = "test"
    private val to = "test"
    private val singleChatTopic = "singleChat"
    private val willTopic = "lux"
    private val clientIdentifier = ""
    private val willMessage = "hello test"
    private val userName = "test"
    private val password = "test"

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ChannelHelper.ctx = ctx
        val socketAddress = ctx.channel().remoteAddress()
        println(socketAddress.toString() + ": channelActive")
        println("start login！user:$userName pwd:$password")
        val mqttConnectMessage = MqttConnectMessage(MqttFixedHeader(MqttMessageType.CONNECT, false, mqttQoS, false, 0)
                , MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(), MqttVersion.MQTT_3_1_1.protocolLevel().toInt()
                , true, true, false, mqttQoS.value(), true, false, 30)
                , MqttConnectPayload(clientIdentifier, willTopic, willMessage.toByteArray(), userName, password.toByteArray()))
        ctx.channel().writeAndFlush(mqttConnectMessage)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.fireChannelInactive()
    }


    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val mqttMessage = msg as MqttMessage
        val socketAddress = ctx.channel().remoteAddress()
        println(socketAddress.toString() + ": " + mqttMessage.toString())
        val msgType: MqttMessageType = mqttMessage.fixedHeader().messageType()
        val sendMsg: MqttMessage
        when (msgType) {
            MqttMessageType.CONNACK -> {
                val mqttConnAckMessage = mqttMessage as MqttConnAckMessage
                val connectReturnCode = mqttConnAckMessage.variableHeader().connectReturnCode()
                when (connectReturnCode) {
                    MqttConnectReturnCode.CONNECTION_ACCEPTED -> {
                        println("login success!")
                        val context = ctx.alloc().buffer()
                        val jsonObj = JSONObject()
                        jsonObj.put("from", from)
                        jsonObj.put("to", to)
                        jsonObj.put("msg", willMessage)
                        context.writeBytes(JSON.toJSONBytes(jsonObj))
                        sendMsg = MqttPublishMessage(MqttFixedHeader(MqttMessageType.PUBLISH, true, mqttQoS
                                , false, 0)
                                , MqttPublishVariableHeader(singleChatTopic, getMessageId()), context)
                        ctx.channel().writeAndFlush(sendMsg)
                    }
                    MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD -> {
                        println("bad user name or password!")
                    }
                    else -> {
                        println("other code:$connectReturnCode")
                    }

                }
            }
            MqttMessageType.PUBLISH -> {
                val mqttPublishMessage = mqttMessage as MqttPublishMessage
                val payload = mqttPublishMessage.payload()
                val buf = kotlin.ByteArray(payload.readableBytes())
                payload.readBytes(buf)
                println("收到消息:${String(buf)}")
                sendMsg = MqttPubAckMessage(MqttFixedHeader(MqttMessageType.PUBACK, false, mqttQoS, false, 0)
                        , MqttMessageIdVariableHeader.from(mqttPublishMessage.variableHeader().packetId()))
                ctx.channel().writeAndFlush(sendMsg)
            }
            MqttMessageType.PUBACK -> {
                println("对方已收到该消息")
            }
            else -> {
                println("other code:$msgType")
            }
        }


    }


    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.fireExceptionCaught(cause)
    }

    private fun getMessageId(): Int {
        val max = 65535
        val min = 1
        val random = Random()
        return random.nextInt(max) % (max - min + 1) + min
    }
}