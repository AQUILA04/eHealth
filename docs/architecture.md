
-----

# **Architecture Document: Système d'Information Hospitalier (SIH) Intégré**

## **1. Introduction**

Ce document présente l'architecture technique détaillée du Système d'Information Hospitalier (SIH) Intégré. Il sert de référence pour le développement, le déploiement et l'évolution de la plateforme.

Le système est conçu pour répondre à une double exigence :

1.  **Gestion Hospitalière Locale (Spoke) :** Assurer l'autonomie opérationnelle des établissements (GAP, DPI, Facturation, Plateaux techniques).
2.  **Interopérabilité & Identité (Hub) :** Garantir une identification unique du patient via une couche d'abstraction (compatible MOSIP/Local) et permettre l'échange sécurisé de données cliniques entre établissements.

L'architecture suit une approche **Microservices** stricte, facilitant la maintenance, l'évolutivité et le déploiement indépendant des modules critiques.

### **Change Log**

| Date | Version | Description | Auteur |
| :--- | :--- | :--- | :--- |
| 2025-05-20 | 1.0.0 | Initialisation de l'architecture (Hub & Spoke, EMPI Hybride) | Winston (Architecte) |

-----

## **2. High Level Architecture**

### **Technical Summary**

Le système adopte une architecture distribuée de type **Hub-and-Spoke**.

  * **Le Hub Central** gère l'identité (EMPI), le registre des consentements et le routage des messages d'interopérabilité.
  * **Les Nœuds Locaux (Hôpitaux)** hébergent les microservices métiers (GAP, DPI, Labo, etc.) et fonctionnent de manière autonome, synchronisant les données critiques via un bus d'événements asynchrone.
  * La communication inter-services utilise **gRPC** pour la performance interne et **REST/FHIR** pour l'exposition externe.

### **Architectural Patterns**

  * **Microservices Architecture :** Découplage fort des domaines fonctionnels (Admin, Clinique, Technique).
  * **Event-Driven Architecture (EDA) :** Utilisation d'un bus de messages (RabbitMQ/Kafka) pour la cohérence à terme et la réactivité (ex: Admission -\> Notification DPI -\> Notification Facturation).
  * **Adapter Pattern (EMPI) :** Abstraction de la source d'identité (MOSIP vs Local) pour isoler la logique métier de la méthode d'identification.
  * **API Gateway Pattern :** Point d'entrée unique pour sécuriser et router les requêtes clients (Web/Mobile) vers les microservices appropriés.

### **High Level Diagram**

```mermaid
graph TD
    subgraph "HUB CENTRAL (Interopérabilité & Identité)"
        API_Hub[API Gateway Central]
        EMPI_Svc[Service EMPI]
        HIE_Router[Routeur Interopérabilité]
        Consent_Svc[Gestion Consentement]
        
        EMPI_DB[(EMPI DB)]
        Audit_DB[(Audit Log)]
        
        EMPI_Svc --> EMPI_DB
        EMPI_Svc -->|Adapter| Ext_MOSIP[Système MOSIP]
        EMPI_Svc -->|Adapter| Ext_Local[Système ID Local]
    end

    subgraph "NOEUD LOCAL (Clinique/Hôpital)"
        API_Local[API Gateway Local]
        Bus{Event Bus (RabbitMQ)}
        
        subgraph "Services Administratifs"
            GAP_Svc[GAP / ADT]
            Billing_Svc[Facturation]
        end
        
        subgraph "Services Cliniques"
            EMR_Svc[DPI Core]
            CPOE_Svc[Prescription]
        end
        
        subgraph "Plateaux Techniques"
            LIS_Svc[Laboratoire]
            RIS_Svc[Radiologie]
        end
        
        GAP_Svc <--> Bus
        EMR_Svc <--> Bus
        Billing_Svc <--> Bus
        LIS_Svc <--> Bus
        
        GAP_DB[(GAP DB)]
        EMR_DB[(DPI NoSQL)]
        
        GAP_Svc --> GAP_DB
        EMR_Svc --> EMR_DB
    end

    API_Local --> GAP_Svc
    API_Local --> EMR_Svc
    
    %% Communication Hub-Spoke
    GAP_Svc -- "gRPC / Secu Tunnel" --> API_Hub
    HIE_Router -.-> API_Local
```

-----

## **3. Tech Stack**

Sélection technologique privilégiant la robustesse, la typage strict et la performance pour les données de santé.

### **Cloud & Infrastructure**

  * **Conteneurisation :** Docker & Kubernetes (K8s) pour l'orchestration.
  * **Gateway :** Kong ou Traefik.
  * **Message Broker :** RabbitMQ (pour la gestion locale) ou Kafka (si très haut débit requis).

### **Technology Stack Table**

