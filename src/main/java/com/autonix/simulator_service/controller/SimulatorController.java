package com.autonix.simulator_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autonix.simulator_service.dto.SimulationResponseDto;
import com.autonix.simulator_service.message.SimulationProducer;
import com.autonix.simulator_service.service.SimulationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {
    private final SimulationService simulationService;
    private final SimulationProducer producer;

    @PostMapping("/executions")
    public ResponseEntity<String> start() {
        // 직접 status를 건드리지 않고 서비스를 호출합니다.
        simulationService.startSimulation(); 
        return ResponseEntity.ok("시뮬레이션 가동 시작");
    }

    @PostMapping("/error/line/{lineId}")
    public ResponseEntity<String> triggerError(@PathVariable String lineId) {
        // 시뮬레이터 중지
        simulationService.stopSimulation();
        
        // 장애 이벤트 발행
        producer.sendFaultEvent("LINE_FAULT", lineId);
        
        return ResponseEntity.ok("라인 " + lineId + " 장애 발생 시뮬레이션 시작");
    }

    @GetMapping("/status")
    public ResponseEntity<SimulationResponseDto> getStatus() {
        return ResponseEntity.ok(simulationService.getCurrentStatus());
    }
}