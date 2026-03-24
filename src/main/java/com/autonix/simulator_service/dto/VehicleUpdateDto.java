package com.autonix.simulator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * line-service PATCH /lines/{lineId}/vehicles 요청 바디
 * line-service VehicleUpdateRequestDTO 필드와 일치
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUpdateDto {
    private int vehicleId;         // line-service DB PK (@NotNull)
    private String currentProcess; // LineType enum 값 (차체/도장/조립/품질검사/출고) (@NotNull)
    private String currentStation; // 조립 라인 전용 세부 공정 (nullable)
}
