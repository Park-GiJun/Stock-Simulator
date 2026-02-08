package com.stocksimulator.gateway

import com.stocksimulator.gateway.config.AuthProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(AuthProperties::class)
@ComponentScan(basePackages = ["com.stocksimulator.gateway", "com.stocksimulator.apigateway"])
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
