package com.uleaf.client.event.message


/**
 * Created by IntelliJ IDEA
 * User: chenfeilong
 * Date: 2017/10/8 15:06
 * Describe:
 */

enum class NetworkEnum {
    CONNECT,
    DISCONNECT
}

class NetworkEvent(val networkEnum: NetworkEnum, val message: String)


interface NetworkListener {
    fun onChange(event: NetworkEvent)
}


object NetworkEventManager {
    private val listeners = HashSet<NetworkListener>()

    fun addListener(networkListener: NetworkListener) {
        listeners.add(networkListener)
    }

    fun removeListener(networkListener: NetworkListener) {
        listeners.remove(networkListener)
    }

    fun fireNetwork(networkEnum: NetworkEnum, msg: String) {
        notifyListeners(NetworkEvent(networkEnum, msg))
    }

    private fun notifyListeners(event: NetworkEvent) {
        listeners.forEach { listener ->
            listener.onChange(event)
        }
    }

}