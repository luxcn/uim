package com.uleaf.hub.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by IntelliJ IDEA
 * User: chenfeilong
 * Date: 2017/10/8 14:49
 * Describe:
 */
@ConfigurationProperties("conf.server")
class ServerConfig {
    var port: Int? = null
    var sslFlag: Boolean? = null
}