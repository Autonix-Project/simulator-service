package com.autonix.simulator_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    // 조립 공정 재고 차감 (Feign 직접 호출 — 현재는 Kafka로 대체되어 미사용)
    @PostMapping("/v1/inventory/deduct")
    void deductInventory(@RequestParam("orderId") Long orderId);

    // 시연용: 특정 부품 재고를 강제로 0으로 만들어 재고 부족 알림 트리거
    // POST /v1/inventory/error/stock/{partId}
    @PostMapping("/v1/inventory/error/stock/{partId}")
    void forceStockEmpty(@PathVariable("partId") String partId);
}