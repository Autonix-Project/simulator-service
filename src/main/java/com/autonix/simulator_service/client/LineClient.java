package com.autonix.simulator_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.autonix.simulator_service.dto.VehicleResponseDto;

@FeignClient(name = "line-service")
public interface LineClient {

    // 가동 중인 전체 차량 조회 (시뮬레이터 시작 시 최초 1회)
    @GetMapping("/v1/lines/vehicles/active")
    List<VehicleResponseDto> getActiveVehicles();

    // Tick마다 차량 공정 위치 업데이트
    // PATCH /v1/lines/{lineId}/vehicles — 명세서 기준 경로
    // vin을 PathVariable로, nextStep을 Body로 전달
    @PatchMapping("/v1/lines/vehicles/{vin}")
    void updateVehicleStep(@PathVariable("vin") String vin, @RequestBody String nextStep);

    // 장애 강제 발생 시 라인 상태를 FAULT로 변경
    // PATCH /v1/lines/{lineId}/status — 명세서 기준
    @PatchMapping("/v1/lines/{lineId}/status")
    void updateLineStatus(@PathVariable("lineId") String lineId, @RequestBody String status);
}