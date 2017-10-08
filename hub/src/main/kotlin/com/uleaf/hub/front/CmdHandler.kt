package com.uleaf.hub.front

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.uleaf.hub.helper.ClientChannelHelper
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.mqtt.*
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.*
import java.util.concurrent.TimeUnit

class CmdHandler(private val stringRedisTemplate: StringRedisTemplate) : ChannelInboundHandlerAdapter() {
    private val mqttQoS: MqttQoS = MqttQoS.AT_LEAST_ONCE
    private val singleChatTopic = "singleChat"

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.fireChannelInactive()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val mqttMessage = msg as MqttMessage
        val socketAddress = ctx.channel().remoteAddress()
        println(socketAddress.toString() + ": " + mqttMessage.toString())
        val msgType: MqttMessageType = mqttMessage.fixedHeader().messageType()
        val sendMsg: MqttMessage
        val opsForHash = stringRedisTemplate.opsForHash<String, String>()
        when (msgType) {
            MqttMessageType.CONNECT -> {
                val mqttConnectMessage = mqttMessage as MqttConnectMessage
                val userName = mqttConnectMessage.payload().userName()
                val password = String(mqttConnectMessage.payload().passwordInBytes())
                val userNameKey = "user_$userName"
                val pwd = stringRedisTemplate.opsForValue().get(userNameKey)
                if (password == pwd) {
                    ClientChannelHelper.client2ChannelMap.put(userName, ctx)
                    ClientChannelHelper.channel2ClientMap.put(ctx.channel().remoteAddress().toString(), userName)
                    val willTopic = mqttConnectMessage.payload().willTopic()
                    stringRedisTemplate.opsForSet().add("topic_$willTopic", userName)
                    sendMsg = MqttConnAckMessage(MqttFixedHeader(MqttMessageType.CONNACK, false, mqttQoS, false, 0)
                            , MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false))
                    val msgListKey = "msg_list_$userName"
                    val members = stringRedisTemplate.opsForSet().members(msgListKey)
                    val collection = HashSet<String>()
                    collection.add("to")
                    collection.add("from")
                    collection.add("msg")
                    for (s in members.stream()) {
                        val msgDetailKey = "msg_detail_$s"
                        val list = opsForHash.multiGet(msgDetailKey, collection)
                        val jsonObj = JSONObject()
                        jsonObj.put("to", list[0])
                        jsonObj.put("from", list[1])
                        jsonObj.put("msg", list[2])
                        val context = ctx.alloc().buffer()
                        context.writeBytes(JSON.toJSONBytes(jsonObj))
                        val mSendMsg = MqttPublishMessage(MqttFixedHeader(MqttMessageType.PUBLISH, true, mqttQoS
                                , false, 0)
                                , MqttPublishVariableHeader(singleChatTopic, s.toInt()), context)
                        ctx.channel().writeAndFlush(mSendMsg)
                    }

                } else {
                    sendMsg = MqttConnAckMessage(MqttFixedHeader(MqttMessageType.CONNACK, false, mqttQoS, false, 0)
                            , MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false))
                }
                ctx.channel().writeAndFlush(sendMsg)

            }
            MqttMessageType.PUBLISH -> {
                val mqttPublishMessage = mqttMessage as MqttPublishMessage
                val payload = mqttPublishMessage.payload()
                val buf = kotlin.ByteArray(payload.readableBytes())
                payload.copy().readBytes(buf)
                val json = JSON.parseObject(String(buf))
                val to = json.getString("to")
                val from = json.getString("from")
                val mMsg = json.getString("msg")
                if (!stringRedisTemplate.hasKey("user_$to")) {
                    println("不存在的客户端:$to")
                    return
                }
                val packetId = mqttPublishMessage.variableHeader().packetId()
                val msgListKey = "msg_list_$to"
                stringRedisTemplate.opsForSet().add(msgListKey, packetId.toString())
                stringRedisTemplate.expire(msgListKey, 1, TimeUnit.DAYS)
                val msgDetailKey = "msg_detail_$packetId"
                opsForHash.put(msgDetailKey, "from", from)
                opsForHash.put(msgDetailKey, "to", to)
                opsForHash.put(msgDetailKey, "msg", mMsg)
                stringRedisTemplate.expire(msgDetailKey, 1, TimeUnit.DAYS)
                val toMsg = MqttPublishMessage(MqttFixedHeader(MqttMessageType.PUBLISH, false, mqttQoS, false, 0)
                        , MqttPublishVariableHeader(singleChatTopic, packetId)
                        , payload)
                val toCtx = ClientChannelHelper.client2ChannelMap[to]
                toCtx?.writeAndFlush(toMsg)

            }
            MqttMessageType.PUBACK -> {
                val mqttPubAckMessage = mqttMessage as MqttPubAckMessage
                val messageId = mqttPubAckMessage.variableHeader().messageId().toString()
                val userName = ClientChannelHelper.channel2ClientMap[ctx.channel().remoteAddress().toString()]
                val msgListKey = "msg_list_$userName"
                stringRedisTemplate.opsForSet().remove(msgListKey, messageId)

                val msgDetailKey = "msg_detail_$messageId"
                val from = opsForHash.get(msgDetailKey, "from")
                stringRedisTemplate.delete(msgDetailKey)
                val fromCtx = ClientChannelHelper.client2ChannelMap[from!!]
                sendMsg = MqttPubAckMessage(MqttFixedHeader(MqttMessageType.PUBACK, false, mqttQoS, false, 0)
                        , MqttMessageIdVariableHeader.from(mqttPubAckMessage.variableHeader().messageId()))
                fromCtx?.writeAndFlush(sendMsg)

            }
            else -> {
                println("other code:$msgType")
            }
        }


    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // Close the connection when an exception is raised.
        cause.printStackTrace()
        ctx.close()
    }

}