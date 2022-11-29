package com.uio.bestgc.model;

import lombok.Getter;

@Getter
public enum PerformanceMetric {
    PAUSETIME("Pause Time"),
    MEMORYUSAGE("Memory Usage"),
    THROUGHPUT("Throughput"),
    ALL("All");

    private String label;
    private PerformanceMetric(String s) {
        this.label= s;
    }
}
