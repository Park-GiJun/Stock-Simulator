package com.stocksimulator.seasonservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class SeasonServiceApplication

fun main(args: Array<String>) {
    runApplication<SeasonServiceApplication>(*args)
}
