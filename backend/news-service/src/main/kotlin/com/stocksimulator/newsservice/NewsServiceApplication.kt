package com.stocksimulator.newsservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class NewsServiceApplication

fun main(args: Array<String>) {
    runApplication<NewsServiceApplication>(*args)
}
