package com.uleaf.client.event.login

/**
 * Created by IntelliJ IDEA
 * User: chenfeilong
 * Date: 2017/10/8 16:46
 * Describe:
 */

enum class LoginEnum {
    SUCCESS,
    FAIL
}

class LoginEvent(val loginEnum: LoginEnum, val message: String)


interface LoginListener {
    fun onLogin(event: LoginEvent)
}


object LoginEventManager {
    private val listeners = HashSet<LoginListener>()

    fun addListener(loginListener: LoginListener) {
        listeners.add(loginListener)
    }

    fun removeListener(loginListener: LoginListener) {
        listeners.remove(loginListener)
    }

    fun fireLogin(loginEnum: LoginEnum, msg: String) {
        notifyListeners(LoginEvent(loginEnum, msg))
    }

    private fun notifyListeners(event: LoginEvent) {
        listeners.forEach { listener ->
            listener.onLogin(event)
        }
    }

}
