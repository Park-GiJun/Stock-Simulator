package com.stocksimulator.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayRoutesConfig {

    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // User Service Routes
            .route("user-service") { r ->
                r.path("/user-service/**")
                    .filters { f ->
                        f.rewritePath("/user-service/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("lb://user-service")
            }
            // Stock Service Routes
            .route("stock-service") { r ->
                r.path("/stock-service/**")
                    .filters { f ->
                        f.rewritePath("/stock-service/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("lb://stock-service")
            }
            // Trading Service Routes
            .route("trading-service") { r ->
                r.path("/trading-service/**")
                    .filters { f ->
                        f.rewritePath("/trading-service/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("lb://trading-service")
            }
            // Event Service Routes
            .route("event-service") { r ->
                r.path("/event-service/**")
                    .filters { f ->
                        f.rewritePath("/event-service/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("lb://event-service")
            }
            // News Service Routes
            .route("news-service") { r ->
                r.path("/news-service/**")
                    .filters { f ->
                        f.rewritePath("/news-service/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("lb://news-service")
            }
            // Scheduler Service Routes
            .route("scheduler-service") { r ->
                r.path("/scheduler-service/**")
                    .filters { f ->
                        f.rewritePath("/scheduler-service/(?<segment>.*)", "/\${segment}")
                    }
                    .uri("lb://scheduler-service")
            }
            .build()
    }
}
