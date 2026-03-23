package com.autonix.simulator_service.domain;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class SimulationStatus {
    private boolean isRunning = false;
    private long tickRate = 5000; // 5초 주기
}
