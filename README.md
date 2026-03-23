# ⚙️ Simulator Service (Autonix Engine)

Autonix 스마트 팩토리의 **핵심 비즈니스 로직을 실행하는 생산 시뮬레이션 엔진**입니다.
`order-service`의 주문을 기점으로 가상의 차량을 생성하고, 전체 공정을 시뮬레이션하며 각 서비스와 유기적으로 통신합니다.

---

## 🚀 주요 기능

- **생산 시뮬레이션 제어:** `order-service`로부터 주문 수신 시 엔진 자동 가동 및 수량별 순차 생산.
- **실시간 공정 이동:** 6개 핵심 라인(차체A/B, 도장, 조립A/B, QC)을 설정된 주기(Tick)에 따라 이동.
- **장애 상황 시뮬레이션:** 특정 라인 강제 장애 발생(`FAULT`), 재고 강제 부족, 복구 기능을 통한 예외 상황 테스트.
- **이벤트 기반 연동:**
  - `production.started` (Kafka): 시뮬레이션 트리거
  - `inventory.deduct` (Kafka): 조립 단계 진입 시 부품 차감 요청
  - `line.fault` (Kafka): 장애 발생 시 실시간 모니터링 알림
- **배송 연동:** QC 완료 시 `shipping-service`에 Feign으로 배송 등록 요청.
- **이벤트 로그:** 시뮬레이터 주요 이벤트(이동, 에러, 완료 등) 최신 100건 내역 조회.
- **장애 탄력성(Resilience):** `Resilience4j` 서킷 브레이커를 적용하여 외부 서비스 장애 시 시스템 보호.

---

## 🛠 Tech Stack

| 항목 | 내용 |
|---|---|
| Framework | Spring Boot 3.4.11 |
| Cloud | Spring Cloud 2024.0.0 |
| Messaging | Apache Kafka (Port: 9092) |
| Storage | Redis (Port: 6379) |
| Resilience | Resilience4j (Circuit Breaker) |
| Monitoring | Spring Boot Actuator |
| Build Tool | Gradle |
| DevOps | Docker, GitHub Actions |

---

## 🛰 서비스 간 통신 구조

| 대상 서비스 | 방식 | 통신 규격 (Topic / API) | 목적 |
|:---|:---:|:---|:---|
| **Order Service** | Async (Kafka) | `production.started` (Sub) | 생산 시작 신호 및 주문 정보 수신 |
| **Line Service** | Sync (Feign) | `PATCH /v1/lines/vehicles/{vin}` | Tick마다 차량 공정 위치 업데이트 |
| **Line Service** | Sync (Feign) | `PATCH /v1/lines/{lineId}/status` | 장애 발생 시 라인 상태 FAULT 변경 |
| **Inventory Service** | Async (Kafka) | `inventory.deduct` (Pub) | 조립 단계(ASSEMBLY_A) 진입 시 부품 차감 요청 |
| **Inventory Service** | Sync (Feign) | `POST /v1/inventory/error/stock/{partId}` | 재고 부족 상황 강제 연출 (시연용) |
| **Shipping Service** | Sync (Feign) | `POST /v1/shippings/create` | QC 완료 시 배송 등록 요청 |
| **Gateway (BFF)** | Async (Kafka) | `line.fault` (Pub) | 라인 장애 발생 시 실시간 알림 전달 |

---

## 🔌 API Specification

**Base URL:** `http://localhost:8086`

### 1. 엔진 제어 및 모니터링

| Method | Path | 설명 |
|:---:|:---|:---|
| GET | `/v1/simulator/status` | 엔진 가동 여부 및 현재 설정 조회 |
| POST | `/v1/simulator/executions` | 시뮬레이션 즉시 가동 |
| POST | `/v1/simulator/executions/stop` | 시뮬레이션 중지 |
| PATCH | `/v1/simulator/config` | 가동 주기(Tick Rate) 등 설정 변경 |
| POST | `/v1/simulator/move/{vehicleId}` | 특정 차량 즉시 다음 공정으로 강제 이동 (시연용) |
| GET | `/v1/simulator/logs` | 시뮬레이터 이벤트 로그 최신 내역 조회 |

### 2. 장애 시뮬레이션 (시연용)

| Method | Path | 설명 |
|:---:|:---|:---|
| POST | `/v1/simulator/error/line/{lineId}` | 특정 라인 강제 FAULT 유도 |
| POST | `/v1/simulator/error/stock/{partId}` | 특정 부품 재고 강제 0으로 알림 트리거 |
| POST | `/v1/simulator/error/clear` | 모든 장애 상태 해제 및 초기화 |

---

## 📂 Project Structure

```text
com.autonix.simulator_service
├── client/         # OpenFeign 인터페이스 (LineClient, InventoryClient, ShippingClient)
├── config/         # Kafka, Feign, Resilience4j, Scheduler 설정
├── controller/     # 엔진 제어 및 상태 조회 API
├── domain/         # ProcessStep(Enum), SimulationStatus, VirtualVehicle 핵심 모델
├── dto/            # 요청/응답/이벤트 데이터 객체
├── exception/      # SimulationException, GlobalExceptionHandler
├── message/        # Kafka Consumer & Producer
└── service/        # 시뮬레이션 메인 로직 및 스케줄러
```

---

## 🏃 실행 및 테스트

### 1. 로컬 실행

```bash
# 1. IntelliJ Gradle 패널 → Tasks → build → bootJar 실행
# 또는 IDE 터미널에서:
./bin/gradle bootJar

# 2. 애플리케이션 기동
java -jar -Dspring.profiles.active=local build/libs/simulator-service-0.0.1-SNAPSHOT.jar
```

### 2. Docker 컨테이너 실행

```bash
# 1. JAR 먼저 빌드 (IntelliJ bootJar 실행)

# 2. 컨테이너 빌드 및 실행
docker-compose up -d

# 3. 로그 확인
docker logs -f simulator-service
```

### 3. 연동 테스트 (Kafka CLI)

주문 서비스 없이 직접 생산 이벤트를 발생시켜 테스트할 수 있습니다.

```bash
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic production.started

# 아래 데이터 입력
{"orderId": "ORD-001", "modelName": "AVANTE", "quantity": 3}
```

---

## ⚙️ 환경 설정 (Profiles)

| 항목 | local | docker |
|---|---|---|
| Kafka | `localhost:9092` | `kafka:29092` |
| Eureka | `http://localhost:8761/eureka/` | `http://eureka-server:8761/eureka/` |
| 실행 방법 | IDE 또는 `java -jar` | `docker-compose up -d` |

---

## 💡 참고 사항

- **Circuit Breaker:** `line-service` 응답 실패 시 10초간 회로가 개방(OPEN)되며 폴백 로직이 수행됩니다.
- **Tick Rate:** `PATCH /v1/simulator/config`로 변경된 tickRate는 상태 조회 응답에 반영됩니다. `@Scheduled` 런타임 동적 변경은 미구현 상태입니다.
- **장애 복구:** `POST /v1/simulator/error/clear` 호출 시 진행 중인 차량 전체가 초기화됩니다. 복구 후 재가동은 `/v1/simulator/executions`를 별도 호출해야 합니다.
- **Monitoring:** `http://localhost:8086/actuator/health`에서 서비스 및 서킷 상태 확인이 가능합니다.
- **연관 서비스 API 의존성:** `line-service`의 `PATCH /v1/lines/{lineId}/status`, `inventory-service`의 `POST /v1/inventory/error/stock/{partId}` API가 구현되어 있어야 정상 동작합니다.