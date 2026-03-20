---

# ⚙️ Simulator Service (Autonix Engine)

Autonix 스마트 팩토리의 **핵심 비즈니스 로직을 실행하는 생산 시뮬레이션 엔진**입니다. 주문 정보를 바탕으로 가상의 차량을 생성하고, 정의된 공정 순서에 따라 차량을 이동시키며 재고 차감 및 출고 요청을 트리거합니다.

---

## 🚀 주요 기능

* **생산 시뮬레이션 제어:** 주문(`order_id`) 기반의 생산 시작, 중지 및 속도 조절.
* **실시간 공정 이동:** 설정된 주기에 따라 차량을 스테이션별(차체 → 도장 → 조립 → 검사)로 자동 이동.
* **장애 상황 시뮬레이션:** 시연 및 테스트를 위한 특정 라인 강제 장애 발생(`FAULT`) 및 복구 기능.
* **이벤트 기반 연동:** 공정 이동 시 재고 차감 요청(Kafka/Feign) 및 완료 시 배송 등록 요청(Kafka).
* **장애 탄력성(Resilience):** Resilience4j 서킷 브레이커를 적용하여 외부 서비스 장애 시에도 엔진 안정성 유지.
* **상태 모니터링:** 실시간 엔진 가동 여부 및 설정 정보 조회 API 제공.

---

## 🛠 Tech Stack

* **Framework:** Spring Boot 3.4.11
* **Cloud:** Spring Cloud 2024.0.0 (Hoxton equivalent for Boot 3.4)
* **Communication:** * **OpenFeign:** `line-service`, `inventory-service` 연동 (Circuit Breaker 적용)
    * **Apache Kafka:** `order-service` (이벤트 수신), `inventory/fault` (이벤트 발행)
* **Resilience:** Resilience4j (Circuit Breaker)
* **Monitoring:** Spring Boot Actuator
* **Registry:** Eureka Discovery Client
* **Build Tool:** Gradle
* **DevOps:** Docker, Docker Compose, GitHub Actions

---

## 🛰 서비스 간 통신 구조

| 대상 서비스 | 방식 | 이벤트 시점 | 목적 |
| :--- | :---: | :--- | :--- |
| **Order Service** | **Async** | 주문 확정 시 | 생산 시작 신호(`production.started`) 수신 |
| **Line Service** | **Sync** | 공정 이동 시 | 차량 위치 및 스테이션 상태 정보 갱신 (Circuit Breaker 보호) |
| **Inventory Service** | **Async/Sync** | 조립 단계 진입 시 | 부품 차감 요청 및 재고 부족 알림 연동 |
| **Monitoring/BFF** | **Async** | 상태 변화 발생 시 | 장애 발생(`fault.occurred`) 알림 및 실시간 데이터 전달 |

---

## 🔌 API Specification

### 1. 시뮬레이션 제어 및 모니터링
* `GET /v1/simulator/status`: 엔진 현재 가동 상태 및 설정 정보 조회 **(NEW)**
* `POST /v1/simulator/executions`: 전체 시뮬레이션 가동 시작
* `POST /v1/simulator/stop`: 모든 작업 일시 정지
* `PATCH /v1/simulator/config`: 이동 속도(Tick) 등 환경 설정 변경

### 2. 장애 시뮬레이션 (시연용)
* `POST /v1/simulator/error/line/{id}`: 특정 라인 강제 장애 발생
* `POST /v1/simulator/error/clear`: 모든 장애 복구 및 가동 재개

---

## 📂 Project Structure (Standard Layout)

```text
com.autonix.simulator_service
├── client/         # 외부 서비스 호출 (FeignClient & Fallback)
├── config/         # Kafka, Feign, Resilience4j 설정
├── controller/     # 엔진 제어 및 상태 조회 REST API
├── domain/         # 공정 단계(Enum), 엔진 상태(Status) 도메인 모델
├── dto/            # 통신용 데이터 전송 객체 (Request/Response/Event)
├── message/        # Kafka Consumer(주문수신) & Producer(장애알림)
└── service/        # 핵심 시뮬레이션 엔진 로직 및 스케줄러
```

---

## 🏃 실행 및 배포 방법

### 로컬 인프라 구동 (Docker)
Kafka, Zookeeper, Redis를 한 번에 실행합니다.
```bash
docker-compose up -d
```

### 애플리케이션 실행
```bash
./gradlew clean bootJar
java -jar build/libs/simulator-service-0.0.1-SNAPSHOT.jar
```

### CI/CD 파이프라인
* **GitHub Actions:** `main` 브랜치 푸시 시 자동으로 빌드 및 AWS ECR 이미지 업로드 수행.
* **Environment Variables:** EKS 배포 시 `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_DATA_REDIS_HOST` 등을 설정해야 합니다.

---

### 💡 도움말
* **Circuit Breaker 상태 확인:** `/actuator/health` 엔드포인트를 통해 `lineService` 서킷 상태를 모니터링할 수 있습니다.
* **로그 확인:** 시뮬레이션의 상세 진행 상황은 애플리케이션 콘솔 로그를 통해 `[Simulation Engine]` 태그로 확인할 수 있습니다.

---