package com.autonix.simulator_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulationResponseDto {
    private boolean isRunning;    // 가동 여부
    private long tickRate;        // 시뮬레이션 주기 (ms)
    private String statusMessage; // 현재 상태 메시지
}
