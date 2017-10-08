package com.uleaf.hub.helper

import com.uleaf.hub.config.ServerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Created by IntelliJ IDEA
 * User: chenfeilong
 * Date: 2017/10/8 00:58
 * Describe:
 */
@Component
class AutowiredHelper {
    @Autowired
    val template: StringRedisTemplate? = null
    @Autowired
    val serverConfig: ServerConfig? = null

}