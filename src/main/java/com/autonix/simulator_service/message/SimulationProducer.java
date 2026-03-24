package com.autonix.simulator_service.message;

import java.util.HashMap;
import java.util.List;
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
    public void sendShippingReadyEvent(String vin, String orderId) {
        kafkaTemplate.send("shipping.ready", Map.of("vin", vin, "orderId", orderId));
    }

    /**
     * 조립 단계 진입 → 재고 차감 이벤트
     * Key: vehicleId / Payload: { vehicleId, station, parts[] }
     */
    public void sendInventoryDeductEvent(Integer vehicleId, String station, List<Map<String, Integer>> parts) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("vehicleId", vehicleId);
        payload.put("station", station);
        payload.put("parts", parts);
        kafkaTemplate.send("inventory.deduct", String.valueOf(vehicleId), payload);
    }

    /**
     * 장애 발생 → 알림 이벤트
     */
    public void sendFaultEvent(String type, String targetId) {
        kafkaTemplate.send("line.fault", Map.of("type", type, "targetId", targetId));
    }
}