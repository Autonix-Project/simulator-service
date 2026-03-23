package com.autonix.simulator_service.domain;

import lombok.Getter;

@Getter
public enum ProcessStep {
    BODY_A("차체A", 3),
    BODY_B("차체B", 3),
    PAINTING("도장", 5),
    ASSEMBLY_A("조립A", 7),
    ASSEMBLY_B("조립B", 7),
    QC("품질검사", 4);

    private final String name;
    private final int duration;

    ProcessStep(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public ProcessStep getNext() {
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < ProcessStep.values().length ? ProcessStep.values()[nextOrdinal] : null;
    }
}