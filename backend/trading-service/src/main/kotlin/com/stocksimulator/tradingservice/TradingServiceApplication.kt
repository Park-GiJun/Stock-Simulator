package com.stocksimulator.tradingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = ["com.stocksimulator.tradingservice", "com.stocksimulator.common"]
)
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = ["com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence"])
@EntityScan(basePackages = ["com.stocksimulator.tradingservice.infrastructure.adapter.out.persistence"])
class TradingServiceApplication

fun main(args: Array<String>) {
    runApplication<TradingServiceApplication>(*args)
}
