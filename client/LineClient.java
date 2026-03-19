package com.autonix.simulator_service.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "line-service") // 라인 서비스 호출
public interface LineClient {
    // 차량 위치 업데이트 요청 (수업시간 배운 PathVariable 방식)
    @PatchMapping("/v1/lines/vehicles/{v_id}")
    void updateVehicleStatus(@PathVariable("v_id") Long vehicleId, @RequestBody Map<String, Object> status);
}