package com.autonix.simulator_service.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.autonix.simulator_service.client.InventoryClient;
import com.autonix.simulator_service.client.LineClient;
import com.autonix.simulator_service.domain.ProcessStep;
import com.autonix.simulator_service.domain.SimulationStatus;
import com.autonix.simulator_service.domain.VirtualVehicle;
import com.autonix.simulator_service.dto.OrderStartedEvent;
import com.autonix.simulator_service.dto.SimulationConfigDto;
import com.autonix.simulator_service.dto.SimulationResponseDto;
import com.autonix.simulator_service.dto.VehicleResponseDto;
import com.autonix.simulator_service.dto.VehicleUpdateDto;
import com.autonix.simulator_service.message.SimulationProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final LineClient lineClient;
    private final InventoryClient inventoryClient;
    private final SimulationStatus status;
    private final SimulationProducer producer;

    private final List<VirtualVehicle> activeVehicles = new CopyOnWriteArrayList<>();

    // 시뮬레이터 주요 이벤트 로그 (최신 100건 유지)
    private final List<String> eventLogs = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LOG_SIZE = 100;

    // -------------------------------------------------------------------------
    // 주문 처리
    // -------------------------------------------------------------------------

    /**
     * Kafka로 수신한 주문 시작 이벤트 — 차량 투입
     * line-service에서 활성 차량 목록을 조회한 뒤 orderId 기준으로 vehicleId/lineId 매핑
     */
    public void startOrderProduction(OrderStartedEvent event) {
        List<VehicleResponseDto> orderVehicles = fetchLineVehiclesByOrderId(event.getOrderId());

        for (int i = 1; i <= event.getQuantity(); i++) {
            String vin = event.getOrderId() + "-" + i;

            VirtualVehicle.VirtualVehicleBuilder builder = VirtualVehicle.builder()
                .orderId(event.getOrderId())
                .vin(vin)
                .modelName(event.getModelName())
                .currentStep(ProcessStep.BODY_A)
                .remainingTime(ProcessStep.BODY_A.getDuration())
                .vehicleId(-1)
                .lineId(-1);

            // 인덱스 순서로 line-service 차량과 1:1 매핑
            if (i - 1 < orderVehicles.size()) {
                VehicleResponseDto matched = orderVehicles.get(i - 1);
                builder.vehicleId(matched.getVehicleId())
                       .lineId(matched.getCurrentLineId());
                addLog("[투입] " + vin + " → vehicleId=" + matched.getVehicleId()
                    + ", lineId=" + matched.getCurrentLineId()
                    + " (모델: " + event.getModelName() + ")");
            } else {
                addLog("[투입] " + vin + " — vehicleId 매핑 실패, line-service 업데이트 생략 예정 (모델: " + event.getModelName() + ")");
            }

            activeVehicles.add(builder.build());
        }
    }

    /**
     * line-service에서 활성 차량 전체 조회 후 특정 orderId에 해당하는 차량만 필터링
     * Feign 호출 실패 시 빈 리스트 반환 (시뮬레이션 자체는 계속 동작)
     */
    private List<VehicleResponseDto> fetchLineVehiclesByOrderId(String orderId) {
        try {
            int orderIdInt = Integer.parseInt(orderId);
            return lineClient.getActiveVehicles().stream()
                .filter(v -> v.getOrderId() == orderIdInt)
                .toList();
        } catch (NumberFormatException e) {
            log.warn("[투입] orderId가 정수형이 아닙니다 — vehicleId 매핑 생략: {}", orderId);
        } catch (Exception e) {
            log.warn("[투입] line-service 차량 조회 실패 — vehicleId 매핑 생략: {}", e.getMessage());
        }
        return List.of();
    }

    // -------------------------------------------------------------------------
    // 시뮬레이션 엔진 Tick
    // -------------------------------------------------------------------------

    /**
     * 1초마다 실행 — 각 차량의 남은 공정 시간 감소 및 다음 단계 이동
     */
    @Scheduled(fixedRate = 1000)
    public void processTick() {
        if (!status.isRunning() || activeVehicles.isEmpty()) return;

        for (VirtualVehicle vehicle : activeVehicles) {
            vehicle.setRemainingTime(vehicle.getRemainingTime() - 1);
            if (vehicle.getRemainingTime() <= 0) {
                moveToNextStep(vehicle);
            }
        }
    }

    /**
     * 다음 공정으로 이동
     * - 마지막 공정(QC) 완료 시: ShippingClient Feign 호출로 배송 등록 (동기)
     * - ASSEMBLY_A 진입 시: 재고 차감 이벤트 발행 (Kafka 비동기)
     * - 모든 공정 전환 시: LineClient Feign으로 동기 업데이트
     */
    private void moveToNextStep(VirtualVehicle vehicle) {
        ProcessStep nextStep = vehicle.getCurrentStep().getNext();

        if (nextStep == null) {
            // QC 완료 → 배송 서비스에 Feign으로 배송 등록 (통신 명세: 동기)
            log.info("[엔진] 차량 생산 완료: {}", vehicle.getVin());
            producer.sendShippingReadyEvent(vehicle.getVin(), vehicle.getOrderId());
            addLog("[완료] " + vehicle.getVin() + " → shipping.ready 이벤트 발행");
            activeVehicles.remove(vehicle);
        } else {
            vehicle.setCurrentStep(nextStep);
            vehicle.setRemainingTime(nextStep.getDuration());

            // Line-Service 동기 업데이트
            // vehicleId가 확인된 차량만 line-service에 전달 (Kafka 이벤트로 투입된 초기 차량은 -1)
            if (vehicle.getVehicleId() > 0) {
                lineClient.updateVehicleStep(
                    vehicle.getLineId(),
                    VehicleUpdateDto.builder()
                        .vehicleId(vehicle.getVehicleId())
                        .currentProcess(nextStep.toLineType())
                        .build()
                );
            } else {
                log.warn("[엔진] vehicleId 미확인 차량 — line-service 업데이트 생략: {}", vehicle.getVin());
            }

            // ASSEMBLY_A 진입 시 재고 차감 (Kafka 비동기)
            if (nextStep == ProcessStep.ASSEMBLY_A) {
                producer.sendInventoryDeductEvent(vehicle.getOrderId());
                addLog("[재고차감] orderId=" + vehicle.getOrderId());
            }

            addLog("[이동] " + vehicle.getVin() + " → " + nextStep.name());
        }
    }

    // -------------------------------------------------------------------------
    // 시뮬레이션 제어
    // -------------------------------------------------------------------------

    public void startSimulation() {
        log.info("시뮬레이션 가동을 시작합니다.");
        status.setRunning(true);
        addLog("[제어] 시뮬레이션 시작");
    }

    public void stopSimulation() {
        log.info("시뮬레이션 가동을 중지합니다.");
        status.setRunning(false);
        addLog("[제어] 시뮬레이션 중지");
    }

    /**
     * Tick Rate 등 설정 변경
     */
    public void updateConfig(SimulationConfigDto configDto) {
        status.setTickRate(configDto.getTickRate());
        log.info("시뮬레이터 설정 변경 — tickRate: {}ms", configDto.getTickRate());
        addLog("[설정] tickRate 변경 → " + configDto.getTickRate() + "ms");
    }

    /**
     * 장애 복구 — activeVehicles 초기화 + 정지
     */
    public void clearAllErrors() {
        int count = activeVehicles.size();
        activeVehicles.clear();
        status.setRunning(false);
        log.info("장애 복구 완료 — 차량 {}대 초기화, 시뮬레이터 정지", count);
        addLog("[복구] 전체 장애 해제, 차량 " + count + "대 초기화");
    }

    // -------------------------------------------------------------------------
    // 명세서 누락 API 구현
    // -------------------------------------------------------------------------

    /**
     * 특정 차량을 즉시 다음 공정으로 강제 이동 (테스트/시연용)
     * POST /simulator/move/{vehicleId}
     * vehicleId = vin (String)
     */
    public void forceMoveVehicle(String vin) {
        VirtualVehicle target = activeVehicles.stream()
            .filter(v -> v.getVin().equals(vin))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당 차량을 찾을 수 없습니다: " + vin));

        addLog("[강제이동] " + vin + " 즉시 다음 공정으로 이동");
        moveToNextStep(target);
    }

    /**
     * 특정 부품 재고를 강제로 0으로 만들어 재고 부족 알림 트리거 (시연용)
     * POST /simulator/error/stock/{partId}
     */
    public void triggerStockError(String partId) {
        log.info("[시연] 부품 {} 재고 강제 0 처리", partId);
        inventoryClient.forceStockEmpty(partId);
        addLog("[재고장애] partId=" + partId + " 강제 재고 0 처리");
    }

    /**
     * 라인 장애 강제 발생
     * POST /simulator/error/line/{lineId}
     * 1. 시뮬레이션 중지
     * 2. Line-Service에 Feign으로 FAULT 상태 변경 (명세: 동기)
     * 3. Kafka fault 이벤트 발행 (명세: 비동기)
     */
    public void triggerLineFault(String lineId) {
        stopSimulation();
        lineClient.updateLineStatus(Integer.parseInt(lineId), Map.of("status", "FAULT"));
        producer.sendFaultEvent("LINE_FAULT", lineId);
        log.info("[장애] 라인 {} FAULT 처리", lineId);
        addLog("[장애] 라인 " + lineId + " FAULT 강제 발생");
    }

    /**
     * 시뮬레이터 이벤트 로그 조회 (최신순)
     * GET /simulator/logs
     */
    public List<String> getLogs() {
        List<String> result = new ArrayList<>(eventLogs);
        Collections.reverse(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // 상태 조회 / 유틸
    // -------------------------------------------------------------------------

    public SimulationResponseDto getCurrentStatus() {
        String message = status.isRunning() ? "시뮬레이터가 가동 중입니다." : "시뮬레이터가 정지 상태입니다.";
        return SimulationResponseDto.builder()
                .isRunning(status.isRunning())
                .tickRate(status.getTickRate())
                .statusMessage(message)
                .build();
    }

    private void addLog(String message) {
        if (eventLogs.size() >= MAX_LOG_SIZE) {
            eventLogs.remove(0);
        }
        eventLogs.add(message);
        log.info(message);
    }
}