| Category | Technology | Version | Purpose | Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Language** | TypeScript | 5.x | Backend & Frontend | Typage strict essentiel pour la sécurité des données cliniques. |
| **Runtime** | Node.js | 20.x (LTS) | Backend Runtime | Performance I/O non-bloquante, vaste écosystème. |
| **Framework BE** | NestJS | 10.x | Microservices | Architecture modulaire, support natif gRPC/Microservices, Injection de dépendance. |
| **Framework FE** | React | 18.x | Frontend SPA | Composants réutilisables, écosystème riche. |
| **Database (Relational)** | PostgreSQL | 16.x | Admin / Facturation / GAP | Robustesse transactionnelle (ACID), support JSONB. |
| **Database (Doc)** | MongoDB | 7.x | DPI (Dossier Clinique) | Flexibilité des schémas pour les formulaires médicaux évolutifs. |
| **Communication** | gRPC / Protobuf | 3.x | Inter-services | Performance, contrats d'interface stricts. |
| **Interop Standard** | HL7 FHIR | R4/R5 | Échange de données | Standard mondial pour l'interopérabilité santé. |

-----

## **4. Components**

Décomposition des services principaux basée sur les modules fonctionnels identifiés.

### **4.1 Service EMPI (Hub)**

  * **Responsabilité :** Gestion unique de l'identité patient.
  * **Pattern :** Utilise un *Provider Adapter* pour basculer dynamiquement entre MOSIP et le système local.
  * **Interfaces :**
      * `verifyIdentity(biometrics/id)`
      * `linkPatient(localId, globalId)`
  * **Stack :** NestJS, PostgreSQL (Index), Redis (Cache).

### **4.2 Service GAP (Local)**

  * **Responsabilité :** Gestion Administrative, Admissions, Mouvements (ADT). Source de vérité pour la localisation du patient.
  * **Dépendances :** Appelle le Service EMPI pour valider l'identité à l'admission. Publie les événements `PatientAdmitted`, `PatientTransfered`.
  * **Stack :** NestJS, PostgreSQL.

### **4.3 Service DPI / EMR (Local)**

  * **Responsabilité :** Gestion du dossier clinique, observations, notes.
  * **Architecture Données :** Utilise MongoDB pour stocker des documents cliniques complexes et variables (formulaires spécialisés par service).
  * **Interfaces :** API FHIR Facade pour l'interopérabilité externe.

### **4.4 Service Interopérabilité (Hub)**

  * **Responsabilité :** Orchestration des échanges entre cliniques.
  * **Fonction :** Reçoit une demande de "Résumé Patient" du Centre B, vérifie le consentement, interroge le Centre A, transforme et transmet la donnée.

-----

## **5. Data Models (Conceptual)**

### **Patient Identity (EMPI View)**

```typescript
interface GlobalPatientIdentity {
  globalUuid: string; // ID Unique Système
  sourceSystem: 'MOSIP' | 'LOCAL_V1';
  sourceId: string; // ID dans le système source (ex: UIN MOSIP)
  demographics: {
    firstName: string;
    lastName: string;
    dob: Date;
    biometricHash?: string; // Hash seulement, pas la donnée brute
  };
  links: LinkedLocalRecord[]; // Liens vers les dossiers locaux (Hôpital A, B...)
}
```

### **Dossier Clinique (EMR Document)**

```typescript
interface ClinicalEncounter {
  encounterId: string;
  patientId: string; // Référence Local ID
  type: 'OUTPATIENT' | 'EMERGENCY' | 'INPATIENT';
  status: 'IN-PROGRESS' | 'FINISHED';
  observations: Array<{
    code: string; // LOINC / SNOMED
    value: any;
    performer: string;
    timestamp: Date;
  }>;
}
```

-----

## **6. API Design & Interoperability**

### **Internal Communication (Microservices)**

  * Utilisation de **gRPC** pour les appels synchrones critiques (ex: GAP demande validation EMPI).
  * Fichiers `.proto` partagés via un package npm privé `@sih/proto`.

### **External Communication (HIE)**

  * Adoption stricte du standard **FHIR (Fast Healthcare Interoperability Resources)**.
  * Le Hub expose une API REST conforme FHIR pour les échanges inter-hospitaliers.
  * Exemple : `GET /fhir/Patient/{id}/$summary`.

-----

## **7. Infrastructure & Deployment**

### **Deployment Strategy**

  * Chaque service possède son propre `Dockerfile`.
  * Un `docker-compose.yml` racine permet de lancer l'environnement complet pour le développement.
  * **CI/CD :** Pipeline GitHub Actions construisant les images et les déployant sur un cluster Kubernetes (Hub) ou des serveurs Docker (Locaux).

### **Security**

  * **mTLS :** Chiffrement obligatoire entre tous les microservices (via Service Mesh type Istio ou configuration native).
  * **OIDC :** Authentification des utilisateurs (Médecins/Admin) via Keycloak.
  * **Audit :** Tout accès au DPI génère une trace immuable (Qui, Quoi, Quand) envoyée au service d'Audit.

-----

## **8. Coding Standards (Critical Rules)**

1.  **Strict Boundaries :** Aucun service n'accède directement à la base de données d'un autre service.
2.  **No Clear Text PII :** Les données identifiantes (PII) doivent être chiffrées au repos.
3.  **Error Handling :** Ne jamais exposer de stack traces ou de détails techniques dans les réponses API externes.
4.  **Types First :** Tout développement commence par la définition des interfaces (TypeScript) ou des contrats (Protobuf).

-----
