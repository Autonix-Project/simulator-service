package com.autonix.simulator_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autonix.simulator_service.service.SimulationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {
    private final SimulationService simulationService;

    @PostMapping("/start")
    public String start() {
        simulationService.setStatus(true);
        return "공정 시뮬레이션 시작";
    }

    @PostMapping("/stop")
    public String stop() {
        simulationService.setStatus(false);
        return "공정 시뮬레이션 정지";
    }
}