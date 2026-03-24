package com.autonix.simulator_service.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.autonix.simulator_service.dto.VehicleResponseDto;
import com.autonix.simulator_service.dto.VehicleUpdateDto;

@FeignClient(name = "line-service")
public interface LineClient {

    // 가동 중인 전체 차량 조회
    // GET /lines/vehicles/active
    @GetMapping("/lines/vehicles/active")
    List<VehicleResponseDto> getActiveVehicles();

    // Tick마다 차량 공정 위치 업데이트
    // PATCH /lines/{lineId}/vehicles
    @PatchMapping("/lines/{lineId}/vehicles")
    void updateVehicleStep(@PathVariable("lineId") int lineId, @RequestBody VehicleUpdateDto dto);

    // 장애 강제 발생 시 라인 상태를 FAULT로 변경
    // PATCH /lines/{lineId}/status
    @PatchMapping("/lines/{lineId}/status")
    void updateLineStatus(@PathVariable("lineId") int lineId, @RequestBody Map<String, String> body);
}
