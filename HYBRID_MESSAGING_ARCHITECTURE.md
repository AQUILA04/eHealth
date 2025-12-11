# Architecture Hybride de Messaging: Artemis + Kafka

**Date:** 11 Décembre 2025  
**Version:** 1.0  
**Status:** ✅ Implémentée

---

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Artemis: Transactions Critiques](#artemis-transactions-critiques)
- [Kafka: Audit Trail & Replay](#kafka-audit-trail--replay)
- [Patterns d'implémentation](#patterns-dimplémentation)
- [Exemples de code](#exemples-de-code)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Vue d'ensemble

L'architecture hybride combine **Artemis** et **Kafka** pour offrir une solution de messaging robuste et conforme aux normes réglementaires:

| Aspect | Artemis | Kafka | Cas d'usage |
|--------|---------|-------|-----------|
| **Rôle** | Transactions critiques | Audit trail immuable | Complémentaires |
| **Garantie** | ACID (Atomicité) | Immuabilité (Append-only) | Sécurité + Conformité |
| **Latence** | Ultra-basse (1-5ms) | Basse (5-20ms) | Temps réel vs Historique |
| **Rétention** | Courte (heures) | Longue (30+ jours) | Opérationnel vs Audit |
| **Replay** | Non natif | Natif (offset management) | Récupération vs Analyse |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Application eHealth                           │
│                  (Services Java/Spring Boot)                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │  EventPublisher      │
                    │  (Service Central)   │
                    └──────────────────────┘
                         ↙        ↘
        ┌──────────────────────┐  ┌──────────────────────┐
        │     Artemis (JMS)    │  │   Kafka (Topics)     │
        │  Transactions ACID   │  │  Audit Trail Immuable│
        └──────────────────────┘  └──────────────────────┘
              ↓                          ↓
        ┌──────────────────┐      ┌──────────────────┐
        │ Event Listeners  │      │ Event Listeners  │
        │ (Artemis)        │      │ (Kafka)          │
        └──────────────────┘      └──────────────────┘
              ↓                          ↓
        ┌──────────────────┐      ┌──────────────────┐
        │ Services         │      │ Audit/Analytics  │
        │ (DPI, GAP, etc)  │      │ (Elasticsearch)  │
        └──────────────────┘      └──────────────────┘
```

---

## 🔴 Artemis: Transactions Critiques

### Quand utiliser Artemis?

Artemis est utilisé pour les opérations **critiques** qui nécessitent une garantie ACID:

- ✅ **Admissions patients** - Doit être atomique
- ✅ **Prescriptions médicales** - Aucune perte de données
- ✅ **Transferts de patients** - Ordre garanti
- ✅ **Opérations financières** - Transactions ACID
- ✅ **Mises à jour critiques** - Cohérence garantie

### Configuration Artemis

**Docker Compose:**
```yaml
artemis:
  image: quay.io/artemiscloud/activemq-artemis:latest
  ports:
    - "61616:61616"  # OpenWire (Java)
    - "5672:5672"    # AMQP
    - "8161:8161"    # Management UI
  environment:
    ARTEMIS_USER: ehealth
    ARTEMIS_PASSWORD: ehealth_dev_password
```

**Accès Management UI:** http://localhost:8161

### Queues Artemis

```
patient.admitted          → Admission de patients
patient.transferred       → Transfert de patients
patient.discharged        → Sortie de patients
prescription.created      → Création de prescriptions
lab.result.ready          → Résultats de laboratoire
image.result.ready        → Résultats d'imagerie
events.default            → Événements par défaut
```

### Caractéristiques

- **Transactions:** ACID complètes
- **Persistance:** Journal transactionnel
- **Failover:** Automatique (Master/Slave)
- **Latence:** 1-5ms
- **Throughput:** 100k+ msg/s
- **Ordre:** Garanti par queue

---

## 🟢 Kafka: Audit Trail & Replay

### Quand utiliser Kafka?

Kafka est utilisé pour l'**audit trail immuable** et le **replay d'événements**:

- ✅ **Audit trail** - Trace immuable de tous les événements
- ✅ **Replay d'événements** - Rejouer l'historique complet
- ✅ **Analytics** - Données historiques pour BI
- ✅ **Conformité** - HIPAA-like, trace complète
- ✅ **Multi-tenancy** - Partitionnement par tenant
- ✅ **Event Sourcing** - Source de vérité pour l'état

### Configuration Kafka

**Docker Compose:**
```yaml
kafka:
  image: confluentinc/cp-kafka:7.5.0
  ports:
    - "9092:9092"
  environment:
    KAFKA_LOG_RETENTION_HOURS: 720  # 30 jours
    KAFKA_COMPRESSION_TYPE: snappy
```

**Accès Kafka UI:** http://localhost:8080

### Topics Kafka

```
ehealth.audit.patient-events          → Audit des événements patients
ehealth.audit.clinical-events         → Audit des événements cliniques
ehealth.audit.administrative-events   → Audit des événements administratifs
ehealth.analytics.events              → Événements pour analytics
```

### Caractéristiques

- **Rétention:** 30 jours (configurable)
- **Partitions:** 3 pour scalabilité
- **Réplication:** 1 (développement), 3+ (production)
- **Compression:** Snappy
- **Offset Management:** Manuel (MANUAL ACK)
- **Replay:** Depuis n'importe quel offset

---

## 📐 Patterns d'implémentation

### Pattern 1: Publication d'événement critique

```java
@Service
@RequiredArgsConstructor
public class PatientService {
    private final EventPublisher eventPublisher;
    
    public void admitPatient(PatientAdmissionRequest request) {
        // Logique métier
        Patient patient = createPatient(request);
        
        // Créer l'événement
        PatientAdmittedEvent event = PatientAdmittedEvent.builder()
            .patientId(patient.getId())
            .firstName(patient.getFirstName())
            .lastName(patient.getLastName())
            .admissionDate(LocalDateTime.now())
            .departmentId(request.getDepartmentId())
            .admittingPhysicianId(request.getPhysicianId())
            .source("GAP")
            .hospitalId(request.getHospitalId())
            .actorId(getCurrentUserId())
            .actorRole("ADMIN")
            .build();
        
        // Publier sur Artemis (ACID) + Kafka (Audit)
        eventPublisher.publishCriticalEvent(event);
    }
}
```

### Pattern 2: Écoute d'événement Artemis

```java
@Service
public class DPIEventHandler extends ArtemisEventListener {
    
    @Override
    protected void processPatientAdmitted(PatientAdmittedEvent event) {
        log.info("DPI: Traitement de l'admission du patient {}", event.getPatientId());
        
        // Créer le dossier patient dans DPI
        PatientRecord record = new PatientRecord();
        record.setPatientId(event.getPatientId());
        record.setFirstName(event.getFirstName());
        record.setLastName(event.getLastName());
        
        patientRecordRepository.save(record);
        
        log.info("✅ Dossier patient créé dans DPI");
    }
}
```

### Pattern 3: Écoute d'événement Kafka (Audit)

```java
@Service
public class AuditService extends KafkaEventListener {
    
    @Override
    protected void handlePatientEventAudit(DomainEvent event, String topic) {
        log.info("Audit: Enregistrement de l'événement {} pour le patient {}", 
            event.getEventType(), 
            ((PatientAdmittedEvent) event).getPatientId());
        
        // Enregistrer dans Elasticsearch pour recherche
        AuditLog auditLog = new AuditLog();
        auditLog.setEventId(event.getEventId());
        auditLog.setEventType(event.getEventType());
        auditLog.setActorId(event.getActorId());
        auditLog.setTimestamp(event.getTimestamp());
        auditLog.setEventData(event);
        
        auditLogRepository.save(auditLog);
    }
}
```

### Pattern 4: Replay d'événements

```java
@Service
@RequiredArgsConstructor
public class EventReplayService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Rejoue tous les événements d'un patient depuis une date
     */
    public void replayPatientEvents(String patientId, LocalDateTime fromDate) {
        // Récupérer les événements depuis Kafka (offset management)
        // Kafka permet de chercher par timestamp
        
        log.info("🔄 Replay des événements du patient {} depuis {}", patientId, fromDate);
        
        // Les événements sont rejouables grâce à Kafka
        // Offset reset: earliest
    }
}
```

---

## 💻 Exemples de code

### Configuration Spring Boot

**application.yml:**
```yaml
spring:
  profiles:
    active: artemis,kafka
  
  artemis:
    host: localhost
    port: 61616
    user: ehealth
    password: ehealth_dev_password
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ehealth-consumer-group
      auto-offset-reset: earliest
```

### Injection des services

```java
@Service
@RequiredArgsConstructor
public class PatientEventService {
    private final EventPublisher eventPublisher;
    
    public void handlePatientAdmission(PatientAdmissionRequest request) {
        // Publier l'événement
        PatientAdmittedEvent event = new PatientAdmittedEvent(
            request.getPatientId(),
            request.getFirstName(),
            request.getLastName()
        );
        
        eventPublisher.publishCriticalEvent(event);
    }
}
```

---

## 📊 Monitoring

### Artemis Management UI

**URL:** http://localhost:8161

**Fonctionnalités:**
- Voir les queues et topics
- Monitorer les messages
- Voir les consumers
- Gérer les connexions

### Kafka UI

**URL:** http://localhost:8080

**Fonctionnalités:**
- Voir les topics et partitions
- Monitorer les messages
- Voir les consumer groups
- Analyser les offsets

### Prometheus Metrics

```
# Artemis
artemis_queue_size
artemis_messages_received
artemis_messages_sent

# Kafka
kafka_consumer_lag
kafka_producer_record_send_total
kafka_consumer_records_consumed_total
```

### Grafana Dashboards

- **Artemis Dashboard** - Queues, throughput, latency
- **Kafka Dashboard** - Topics, partitions, consumer lag
- **Event Flow Dashboard** - Événements end-to-end

---

## 🔧 Troubleshooting

### Artemis ne démarre pas

```bash
# Vérifier les logs
docker logs ehealth-artemis

# Vérifier le port
netstat -an | grep 61616

# Redémarrer
docker restart ehealth-artemis
```

### Kafka consumer lag élevé

```bash
# Vérifier les offsets
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group ehealth-consumer-group --describe

# Réinitialiser les offsets
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group ehealth-consumer-group --reset-offsets --to-earliest --execute
```

### Messages perdus

**Vérifier:**
1. Artemis: Vérifier la persistance du journal
2. Kafka: Vérifier les réplicas et la rétention
3. Logs: Chercher les erreurs de publication

### Performance dégradée

**Optimisations:**
1. Augmenter les threads concurrents
2. Augmenter la taille des batches
3. Ajouter des partitions Kafka
4. Monitorer avec Prometheus/Grafana

---

## 🚀 Déploiement

### Développement

```bash
# Démarrer l'infrastructure
docker-compose -f infrastructure/docker/docker-compose.dev.yml up -d

# Vérifier
docker ps | grep -E "artemis|kafka"
```

### Production

**Considérations:**
1. **Artemis:** Cluster Master/Slave (2-3 nœuds)
2. **Kafka:** Cluster 3-5 brokers avec réplication 3
3. **Monitoring:** Prometheus + Grafana
4. **Logging:** ELK Stack
5. **Backup:** Snapshots réguliers
6. **Sécurité:** SSL/TLS, authentification

---

## 📚 Ressources

- [Artemis Documentation](https://activemq.apache.org/artemis/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Boot Artemis](https://spring.io/guides/gs/messaging-jms/)
- [Spring Boot Kafka](https://spring.io/guides/gs/spring-kafka/)

---

## ✅ Checklist d'implémentation

- [x] Docker Compose avec Artemis + Kafka
- [x] Configuration Spring Boot (ArtemisConfig, KafkaConfig)
- [x] Modèles d'événements (DomainEvent, PatientEvents, ClinicalEvents)
- [x] EventPublisher (Artemis + Kafka)
- [x] ArtemisEventListener (base pour services)
- [x] KafkaEventListener (base pour audit)
- [x] Documentation architecture
- [ ] Tests unitaires
- [ ] Tests d'intégration
- [ ] Monitoring Prometheus
- [ ] Grafana dashboards
- [ ] Déploiement production

---

**Architecture hybride Artemis + Kafka implémentée avec succès! 🎉**

*Dernière mise à jour: 11 Décembre 2025*
