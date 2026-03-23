package com.autonix.simulator_service.domain;

public enum ProcessStep {
    BODY_SHOP("차체공정", 3),      // 3초 소요
    PAINT_SHOP("도장공정", 5),     // 5초 소요
    ASSEMBLY_LINE("조립공정", 7),  // 7초 소요
    QUALITY_CONTROL("검사공정", 4); // 4초 소요

    private final String name;
    private final int duration; // 공정 소요 시간(초)

    ProcessStep(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() { return name; }
    public int getDuration() { return duration; }

    // 다음 공정 가져오기 로직
    public ProcessStep getNext() {
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < ProcessStep.values().length ? ProcessStep.values()[nextOrdinal] : null;
    }
}
