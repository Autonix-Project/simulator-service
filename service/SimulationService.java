package com.autonix.simulator_service.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.autonix.simulator_service.client.LineClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulationService {
    private final LineClient lineClient;
    private boolean active = false; // 가동 플래그

    @Scheduled(fixedDelay = 5000) // 5초마다 '틱' 발생
    public void runSimulationTick() {
        if (!active) return;

        System.out.println("[Simulator] 공정 이동 로직 실행 중...");
        
        // 로직 순서:
        // 1. Line Service에서 현재 생산 중인 차량 리스트 조회 (Feign)
        // 2. 각 차량의 현재 단계를 확인 후 다음 단계 계산
        // 3. Line Service에 업데이트 요청 (Feign)
        // 4. 만약 '조립' 단계라면 Inventory Service에 재고 차감 요청
    }

    public void setStatus(boolean status) { this.active = status; }
}