package com.stocksimulator.stockservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = ["com.stocksimulator.stockservice", "com.stocksimulator.common"]
)
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = ["com.stocksimulator.stockservice.adapter.out.persistence"])
@EntityScan(basePackages = ["com.stocksimulator.stockservice.adapter.out.persistence"])
class StockServiceApplication

fun main(args: Array<String>) {
    runApplication<StockServiceApplication>(*args)
}
