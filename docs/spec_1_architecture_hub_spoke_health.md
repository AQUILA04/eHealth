# SPEC-1-Hub-and-Spoke National Health System Architecture

## Background

A national eHealth ecosystem is being designed according to a hub-and-spoke architecture. Each healthcare facility (spoke) contains its local operational systems—GAP, DPI, CPOE, LIS, RIS, Pharma—while a centralized Health Information Exchange (HIE) hub enables interoperability, master patient identity management (EMPI), consent management, secure routing of clinical transactions, and normalization of clinical data.

The initial architecture required consolidation, risk mitigation, and modernization. This specification defines the **optimal end-to-end architecture**, including data governance, terminology management, multi-tenancy, offline capability, workflow orchestration, and secure microservice communication.

## Requirements

### Must Have (M)

* M1: Centralized Health Information Exchange (HIE) supporting FHIR R5.
* M2: Master Patient Index (EMPI) with deterministic + probabilistic matching.
* M3: Standardized Terminology Service (SNOMED CT, LOINC, ICD-10).
* M3: Consent Management integrated into HIE.
* M4: Secure API Gateway + Service Mesh for zero-trust interservice communication.
* M5: Offline-first clinic nodes with full clinical autonomy (up to several days).
* M6: Clinical Data Repository (CDR) at the hub for normalized longitudinal data.
* M7: Workflow Orchestrator (BPMN/Temporal/Camunda) at each facility.
* M8: Multi-tenancy per facility with full isolation.
* M9: Event sourcing + audit trails across all transactions.

### Should Have (S)

* S1: Store-and-forward message queues for intermittent connectivity.
* S2: Operational dashboards and observability stack.
* S3: Data lake for analytics and AI/ML pipelines.

### Could Have (C)

* C1: Health Knowledge Graph for population analytics.
* C2: Predictive analytics triggers integrated into workflows.

### Won’t Have (W)

* W1: Full national EHR consolidation (hub does not replace facility DPI).

## Method

### 1. **High-Level Architecture**

```plantuml
@startuml
skinparam componentStyle rectangle

package "Hub (National)" {
  [API Gateway] --> [Service Mesh]
  [Service Mesh] --> [HIE Router]
  [Service Mesh] --> [EMPI]
  [Service Mesh] --> [Consent Service]
  [Service Mesh] --> [Terminology Server]
  [Service Mesh] --> [FHIR Normalization Engine]
  [Service Mesh] --> [Central CDR]
  [Service Mesh] --> [Message Broker]
}

package "Spoke Facility" {
  [Local API Gateway]
  [Local Service Mesh]
  [GAP]
  [DPI]
  [CPOE]
  [LIS]
  [RIS]
  [Pharmacy]
  [Care Workflow Engine]
  [Local Sync Cache]
}

[Local API Gateway] --> [Local Service Mesh]
[Local Service Mesh] --> [Care Workflow Engine]
[Local Service Mesh] --> [DPI]
[Local Service Mesh] --> [Local Sync Cache]

[Local Sync Cache] <--> [Message Broker]
[Message Broker] --> [HIE Router]

@enduml
```

```mermaid
graph TD
    subgraph "Hub (National)"
        AG["API Gateway"]
        SM["Service Mesh"]
        HR["HIE Router"]
        EMPI["EMPI"]
        CS["Consent Service"]
        TS["Terminology Server"]
        FNE["FHIR Normalization Engine"]
        CCDR["Central CDR"]
        MB["Message Broker"]
        
        AG --> SM
        SM --> HR
        SM --> EMPI
        SM --> CS
        SM --> TS
        SM --> FNE
        SM --> CCDR
        SM --> MB
    end

    subgraph "Spoke Facility"
        LAG["Local API Gateway"]
        LSM["Local Service Mesh"]
        GAP["GAP"]
        DPI["DPI"]
        CPOE["CPOE"]
        LIS["LIS"]
        RIS["RIS"]
        Pharmacy["Pharmacy"]
        CWE["Care Workflow Engine"]
        LSC["Local Sync Cache"]
        
        LAG --> LSM
        LSM --> CWE
        LSM --> DPI
        LSM --> LSC
    end

    LSC <--> MB
    MB --> HR
```

