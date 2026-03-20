# ⚙️ Simulator Service (Autonix Engine)

Autonix 스마트 팩토리의 **핵심 비즈니스 로직을 실행하는 생산 시뮬레이션 엔진**입니다. 
주문 정보를 바탕으로 가상의 차량을 생성하고, 공정 순서에 따라 차량을 이동시키며 재고 차감 및 장애 이벤트를 관리합니다.

---

## 🚀 주요 기능

* **생산 시뮬레이션 제어:** `order-service`로부터 주문 수신 시 엔진 자동 가동.
* **실시간 공정 이동:** 6개 라인(차체A/B, 도장, 조립A/B, QC)을 가상 차량이 순차적으로 통과.
* **이벤트 기반 연동:** - `production.started` 수신 시 시뮬레이션 시작.
  - `inventory.deduct` 발행을 통한 실시간 부품 재고 차감 요청.
* **장애 상황 시뮬레이션:** 테스트를 위한 라인 장애 발생(`line.fault`) 및 복구.
* **장애 탄력성(Resilience):** Resilience4j 서킷 브레이커 적용으로 외부 서비스 장애 대응.

---

## 🛠 Tech Stack

* **Framework:** Spring Boot 3.4.11
* **Cloud:** Spring Cloud 2024.0.0
* **Messaging:** Apache Kafka (Bootstrap: localhost:9092)
* **Resilience:** Resilience4j (Circuit Breaker)
* **Build Tool:** Gradle
* **Infra:** Docker Compose (MySQL 8.0, Kafka, Zookeeper)

---

## 🛰 서비스 간 통신 구조 (Updated)

| 대상 서비스 | 방식 | 토픽 / API | 목적 |
| :--- | :---: | :--- | :--- |
| **Order Service** | **Async** | `production.started` (Sub) | 생산 시작 신호 및 주문 정보 수신 |
| **Line Service** | **Sync** | `/v1/lines/vehicles/active` | 차량 위치 및 라인 상태 동기화 |
| **Inventory Service** | **Async** | `inventory.deduct` (Pub) | 조립 단계 진입 시 부품 차감 요청 |
| **Gateway (BFF)** | **Async** | `line.fault` (Pub) | 라인 장애 발생 시 실시간 알림 |

---

## 📂 데이터베이스 구성 (Infrastructure)
* **External Port:** 3307 (Local Access)
* **Internal Port:** 3306 (Docker Network)
* **Databases:**
  - `line_db`: 공정 및 차량 마스터 데이터
  - `order_db`: 주문 및 회원 데이터
  - `inventory_db`: 부품 및 재고 내역
  - `shipping_db`: 출고 내역

---

## 🏃 실행 방법 (Local)

1. **공통 인프라 가동 (msa-config 폴더)**
   ```bash
   docker-compose up -d