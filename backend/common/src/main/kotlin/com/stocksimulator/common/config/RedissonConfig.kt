package com.stocksimulator.common.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig(
    @Value("\${spring.data.redis.host:localhost}")
    private val host: String,

    @Value("\${spring.data.redis.port:6380}")
    private val port: Int,

    @Value("\${spring.data.redis.password:stocksim123}")
    private val password: String
) {

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config().apply {
            useSingleServer().apply {
                address = "redis://$host:$port"
                setPassword(password)
                connectionMinimumIdleSize = 5
                connectionPoolSize = 20
                idleConnectionTimeout = 10000
                connectTimeout = 10000
                timeout = 3000
                retryAttempts = 3
                @Suppress("DEPRECATION")
                retryInterval = 1500
            }
        }
        return Redisson.create(config)
    }
}
