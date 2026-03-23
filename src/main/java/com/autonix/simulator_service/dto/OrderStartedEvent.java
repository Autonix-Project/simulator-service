package com.autonix.simulator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStartedEvent {
    private String orderId;
    private String modelName;
    private Integer quantity; // 생산해야 할 차량 대수
}
