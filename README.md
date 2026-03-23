---

# ⚙️ Simulator Service (Autonix Engine)

Autonix 스마트 팩토리의 **핵심 비즈니스 로직을 실행하는 생산 시뮬레이션 엔진**입니다. 
`order-service`의 주문을 기점으로 가상의 차량을 생성하고, 전체 공정을 시뮬레이션하며 각 서비스와 유기적으로 통신합니다.

---

## 🚀 주요 기능

* **생산 시뮬레이션 제어:** `order-service`로부터 주문 수신 시 엔진 자동 가동 및 수량별 순차 생산.
* **실시간 공정 이동:** 6개 핵심 라인(차체A/B, 도장, 조립A/B, QC)을 설정된 주기(Tick)에 따라 이동.
* **장애 상황 시뮬레이션:** 특정 라인 강제 장애 발생(`FAULT`) 및 복구 기능을 통한 예외 상황 테스트.
* **이벤트 기반 연동:** - `production.started` (Kafka): 시뮬레이션 트리거
  - `inventory.deduct` (Kafka): 조립 단계 진입 시 부품 차감 요청
  - `line.fault` (Kafka): 장애 발생 시 실시간 모니터링 알림
* **장애 탄력성(Resilience):** `Resilience4j` 서킷 브레이커를 적용하여 `line-service` 등 외부 장애 시 시스템 보호.

---

## 🛠 Tech Stack

* **Framework:** Spring Boot 3.4.11
* **Cloud:** Spring Cloud 2024.0.0
* **Messaging:** Apache Kafka (Port: 9092)
* **Storage:** Redis (Port: 6379)
* **Resilience:** Resilience4j (Circuit Breaker)
* **Monitoring:** Spring Boot Actuator
* **Build Tool:** Gradle
* **DevOps:** Docker, GitHub Actions

---

## 🛰 서비스 간 통신 구조

| 대상 서비스 | 방식 | 통신 규격 (Topic / API) | 목적 |
| :--- | :---: | :--- | :--- |
| **Order Service** | **Async** | `production.started` (Sub) | 생산 시작 신호 및 주문 정보 수신 |
| **Line Service** | **Sync** | `GET /v1/lines/vehicles/active` | 실시간 차량 위치 및 상태 동기화 |
| **Inventory Service** | **Async** | `inventory.deduct` (Pub) | 조립 단계 진입 시 부품 차감 요청 |
| **Gateway (BFF)** | **Async** | `line.fault` (Pub) | 라인 장애 발생 시 실시간 데이터 전달 |

---

## 🔌 API Specification

**Base URL:** `http://localhost:8086`

### 1. 엔진 제어 및 모니터링
* `GET /v1/simulator/status`: 엔진 가동 여부 및 현재 설정 조회
* `POST /v1/simulator/executions`: 시뮬레이션 즉시 가동
* `POST /v1/simulator/stop`: 시뮬레이션 일시 정지
* `PATCH /v1/simulator/config`: 가동 주기(Tick Rate) 등 설정 변경

### 2. 장애 시뮬레이션 (테스트용)
* `POST /v1/simulator/error/line/{id}`: 특정 라인 강제 장애 유도
* `POST /v1/simulator/error/clear`: 모든 장애 상태 해제

---

## 📂 Project Structure

```text
com.autonix.simulator_service
├── client/         # OpenFeign 인터페이스 및 Fallback(Circuit Breaker)
├── config/         # Kafka, Feign, Resilience4j, Scheduler 설정
├── controller/     # 엔진 제어 및 상태 조회 API
├── domain/         # ProcessStep(Enum), SimulationStatus 등 핵심 모델
├── dto/            # 요청/응답/이벤트 데이터 객체
├── message/        # Kafka Consumer & Producer
└── service/        # 시뮬레이션 메인 로직 및 스케줄러
```

---

## 🏃 실행 및 테스트

### 1. 인프라 가동 (Docker)
```bash
# msa-config 폴더에서 실행
docker-compose up -d
```

### 2. 애플리케이션 기동
```bash
./gradlew clean bootJar
java -jar -Dspring.profiles.active=local build/libs/simulator-service-0.0.1-SNAPSHOT.jar
```

### 3. 연동 테스트 (Kafka CLI)
주문 서비스 없이 직접 생산 이벤트를 발생시켜 테스트할 수 있습니다.
```bash
docker exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic production.started
# 아래 데이터 입력
{"orderId": "ORD-001", "modelName": "AVANTE", "quantity": 3}
```

---

### 💡 참고 사항
* **Circuit Breaker:** `line-service` 응답 실패 시 10초간 회로가 개방(OPEN)되며 폴백 로직이 수행됩니다.
* **Monitoring:** `http://localhost:8086/actuator/health`에서 서비스 및 서킷 상태 확인이 가능합니다.

---