---

# ⚙️ Simulator Service (Autonix Engine)

Autonix 스마트 팩토리의 **핵심 비즈니스 로직을 실행하는 생산 시뮬레이션 엔진**입니다. 주문 정보를 바탕으로 가상의 차량을 생성하고, 정의된 공정 순서에 따라 차량을 이동시키며 재고 차감 및 출고 요청을 트리거합니다.

---

## 🚀 주요 기능

* **생산 시뮬레이션 제어:** 주문(`order_id`) 기반의 생산 시작, 중지 및 속도 조절.
* **실시간 공정 이동:** 설정된 주기에 따라 차량을 스테이션별(차체 → 도장 → 조립 → 검사)로 자동 이동.
* **장애 상황 시뮬레이션:** 시연 및 테스트를 위한 특정 라인 강제 장애 발생(`FAULT`) 및 복구 기능.
* **이벤트 기반 연동:** 공정 이동 시 재고 차감 요청(Kafka/Feign) 및 완료 시 배송 등록 요청(Kafka).

---

## 🛠 Tech Stack

* **Framework:** Spring Boot 3.x
* **Communication:** * **OpenFeign:** `line-service`, `inventory-service` (동기 상태 업데이트)
    * **Apache Kafka:** `order-service` (이벤트 수신), `shipping/notification` (이벤트 발행)
* **Registry:** Eureka Discovery Client
* **Build Tool:** Gradle

---

## 🛰 서비스 간 통신 구조

시뮬레이터는 시스템의 '동작'을 담당하므로 타 서비스와 밀접하게 연결되어 있습니다.

| 대상 서비스 | 방식 | 이벤트 시점 | 목적 |
| :--- | :---: | :--- | :--- |
| **Order Service** | **Async** | 주문 확정 시 | 생산 시작 신호(`production.started`) 수신 |
| **Line Service** | **Sync** | 공정 이동 시 | 차량 위치 및 스테이션 상태 정보 갱신 요청 |
| **Inventory Service** | **Async** | 조립 단계 진입 시 | 부품 차감 이벤트 발행 및 재고 부족 알림 연동 |
| **Shipping Service** | **Sync** | 품질검사 합격 시 | 최종 생산 완료된 차량의 출고 대기 등록 |
| **Gateway (BFF)** | **Async** | 상태 변화 발생 시 | SSE를 통한 클라이언트 실시간 모니터링 데이터 전달 |

---

## 🔌 API Specification

시뮬레이터는 UI가 없는 엔진으로, 아래 API를 통해 제어합니다.

### 1. 시뮬레이션 제어
* `POST /v1/simulator/start`: 전체 시뮬레이션 가동 시작
* `POST /v1/simulator/stop`: 모든 작업 일시 정지
* `PATCH /v1/simulator/config`: 이동 속도(Tick) 등 환경 설정 변경

### 2. 장애 시뮬레이션 (시연용)
* `POST /v1/simulator/error/line/{id}`: 특정 라인 강제 장애 발생
* `POST /v1/simulator/error/clear`: 모든 장애 복구 및 가동 재개

---

## 📂 Project Structure

```text
com.autonix.simulator_service
├── client/      # 타 서비스 호출을 위한 OpenFeign 인터페이스
├── config/      # Kafka, Feign, Scheduler 설정
├── controller/  # 엔진 제어용 REST API
├── domain/      # 공정 단계(Enum), 엔진 상태 모델
├── dto/         # 통신용 데이터 전송 객체
├── message/     # Kafka Consumer(수신) & Producer(발행)
└── service/     # 핵심 시뮬레이션 엔진 및 스케줄러 로직
```

---

## 🏃 실행 방법

1.  **Kafka 및 Eureka Server**가 기동되어 있어야 합니다.
2.  `application.yml`의 환경 설정(포트 `8084`)을 확인합니다.
3.  `./gradlew bootRun` 명령어로 서비스를 시작합니다.
4.  **Postman**을 이용해 `/start` 엔드포인트를 호출하여 시뮬레이션을 가동합니다.

---

**규빈님, 이 ReadMe를 올리면 팀원들이 전체 흐름을 이해하는 데 큰 도움이 될 거예요. 이제 이 문서를 깃허브에 같이 푸시해볼까요?**
