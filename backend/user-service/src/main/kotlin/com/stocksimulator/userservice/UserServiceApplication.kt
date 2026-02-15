package com.stocksimulator.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = ["com.stocksimulator.userservice", "com.stocksimulator.common"]
)
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = ["com.stocksimulator.userservice.infrastructure.adapter.out.persistence"])
@EntityScan(basePackages = ["com.stocksimulator.userservice.infrastructure.adapter.out.persistence"])
class UserServiceApplication

fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}
