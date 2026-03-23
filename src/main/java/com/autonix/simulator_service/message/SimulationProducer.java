package com.autonix.simulator_service.message;

import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SimulationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendShippingReadyEvent(Long vehicleId) {
        kafkaTemplate.send("shipping.ready", Map.of("vehicleId", vehicleId));
    }
    
    // 조립 단계 진입 시 재고 서비스에 알림
    public void sendInventoryDeductEvent(Long orderId) {
        kafkaTemplate.send("inventory.deduct", Map.of("orderId", orderId, "action", "DECREASE"));
    }

    // 장애 발생 시 알림 서비스에 전송
    public void sendFaultEvent(String type, Object targetId) {
        kafkaTemplate.send("fault.occurred", Map.of("type", type, "targetId", targetId));
    }
}