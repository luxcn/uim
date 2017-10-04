package com.uleaf.hub

import com.uleaf.hub.front.FrontEndService
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.redis.core.StringRedisTemplate


@SpringBootApplication
class HubApplication : InitializingBean {
    @Autowired
    val template: StringRedisTemplate? = null

    override fun afterPropertiesSet() {
        FrontEndService.boot(9999, template!!)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(HubApplication::class.java, *args)
}




