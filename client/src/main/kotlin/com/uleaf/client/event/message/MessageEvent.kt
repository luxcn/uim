package com.uleaf.client.event.message


/**
 * Created by IntelliJ IDEA
 * User: chenfeilong
 * Date: 2017/10/8 15:06
 * Describe:
 */

enum class MessageEnum
{
    MSG,
    MSGACK
}
class MessageEvent(val messageEnum: MessageEnum,val msgId:Int,val message: String)


interface MessageListener {
    fun onMessage(event: MessageEvent)
}


object MessageEventManager {
    private val listeners = HashSet<MessageListener>()

    fun addListener(messageListener: MessageListener) {
        listeners.add(messageListener)
    }

    fun removeListener(messageListener: MessageListener) {
        listeners.remove(messageListener)
    }

    fun fireMessage(messageEnum: MessageEnum,msgId: Int,msg: String) {
        notifyListeners(MessageEvent(messageEnum,msgId,msg))
    }

    private fun notifyListeners(event: MessageEvent) {
        listeners.forEach { listener ->
            listener.onMessage(event)
        }
    }

}