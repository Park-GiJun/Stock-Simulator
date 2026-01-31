package com.stocksimulator.common.logging

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

/**
 * 로깅 시스템 설정
 *
 * MongoDB 로깅을 활성화하고 MongoTemplate을 MongoDbAppender에 주입
 */
@Configuration
@ConditionalOnProperty(
    prefix = "logging.mongodb",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class LoggingConfig(
    private val mongoDatabaseFactory: MongoDatabaseFactory
) {

    /**
     * 로깅 전용 MongoTemplate
     * _class 필드를 제거하여 저장 용량 절감
     */
    @Bean(name = ["loggingMongoTemplate"])
    fun loggingMongoTemplate(): MongoTemplate {
        val mappingContext = MongoMappingContext()
        val dbRefResolver = DefaultDbRefResolver(mongoDatabaseFactory)
        val converter = MappingMongoConverter(dbRefResolver, mappingContext)

        // _class 필드 제거
        converter.setTypeMapper(DefaultMongoTypeMapper(null))

        return MongoTemplate(mongoDatabaseFactory, converter)
    }

    /**
     * MongoDbAppender에 MongoTemplate 주입
     */
    @PostConstruct
    fun configureMongoDbAppender() {
        try {
            val mongoTemplate = loggingMongoTemplate()
            MongoDbAppender.setMongoTemplate(mongoTemplate)
            println("✅ MongoDbAppender configured with MongoTemplate")
        } catch (e: Exception) {
            System.err.println("❌ Failed to configure MongoDbAppender: ${e.message}")
            e.printStackTrace()
        }
    }
}

/**
 * MongoDB 로깅 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "logging.mongodb")
data class MongoLoggingProperties(
    var enabled: Boolean = false,
    var batchSize: Int = 100,
    var flushIntervalMs: Long = 1000
)
