package com.uleaf.client.client

import com.uleaf.client.event.login.LoginEnum
import com.uleaf.client.event.login.LoginEventManager
import com.uleaf.client.event.message.MessageEnum
import com.uleaf.client.event.message.MessageEventManager
import com.uleaf.client.event.message.NetworkEnum
import com.uleaf.client.event.message.NetworkEventManager
import com.uleaf.client.helper.ChannelHelper
import com.uleaf.client.helper.MessageHelper
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.mqtt.*


class CmdHandler : ChannelInboundHandlerAdapter() {


    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ChannelHelper.ctx = ctx
        val socketAddress = ctx.channel().remoteAddress()
        println(socketAddress.toString() + ": channelActive")
//        println("start login！user:$userName pwd:$password")
//        val mqttConnectMessage = MqttConnectMessage(MqttFixedHeader(MqttMessageType.CONNECT, false, mqttQoS, false, 0)
//                , MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(), MqttVersion.MQTT_3_1_1.protocolLevel().toInt()
//                , true, true, false, mqttQoS.value(), true, false, 30)
//                , MqttConnectPayload(clientIdentifier, willTopic, willMessage.toByteArray(), userName, password.toByteArray()))
//        ctx.channel().writeAndFlush(mqttConnectMessage)
        NetworkEventManager.fireNetwork(NetworkEnum.CONNECT, "连接已建立")
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        NetworkEventManager.fireNetwork(NetworkEnum.DISCONNECT, "连接已断开")
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
//                        println("login success!")
//                        val context = ctx.alloc().buffer()
//                        val jsonObj = JSONObject()
//                        jsonObj.put("from", from)
//                        jsonObj.put("to", to)
//                        jsonObj.put("msg", willMessage)
//                        context.writeBytes(JSON.toJSONBytes(jsonObj))
//                        sendMsg = MqttPublishMessage(MqttFixedHeader(MqttMessageType.PUBLISH, true, mqttQoS
//                                , false, 0)
//                                , MqttPublishVariableHeader(singleChatTopic, getMessageId()), context)
//                        ctx.channel().writeAndFlush(sendMsg)
                        LoginEventManager.fireLogin(LoginEnum.SUCCESS, "登录成功")
                    }
                    MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD -> {
//                        println("bad user name or password!")
                        LoginEventManager.fireLogin(LoginEnum.FAIL, "用户名或密码错误")

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
                val msgId = mqttPublishMessage.variableHeader().packetId()
                MessageEventManager.fireMessage(MessageEnum.MSG, msgId, String(buf))
                sendMsg = MqttPubAckMessage(MqttFixedHeader(MqttMessageType.PUBACK, false, MessageHelper.qos, false, 0)
                        , MqttMessageIdVariableHeader.from(msgId))
                ctx.channel().writeAndFlush(sendMsg)
            }
            MqttMessageType.PUBACK -> {
                val mqttPubAckMessage = mqttMessage as MqttPubAckMessage
                val msgId = mqttPubAckMessage.variableHeader().messageId()
                MessageEventManager.fireMessage(MessageEnum.MSGACK, msgId, "对方已收到该消息")
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


}