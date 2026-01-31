package com.stocksimulator.eurekaserver.listener

import com.netflix.appinfo.InstanceInfo
import com.stocksimulator.common.logging.CustomLogger
import org.springframework.cloud.netflix.eureka.server.event.*
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Eureka Server 이벤트 리스너
 *
 * 서비스 등록/해제 이벤트를 로깅하여 MongoDB에 저장
 */
@Component
class EurekaEventLogger {

    private val log = CustomLogger(EurekaEventLogger::class.java)

    /**
     * 서비스 등록 이벤트
     */
    @EventListener
    fun onServiceRegistered(event: EurekaInstanceRegisteredEvent) {
        val instance = event.instanceInfo
        log.info(
            "Service registered to Eureka",
            mapOf(
                "instanceId" to instance.instanceId,
                "appName" to instance.appName,
                "ipAddr" to instance.ipAddr,
                "port" to instance.port,
                "status" to instance.status.name,
                "homePageUrl" to instance.homePageUrl
            )
        )
    }

    /**
     * 서비스 등록 해제 이벤트
     */
    @EventListener
    fun onServiceCancelled(event: EurekaInstanceCanceledEvent) {
        log.info(
            "Service cancelled from Eureka",
            mapOf(
                "appName" to event.appName,
                "serverId" to event.serverId
            )
        )
    }

    /**
     * 서비스 갱신 이벤트 (Heartbeat)
     */
    @EventListener
    fun onServiceRenewed(event: EurekaInstanceRenewedEvent) {
        val instance = event.instanceInfo
        log.debug(
            "Service heartbeat renewed",
            mapOf(
                "instanceId" to instance.instanceId,
                "appName" to instance.appName,
                "ipAddr" to instance.ipAddr,
                "status" to instance.status.name
            )
        )
    }

    /**
     * Eureka Server 시작 이벤트
     */
    @EventListener
    fun onEurekaRegistryAvailable(event: EurekaRegistryAvailableEvent) {
        log.info("Eureka Registry is now available")
    }

    /**
     * Eureka Server 시작 이벤트 (레거시)
     */
    @EventListener
    fun onEurekaServerStarted(event: EurekaServerStartedEvent) {
        log.info("Eureka Server has started")
    }
}
