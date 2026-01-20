package com.stocksimulator.stockservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class StockServiceApplication

fun main(args: Array<String>) {
    runApplication<StockServiceApplication>(*args)
}
