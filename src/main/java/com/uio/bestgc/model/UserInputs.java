package com.uio.bestgc.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInputs {
    private String applicationName;
    private PerformanceMetric metric;
    private String userOS;
    private String pId;
    private String userAppToRun;
    private Double weightThroughput;
    private Double weightPause;
    private String userAvailableMemory;
    private int samplingTime;
    private Boolean runAppWithBestGC;
}
