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

    // 반환 타입을 VehicleResponseDto 리스트로 수정하여 Type Mismatch 해결
    @GetMapping("/v1/lines/vehicles/active")
    List<VehicleResponseDto> getActiveVehicles();

    // 두 번째 인자를 String(nextStep)으로 받거나, DTO를 새로 생성해서 보내도록 수정
    @PatchMapping("/v1/lines/vehicles/{v_id}")
    void updateVehicleStep(@PathVariable("v_id") Long vehicleId, @RequestBody String nextStep);
}