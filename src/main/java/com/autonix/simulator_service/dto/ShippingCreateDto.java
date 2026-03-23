package com.autonix.simulator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingCreateDto {
    private String vin;      // 차량 식별 번호 (예: "ORDER-1")
    private String orderId;  // 주문 ID — shipping-service가 order-service 조회 시 사용
}