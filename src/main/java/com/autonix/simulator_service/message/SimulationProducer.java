package com.autonix.simulator_service.message;

import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SimulationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 생산 완료 → 출고 이벤트
     * vin은 "orderId-1" 형식의 String
     */
    public void sendShippingReadyEvent(String vin) {
        kafkaTemplate.send("shipping.ready", Map.of("vin", vin));
    }

    /**
     * 조립 단계(ASSEMBLY_A) 진입 → 재고 차감 이벤트
     * orderId는 String
     */
    public void sendInventoryDeductEvent(String orderId) {
        kafkaTemplate.send("inventory.deduct", Map.of("orderId", orderId, "action", "DECREASE"));
    }

    /**
     * 장애 발생 → 알림 이벤트
     */
    public void sendFaultEvent(String type, String targetId) {
        kafkaTemplate.send("line.fault", Map.of("type", type, "targetId", targetId));
    }
}