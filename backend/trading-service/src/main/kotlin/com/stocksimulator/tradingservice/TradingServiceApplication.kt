package com.stocksimulator.tradingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class TradingServiceApplication

fun main(args: Array<String>) {
    runApplication<TradingServiceApplication>(*args)
}
