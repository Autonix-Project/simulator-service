package com.autonix.simulator_service.domain;

public enum ProcessStep {
    BODY_SHOP("차체"), 
    PAINT_SHOP("도장"), 
    ASSEMBLY_LINE("조립"), 
    QUALITY_CHECK("품질검사"), 
    SHIPPING_READY("출고대기");

    private final String name;
    ProcessStep(String name) { this.name = name; }
}
