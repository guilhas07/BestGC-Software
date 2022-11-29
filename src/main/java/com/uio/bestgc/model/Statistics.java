package com.uio.bestgc.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Statistics {
    public Statistics(List<Double> cpuUsage, List<Double> memoryUsage) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    String pid;
    List<Double> cpuUsage;
    List<Double> memoryUsage;
    Boolean isCpuIntensive;
    Double avgCpuPerCore;
    Double maxHeapUsage;//in KB
    Double avgHeapUsage;
}
