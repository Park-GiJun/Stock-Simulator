package com.stocksimulator.eurekaserver.listener

import com.netflix.appinfo.InstanceInfo
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.eureka.server.event.*
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Eureka Server 이벤트 리스너
 *
 * 서비스 등록/해제 이벤트를 로깅
 */
@Component
class EurekaEventLogger {

    private val log = LoggerFactory.getLogger(EurekaEventLogger::class.java)

    /**
     * 서비스 등록 이벤트
     */
    @EventListener
    fun onServiceRegistered(event: EurekaInstanceRegisteredEvent) {
        val instance = event.instanceInfo
        log.info(
            "Service registered to Eureka: instanceId={}, appName={}, ipAddr={}, port={}, status={}, homePageUrl={}",
            instance.instanceId,
            instance.appName,
            instance.ipAddr,
            instance.port,
            instance.status.name,
            instance.homePageUrl
        )
    }

    /**
     * 서비스 등록 해제 이벤트
     */
    @EventListener
    fun onServiceCancelled(event: EurekaInstanceCanceledEvent) {
        log.info(
            "Service cancelled from Eureka: appName={}, serverId={}",
            event.appName,
            event.serverId
        )
    }

    /**
     * 서비스 갱신 이벤트 (Heartbeat)
     */
    @EventListener
    fun onServiceRenewed(event: EurekaInstanceRenewedEvent) {
        val instance = event.instanceInfo
        log.debug(
            "Service heartbeat renewed: instanceId={}, appName={}, ipAddr={}, status={}",
            instance.instanceId,
            instance.appName,
            instance.ipAddr,
            instance.status.name
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
