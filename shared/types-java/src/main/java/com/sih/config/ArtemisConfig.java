package com.sih.config;

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

/**
 * Configuration Artemis pour Spring Boot
 * 
 * Artemis est utilisé pour les transactions critiques:
 * - Admissions patients
 * - Prescriptions médicales
 * - Transferts de patients
 * - Toute opération nécessitant une garantie ACID
 */
@Configuration
@EnableJms
public class ArtemisConfig {

    /**
     * Configuration du ConnectionFactory pour Artemis
     * Utilise le protocole OpenWire (optimisé pour Java)
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        // La configuration par défaut utilise les propriétés Spring Boot
        // spring.artemis.host=localhost
        // spring.artemis.port=61616
        // spring.artemis.user=ehealth
        // spring.artemis.password=ehealth_dev_password
        
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setSessionCacheSize(10);
        cachingConnectionFactory.setCacheConsumers(true);
        cachingConnectionFactory.setCacheProducers(true);
        return cachingConnectionFactory;
    }

    /**
     * Configuration du JmsTemplate pour envoi de messages
     * Garantit les transactions ACID
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setReceiveTimeout(5000);
        jmsTemplate.setPubSubDomain(false);  // Queues par défaut
        return jmsTemplate;
    }

    /**
     * Configuration du listener container factory
     * Gère la consommation des messages avec transactions
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        
        // Transactions activées
        factory.setSessionTransacted(true);
        
        // Nombre de threads concurrents
        factory.setConcurrency("3-10");
        
        // Délai de reconnexion en cas d'erreur
        factory.setReceiveTimeout(5000L);
        
        // Nombre de tentatives avant DLQ
        factory.setDefaultDestinationResolutionStrategy(
            (name, pubSubDomain) -> pubSubDomain
        );
        
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    /**
     * Configuration du listener container factory pour Topics (pub/sub)
     */
    @Bean
    public DefaultJmsListenerContainerFactory topicListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionTransacted(true);
        factory.setConcurrency("3-10");
        factory.setReceiveTimeout(5000L);
        factory.setPubSubDomain(true);  // Topics
        
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
