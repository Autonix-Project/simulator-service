package com.autonix.simulator_service.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class SimulationException extends RuntimeException {

    private final HttpStatus status;

    public SimulationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}