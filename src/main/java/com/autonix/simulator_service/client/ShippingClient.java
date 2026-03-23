package com.autonix.simulator_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.autonix.simulator_service.dto.ShippingCreateDto;

@FeignClient(name = "shipping-service")
public interface ShippingClient {

    // QC 완료(생산 완료) 시 배송 등록 요청
    // POST /v1/shippings/create — 명세서 기준
    @PostMapping("/v1/shippings/create")
    void createShipping(@RequestBody ShippingCreateDto dto);
}