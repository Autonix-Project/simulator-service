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
    private Long orderId;
    private String orderNumber;
    private String carModel;
    private String carColor;
    private Integer totalQuantity;
}
