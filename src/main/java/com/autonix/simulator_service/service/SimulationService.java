package com.autonix.simulator_service.service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.autonix.simulator_service.client.InventoryClient;
import com.autonix.simulator_service.client.LineClient;
import com.autonix.simulator_service.domain.ProcessStep;
import com.autonix.simulator_service.domain.SimulationStatus;
import com.autonix.simulator_service.domain.VirtualVehicle;
import com.autonix.simulator_service.dto.OrderStartedEvent;
import com.autonix.simulator_service.dto.SimulationResponseDto;
import com.autonix.simulator_service.dto.VehicleResponseDto;
import com.autonix.simulator_service.message.SimulationProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @CircuitBreaker(name = "lineService", fallbackMethod = "fallbackGetVehicles")
    public List<VehicleResponseDto> getActiveVehiclesFromLine() {
        return lineClient.getActiveVehicles();
    }

    public List<VehicleResponseDto> fallbackGetVehicles(Throwable t) {
        log.error("Line Service 호출 실패! 서킷 브레이커 작동 또는 에러 발생: {}", t.getMessage());
        return Collections.emptyList();
    }

    @Scheduled(fixedDelayString = "#{@simulationStatus.tickRate}")
    public void runSimulationTick() {
        if (!status.isRunning()) return;

        log.info("--- Simulation Tick: 차량 이동 시작 ---");

        // lineClient.getActiveVehicles() 대신 서킷브레이커 적용된 메서드 호출
        List<VehicleResponseDto> activeVehicles = getActiveVehiclesFromLine();

        for (VehicleResponseDto vehicle : activeVehicles) {
            ProcessStep currentStep = ProcessStep.valueOf(vehicle.getCurrentStep());
            ProcessStep nextStep = currentStep.getNext();

            if (nextStep != null) {
                lineClient.updateVehicleStep(vehicle.getId(), nextStep.name());

                if (nextStep == ProcessStep.ASSEMBLY_LINE) {
                    inventoryClient.deductInventory(vehicle.getOrderId());
                }

                if (nextStep == ProcessStep.QUALITY_CONTROL) {
                    boolean isPassed = new Random().nextInt(10) > 1;
                    if (isPassed) {
                        producer.sendShippingReadyEvent(vehicle.getId());
                    } else {
                        producer.sendFaultEvent("QC_FAIL", vehicle.getId());
                    }
                }
            }
        }
    }

    public void startOrderProduction(OrderStartedEvent event) {
        for (int i = 1; i <= event.getQuantity(); i++) {
            String vin = event.getOrderId() + "-" + i;
            // 초기 단계인 BODY_SHOP으로 생성
            activeVehicles.add(new VirtualVehicle(event.getOrderId(), vin, event.getModelName(), ProcessStep.BODY_SHOP, ProcessStep.BODY_SHOP.getDuration()));
            log.info("[엔진] 차량 투입: {} (모델: {})", vin, event.getModelName());
        }
    }

    @Scheduled(fixedRate = 1000)
    public void processTick() {
        if (activeVehicles.isEmpty()) return;

        for (VirtualVehicle vehicle : activeVehicles) {
            vehicle.setRemainingTime(vehicle.getRemainingTime() - 1);

            if (vehicle.getRemainingTime() <= 0) {
                moveToNextStep(vehicle);
            }
        }
    }

    private void moveToNextStep(VirtualVehicle vehicle) {
        ProcessStep nextStep = vehicle.getCurrentStep().getNext();

        if (nextStep == null) {
            // 최종 공정 완료 -> 출고 서비스로 알림 (Kafka)
            log.info("[엔진] 차량 생산 완료: {}", vehicle.getVin());
            activeVehicles.remove(vehicle);
            kafkaTemplate.send("shipping.requested", vehicle.getVin());
            log.info("[Kafka] 출고 요청 발송: {}", vehicle.getVin());
        } else {
            // 다음 공정으로 이동
            vehicle.setCurrentStep(nextStep);
            vehicle.setRemainingTime(nextStep.getDuration());
            
            log.info("[엔진] 차량 이동: {} -> {}", vehicle.getVin(), nextStep.getName());

            // [동기 연동] Line-Service에 위치 업데이트 요청 (Feign)
            // updateVehicleLocation(vehicle.getVin(), nextStep.name());

            // [비동기 연동] 조립 단계 진입 시 재고 차감 요청 (Kafka)
            if (nextStep == ProcessStep.ASSEMBLY_LINE) {
                sendInventoryDeductEvent(vehicle);
            }
        }
    }

    private void sendInventoryDeductEvent(VirtualVehicle vehicle) {
        // msa-config에 정의된 토픽명 사용
        kafkaTemplate.send("inventory.deduct", vehicle.getVin()); 
        log.info("[Kafka] 재고 차감 요청 발송: {}", vehicle.getVin());
    }

    public void startSimulation() {
        log.info("시뮬레이션 가동을 시작합니다.");
        status.setRunning(true);
    }

    public void stopSimulation() {
        log.info("시뮬레이션 가동을 중지합니다.");
        status.setRunning(false);
    }

    public SimulationResponseDto getCurrentStatus() {
        String message = status.isRunning() ? "시뮬레이터가 가동 중입니다." : "시뮬레이터가 정지 상태입니다.";
        
        return SimulationResponseDto.builder()
                .isRunning(status.isRunning())
                .tickRate(status.getTickRate())
                .statusMessage(message)
                .build();
    }
}