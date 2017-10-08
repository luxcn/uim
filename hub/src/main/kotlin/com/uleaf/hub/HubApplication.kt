package com.uleaf.hub

import com.uleaf.hub.config.ServerConfig
import com.uleaf.hub.front.FrontEndService
import com.uleaf.hub.helper.AutowiredHelper
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties


@SpringBootApplication
@EnableConfigurationProperties(ServerConfig::class)
class HubApplication : InitializingBean {
    @Autowired
    val autowiredHelper: AutowiredHelper? = null

    override fun afterPropertiesSet() {
        FrontEndService.boot(autowiredHelper!!)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(HubApplication::class.java, *args)
}




