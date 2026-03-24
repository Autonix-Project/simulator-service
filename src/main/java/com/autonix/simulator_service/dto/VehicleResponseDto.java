package com.autonix.simulator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * line-service GET /lines/vehicles/active 응답 DTO
 * line-service VehicleResponseDTO 필드와 일치
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDto {
    private int vehicleId;         // line-service DB PK
    private String vehicleNumber;  // 차량 번호 (VH-0001 형식) — vin 역할
    private int orderId;
    private String carModel;
    private String carColor;
    private String currentProcess; // LineType enum 값 (차체/도장/조립/품질검사/출고)
    private String currentStation; // 조립 라인 전용 세부 공정
    private int currentLineId;     // 현재 라인 ID
    private String status;         // VehicleStatus (PENDING/PROCESSING/QC_PASS/QC_FAIL)
}
