package com.autonix.simulator_service.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VirtualVehicle {
    private String orderId;
    private String vin;           // 차량 식별 번호
    private String modelName;
    private ProcessStep currentStep;
    private int remainingTime;    // 현재 공정 남은 시간 (초)

    // line-service 연동용 — 주문 이벤트로 생성된 차량은 -1 (미확인)
    private int vehicleId;        // line-service DB PK
    private int lineId;           // 현재 라인 ID
}
