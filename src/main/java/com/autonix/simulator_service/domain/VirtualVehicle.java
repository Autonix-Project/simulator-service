package com.autonix.simulator_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VirtualVehicle {
    private String orderId;
    private String vin; // 차량 식별 번호
    private String modelName;
    private ProcessStep currentStep;
    private int remainingTime; // 현재 공정 남은 시간
}
