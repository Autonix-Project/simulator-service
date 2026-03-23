package com.autonix.simulator_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    // 재고 차감 요청 (팀원 명세에 따라 경로 수정 필요 가능)
    @PostMapping("/v1/inventory/deduct")
    void deductInventory(@RequestParam("orderId") Long orderId);
}
