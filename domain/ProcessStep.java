package com.autonix.simulator_service.domain;

public enum ProcessStep {
    BODY_SHOP, PAINT_SHOP, ASSEMBLY_LINE, QUALITY_CHECK, SHIPPING_READY;

    public ProcessStep next() {
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < values().length ? values()[nextOrdinal] : null;
    }
}
