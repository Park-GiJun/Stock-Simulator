package com.stocksimulator.schedulerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    scanBasePackages = ["com.stocksimulator.schedulerservice", "com.stocksimulator.common"]
)
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
class SchedulerServiceApplication

fun main(args: Array<String>) {
    runApplication<SchedulerServiceApplication>(*args)
}