### 2. **Core Components**

#### **2.1 HIE Router (FHIR R5)**

* Routes messages using SMART-on-FHIR security.
* Manages facility-specific routing rules.
* Handles versioning and FHIR document reconciliation.

#### **2.2 EMPI**

* Deterministic matching: MRN, national ID, phone.
* Probabilistic matching: Jaro-Winkler, Fellegi-Sunter.
* Supports survivorship rules.

**Patient Identity Table (simplified):**

```plaintext
patient_identity(
  id UUID PK,
  mrn VARCHAR,
  national_id VARCHAR,
  phone VARCHAR,
  facility_id UUID,
  confidence_score FLOAT,
  master_patient_id UUID
)
```

#### **2.3 Terminology Service**

* Based on open-source Snowstorm (SNOMED CT).
* Supports LOINC + ICD-10 mapping.
* Exposed via FHIR Terminology API.

#### **2.4 Consent Management Service**

* Stores patient consent decisions.
* Enforced at query time in the HIE Router.

#### **2.5 Central Clinical Data Repository (CDR)**

* Stores **normalized FHIR resources**.
* Append-only event store to support versioning.

**FHIR Event Table:**

```plaintext
fhir_events(
  event_id UUID,
  resource_type VARCHAR,
  resource_id VARCHAR,
  version INT,
  payload JSONB,
  timestamp TIMESTAMP,
  facility_id VARCHAR
)
```

#### **2.6 Local Clinical Systems**

Each spoke contains:

* GAP
* DPI
* CPOE
* LIS, RIS
* Pharmacy
* **Workflow Engine (BPMN/Temporal)**

#### **2.7 Offline Mode and Synchronization**

* Incoming/outgoing traffic queued in **Local Sync Cache** using Kafka/Redpanda.
* Store-and-forward pattern for all FHIR messages.
* Conflict resolution rules:

  * Last Writer Wins for administrative data.
  * Versioned merges for clinical resources.

### 3. Workflows

#### **3.2 Care Workflow Post‑Admission (BPMN)**

```plantuml
@startuml
start
:Admission Completed;
:Initialize Care Plan;
partition "Nurse Station" {
  :Nursing Assessment;
  :Record Vitals;
}
partition "Physician" {
  :Review Assessment;
  :Order Labs/Imaging (CPOE);
}
partition "LIS/RIS" {
  :Perform Lab/Imaging;
  :Return Results;
}
partition "Care Coordination" {
  :Update Care Plan;
  :Trigger Alerts/Tasks;
}
partition "Pharmacy" {
  :Validate Medication Orders;
  :Prepare Medication;
}
partition "Ward" {
  :Administer Medication;
  :Monitor Patient;
}
:Daily Review & Adjust Plan;
stop
@enduml
```

```mermaid
flowchart TD
    Start["Start"] --> AdmissionCompleted["Admission Completed"]
    AdmissionCompleted --> InitializeCarePlan["Initialize Care Plan"]
    
    subgraph "Nurse Station"
        NursingAssessment["Nursing Assessment"]
        RecordVitals["Record Vitals"]
    end
    
    InitializeCarePlan --> NursingAssessment
    NursingAssessment --> RecordVitals
    
    subgraph "Physician"
        ReviewAssessment["Review Assessment"]
        OrderLabs["Order Labs/Imaging (CPOE)"]
    end
    
    RecordVitals --> ReviewAssessment
    ReviewAssessment --> OrderLabs
    
    subgraph "LIS/RIS"
        PerformLabs["Perform Lab/Imaging"]
        ReturnResults["Return Results"]
    end
    
    OrderLabs --> PerformLabs
    PerformLabs --> ReturnResults
    
    subgraph "Care Coordination"
        UpdateCarePlan["Update Care Plan"]
        TriggerAlerts["Trigger Alerts/Tasks"]
    end
    
    ReturnResults --> UpdateCarePlan
    UpdateCarePlan --> TriggerAlerts
    
    subgraph "Pharmacy"
        ValidateMedication["Validate Medication Orders"]
        PrepareMedication["Prepare Medication"]
    end
    
    TriggerAlerts --> ValidateMedication
    ValidateMedication --> PrepareMedication
    
    subgraph "Ward"
        AdministerMedication["Administer Medication"]
        MonitorPatient["Monitor Patient"]
    end
    
    PrepareMedication --> AdministerMedication
    AdministerMedication --> MonitorPatient
    
    MonitorPatient --> DailyReview["Daily Review & Adjust Plan"]
    DailyReview --> Stop["Stop"]
```

