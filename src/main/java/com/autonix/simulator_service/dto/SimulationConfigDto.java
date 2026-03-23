package com.autonix.simulator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimulationConfigDto {
    // 시뮬레이션 심박 주기 (밀리초 단위, 예: 5000)
    private long tickRate;
}