package com.sih.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka pour Spring Boot
 * 
 * Kafka est utilisé pour:
 * - Audit trail immuable (30 jours de rétention)
 * - Replay d'événements
 * - Analytics et BI
 * - Conformité réglementaire (HIPAA-like)
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:ehealth-consumer-group}")
    private String groupId;

    /**
     * Configuration du Kafka Admin pour créer les topics automatiquement
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Création des topics Kafka pour audit trail
     */
    @Bean
    public NewTopic auditPatientEvents() {
        return TopicBuilder.name("ehealth.audit.patient-events")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(30 * 24 * 60 * 60 * 1000L))  // 30 jours
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic auditClinicalEvents() {
        return TopicBuilder.name("ehealth.audit.clinical-events")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(30 * 24 * 60 * 60 * 1000L))
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic auditAdministrativeEvents() {
        return TopicBuilder.name("ehealth.audit.administrative-events")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(30 * 24 * 60 * 60 * 1000L))
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic analyticsEvents() {
        return TopicBuilder.name("ehealth.analytics.events")
                .partitions(5)
                .replicas(1)
                .config("retention.ms", String.valueOf(90 * 24 * 60 * 60 * 1000L))  // 90 jours
                .config("compression.type", "snappy")
                .build();
    }

    /**
     * Configuration du Producer Factory pour Kafka
     * Garantit la sérialisation JSON des événements
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Garantie de livraison
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");  // Tous les replicas doivent acquitter
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);  // Ordre garanti
        
        // Compression
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Batching
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka Template pour envoyer des messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Configuration du Consumer Factory pour Kafka
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Offset reset
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // Replay depuis le début
        
        // Désactiver l'auto-commit pour contrôler les offsets
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        
        // Session timeout
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        
        // Heartbeat
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        // JsonDeserializer config
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.sih.event.DomainEvent");
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sih.*");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Listener Container Factory pour Kafka
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Nombre de threads concurrents
        factory.setConcurrency(3);
        
        // Ack mode: MANUAL pour contrôler les offsets
        factory.getContainerProperties().setAckMode(
            org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );
        
        return factory;
    }
}
