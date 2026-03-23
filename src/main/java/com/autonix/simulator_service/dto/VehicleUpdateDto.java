package com.autonix.simulator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUpdateDto {
    private Long vehicleId;
    private String nextStep; // 예: "PAINT_SHOP", "ASSEMBLY_LINE"
    private String status;   // 예: "NORMAL", "FAULT"
}
