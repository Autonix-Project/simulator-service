package com.autonix.simulator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDto {
    private Long id;
    private Long orderId;
    private String currentStep; // 예: "BODY_SHOP"
}