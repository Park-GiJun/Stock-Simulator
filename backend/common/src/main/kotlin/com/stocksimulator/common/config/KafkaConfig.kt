package com.stocksimulator.common.config

import com.stocksimulator.common.event.KafkaTopics
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerializer


@EnableKafka
@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers:localhost:9093}")
    private val bootstrapServers: String,

    @Value("\${spring.application.name:unknown}")
    private val applicationName: String
) {

    // ===== Producer Configuration =====

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonJsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            "spring.json.add.type.headers" to true
        )
        return DefaultKafkaProducerFactory<String, Any>(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory())
    }

    // ===== Consumer Configuration =====

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to applicationName,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JacksonJsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            "spring.json.trusted.packages" to "com.stocksimulator.*"
        )
        return DefaultKafkaConsumerFactory<String, Any>(configProps)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
            setConsumerFactory(consumerFactory())
            setConcurrency(3)
            containerProperties.ackMode = org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE
        }
    }

    // ===== Topic Configuration =====

    @Bean
    fun orderCreatedTopic(): NewTopic = createTopic(KafkaTopics.ORDER_CREATED)

    @Bean
    fun orderMatchedTopic(): NewTopic = createTopic(KafkaTopics.ORDER_MATCHED)

    @Bean
    fun orderCancelledTopic(): NewTopic = createTopic(KafkaTopics.ORDER_CANCELLED)

    @Bean
    fun tradeExecutedTopic(): NewTopic = createTopic(KafkaTopics.TRADE_EXECUTED)

    @Bean
    fun priceUpdatedTopic(): NewTopic = createTopic(KafkaTopics.PRICE_UPDATED)

    @Bean
    fun orderBookUpdatedTopic(): NewTopic = createTopic(KafkaTopics.ORDERBOOK_UPDATED)

    @Bean
    fun eventOccurredTopic(): NewTopic = createTopic(KafkaTopics.EVENT_OCCURRED)

    @Bean
    fun rankingUpdatedTopic(): NewTopic = createTopic(KafkaTopics.RANKING_UPDATED)

    @Bean
    fun scheduleTradeTopic(): NewTopic = createTopic(KafkaTopics.SCHEDULE_TRADE)

    @Bean
    fun incomeDistributedTopic(): NewTopic = createTopic(KafkaTopics.INCOME_DISTRIBUTED)

    @Bean
    fun userCreatedTopic(): NewTopic = createTopic(KafkaTopics.USER_CREATED)

    // Scheduler Trigger Topics
    @Bean
    fun triggerStockListingTopic(): NewTopic = createTopic(KafkaTopics.TRIGGER_STOCK_LISTING)

    @Bean
    fun triggerStockDelistingTopic(): NewTopic = createTopic(KafkaTopics.TRIGGER_STOCK_DELISTING)

    @Bean
    fun triggerNpcCreationTopic(): NewTopic = createTopic(KafkaTopics.TRIGGER_NPC_CREATION)

    @Bean
    fun triggerInstitutionCreationTopic(): NewTopic = createTopic(KafkaTopics.TRIGGER_INSTITUTION_CREATION)

    private fun createTopic(name: String, partitions: Int = 3, replicas: Int = 1): NewTopic {
        return TopicBuilder.name(name)
            .partitions(partitions)
            .replicas(replicas)
            .build()
    }
}
