package com.autonix.simulator_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autonix.simulator_service.dto.SimulationConfigDto;
import com.autonix.simulator_service.dto.SimulationResponseDto;
import com.autonix.simulator_service.service.SimulationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulationService simulationService;
    // producer 직접 주입 제거 — 모든 로직은 SimulationService로 위임

    /** GET /v1/simulator/status — 현재 가동 상태 조회 */
    @GetMapping("/status")
    public ResponseEntity<SimulationResponseDto> getStatus() {
        return ResponseEntity.ok(simulationService.getCurrentStatus());
    }

    /** POST /v1/simulator/executions — 시뮬레이션 시작 */
    @PostMapping("/executions")
    public ResponseEntity<String> start() {
        simulationService.startSimulation();
        return ResponseEntity.ok("시뮬레이션 가동 시작");
    }

    /** POST /v1/simulator/executions/stop — 시뮬레이션 중지 (명세서 경로 수정) */
    @PostMapping("/executions/stop")
    public ResponseEntity<String> stop() {
        simulationService.stopSimulation();
        return ResponseEntity.ok("시뮬레이션 중지");
    }

    /** PATCH /v1/simulator/config — 공정 속도(Tick Rate) 조절 */
    @PatchMapping("/config")
    public ResponseEntity<String> updateConfig(@RequestBody SimulationConfigDto configDto) {
        simulationService.updateConfig(configDto);
        return ResponseEntity.ok("시뮬레이터 설정 변경 완료");
    }

    /** POST /v1/simulator/move/{vehicleId} — 특정 차량 즉시 다음 공정으로 강제 이동 */
    @PostMapping("/move/{vehicleId}")
    public ResponseEntity<String> forceMove(@PathVariable String vehicleId) {
        simulationService.forceMoveVehicle(vehicleId);
        return ResponseEntity.ok("차량 " + vehicleId + " 즉시 다음 공정으로 이동");
    }

    /** POST /v1/simulator/error/line/{lineId} — 특정 라인 장애 강제 발생 */
    @PostMapping("/error/line/{lineId}")
    public ResponseEntity<String> triggerLineFault(@PathVariable String lineId) {
        simulationService.triggerLineFault(lineId);
        return ResponseEntity.ok("라인 " + lineId + " 장애 발생 처리 완료");
    }

    /** POST /v1/simulator/error/stock/{partId} — 특정 부품 재고 강제 0으로 알림 트리거 */
    @PostMapping("/error/stock/{partId}")
    public ResponseEntity<String> triggerStockError(@PathVariable String partId) {
        simulationService.triggerStockError(partId);
        return ResponseEntity.ok("부품 " + partId + " 재고 부족 상황 연출 완료");
    }

    /** POST /v1/simulator/error/clear — 모든 장애 복구 */
    @PostMapping("/error/clear")
    public ResponseEntity<String> clearError() {
        simulationService.clearAllErrors();
        return ResponseEntity.ok("모든 장애 복구 및 가동 준비 완료");
    }

    /** GET /v1/simulator/logs — 시뮬레이터 이벤트 로그 최신 내역 조회 */
    @GetMapping("/logs")
    public ResponseEntity<List<String>> getLogs() {
        return ResponseEntity.ok(simulationService.getLogs());
    }
}