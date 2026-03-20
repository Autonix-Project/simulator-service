package com.autonix.simulator_service.service;

import java.util.List;
import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.autonix.simulator_service.client.InventoryClient;
import com.autonix.simulator_service.client.LineClient;
import com.autonix.simulator_service.domain.ProcessStep;
import com.autonix.simulator_service.domain.SimulationStatus;
import com.autonix.simulator_service.dto.OrderStartedEvent;
import com.autonix.simulator_service.dto.VehicleResponseDto;
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
    private final SimulationProducer producer; // Kafka 발행용

    @Scheduled(fixedDelayString = "#{@simulationStatus.tickRate}")
    public void runSimulationTick() {
        if (!status.isRunning()) return;

        log.info("--- Simulation Tick: 차량 이동 시작 ---");

        // 1. Line Service로부터 현재 공정 중인 차량 목록 조회 (Sync Feign)
        List<VehicleResponseDto> activeVehicles = lineClient.getActiveVehicles();

        for (VehicleResponseDto vehicle : activeVehicles) {
            ProcessStep currentStep = ProcessStep.valueOf(vehicle.getCurrentStep());
            ProcessStep nextStep = currentStep.next();

            if (nextStep != null) {
                // 2. 차량 공정 이동 요청 (Sync Feign)
                lineClient.updateVehicleStep(vehicle.getId(), nextStep.name());

                // 3. 조립 공정 진입 시 재고 차감 (Async Kafka 이벤트 발행)
                if (nextStep == ProcessStep.ASSEMBLY_LINE) {
                    inventoryClient.deductInventory(vehicle.getOrderId());
                }

                // 4. 품질 검사 완료 시 출고 등록 (Async Kafka 이벤트 발행)
                if (nextStep == ProcessStep.SHIPPING_READY) {
                    // 난수로 QC 합격 여부 결정 (시뮬레이션 로직)
                    boolean isPassed = new Random().nextInt(10) > 1; // 80% 합격
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
        log.info("시뮬레이션 엔진: 주문 생산 시작 처리 - orderId: {}, 수량: {}", 
                event.getOrderId(), event.getQuantity());
        
        // 1. 시뮬레이터 가동 상태를 TRUE로 변경
        status.setRunning(true);
        
        // 2. 추가 로직: 필요하다면 여기서 lineClient를 통해 초기 차량 생성을 요청할 수도 있습니다.
        // 현재 구조에서는 status만 true로 바꿔주면 @Scheduled 루프가 돌면서 로직을 수행합니다.
    }

    // SimulationService.java 클래스 내부에 추가

    public void startSimulation() {
        log.info("시뮬레이션 가동을 시작합니다.");
        status.setRunning(true);
    }

    public void stopSimulation() {
        log.info("시뮬레이션 가동을 중지합니다.");
        status.setRunning(false);
    }
}