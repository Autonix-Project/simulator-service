package com.autonix.simulator_service.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimulationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 생산 완료 → 출고 이벤트
     * vin은 "orderId-1" 형식의 String
     */
    public void sendShippingReadyEvent(String vin, String orderId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("vin", vin);
            payload.put("orderId", Integer.parseInt(orderId));
            kafkaTemplate.send("shipping.ready", payload);
        } catch (NumberFormatException e) {
            log.warn("[shipping.ready] orderId가 정수형이 아니어서 발행 생략: {}", orderId);
        }
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