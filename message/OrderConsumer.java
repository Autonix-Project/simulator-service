package com.autonix.simulator_service.message;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.autonix.simulator_service.dto.OrderStartedEvent;
import com.autonix.simulator_service.service.SimulationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final SimulationService simulationService;

    @KafkaListener(topics = "production.started", groupId = "simulator-group")
    public void consumeOrder(OrderStartedEvent event) {
        log.info("주문 수신: orderId={}, 모델={}", event.getOrderId(), event.getModelName());
        // 주문 정보를 바탕으로 시뮬레이션 엔진 가동
        simulationService.startOrderProduction(event);
    }
}