#### **3.1 Patient Admission (Sample BPMN)**

```plantuml
@startuml
start
:Search Patient in DPI;
:Query EMPI;
if (Match Found?) then (yes)
  :Retrieve CDR summary;
else (no)
  :Create local patient;
  :Sync to EMPI;
endif
:Trigger Care Workflow;
stop
@enduml
```

```mermaid
flowchart TD
    Start([Start]) --> SearchPatient[Search Patient in DPI]
    SearchPatient --> QueryEMPI[Query EMPI]
    QueryEMPI --> Decision{Match Found?}
    
    Decision -- Yes --> RetrieveSummary[Retrieve CDR summary]
    
    Decision -- No --> CreatePatient[Create local patient]
    CreatePatient --> SyncToEMPI[Sync to EMPI]
    
    RetrieveSummary --> TriggerWorkflow[Trigger Care Workflow]
    SyncToEMPI --> TriggerWorkflow
    
    TriggerWorkflow --> Stop([Stop])
```

### 4. **Security and Networking**

* API Gateway for request enforcement.
* Service Mesh (Istio) for mTLS and policy enforcement.
* OAuth2 / OIDC with Keycloak.
* Event-level audit logs.

### 5. **Facility Segmentation Model**

* Each facility operates its **own independent infrastructure** (on-premise or cloud) and is not a logical tenant within a shared platform.
* The hub maintains **facility-aware segmentation**, not multi-tenancy:

  * FHIR store is segmented using `facility_id` namespaces.
  * CDR events are partitioned per facility.
  * Message broker topics are facility-specific.
  * Authentication/authorization realms are separated per facility.
* Provides strict **data isolation**, **routing control**, **audit boundaries**, and **governance** for each facility.

### 6. **Data Flow Overview**

1. Clinical action at facility → local system
2. Local Event → Local Sync Cache
3. Forward to Hub → HIE Router
4. Normalize → Terminology → CDR
5. EMPI reconciliation
6. Consent enforcement on access

## Implementation

### Step-by-step

1. **Foundation Layer**

   * Deploy Kubernetes-based infrastructure.
   * Install Istio service mesh.
   * Deploy API gateway (Kong/NGINX APIM).

2. **Hub Services**

   * Deploy EMPI
   * Deploy Terminology Server
   * Deploy HIE Router
   * Deploy Consent Service
   * Deploy Central CDR / FHIR store
   * Deploy Message Broker

3. **Spoke Nodes**

   * Deploy Local API Gateway
   * Deploy Local Service Mesh
   * Deploy Sync Cache (Redpanda/Kafka)
   * Integrate local DPI/GAP modules
   * Deploy Workflow Engine (Camunda/Temporal)

4. **Data Integration**

   * Configure facility-specific adapters
   * Map HL7v2 → FHIR
   * Implement normalization rules

5. **Security Setup**

   * OAuth2 realms per facility
   * mTLS configuration
   * Zero-trust policies

## Milestones

1. **M1 – Infrastructure Base** (Month 1–2)
2. **M2 – Hub Core Services Operational** (Month 3–4)
3. **M3 – First Facility Pilot** (Month 5)
4. **M4 – Multi-Facility Rollout** (Month 6–9)
5. **M5 – Analytics + Data Lake** (Month 10–12)

## Gathering Results

* Evaluate latency of HIE routing.
* Confirm EMPI match accuracy (>95% deterministic, >85% probabilistic).
* Validate full clinical autonomy offline.
* Confirm compliance with privacy regulations.
* Conduct interoperability certification tests.

