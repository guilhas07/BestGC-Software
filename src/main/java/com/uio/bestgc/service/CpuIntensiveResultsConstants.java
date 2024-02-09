package com.uio.bestgc.service;

import java.util.HashMap;
import java.util.Map;

public final class CpuIntensiveResultsConstants {
    //static final Map<String, String> cpuIntensiveResults;
    static final Map<String, Double> matrix_8192 = new HashMap<>();
    static final Map<String, Double> matrix_4096 = new HashMap<>();
    static final Map<String, Double> matrix_2048 = new HashMap<>();
    static final Map<String, Double> matrix_1024 = new HashMap<>();
    static final Map<String, Double> matrix_512 = new HashMap<>();
    static final Map<String, Double> matrix_256 = new HashMap<>();

    /*static {
        cpuIntensiveResults = new HashMap<>();
        *//*cpuIntensiveResults.put("throughput", "CMS");
        cpuIntensiveResults.put("pausetime", "ZGC");
        cpuIntensiveResults.put("memoryusage", "CMS");*//*
        //cpuIntensiveResults = Collections.unmodifiableMap(cpuIntensiveResults);
        cpuIntensiveResults.put("throughput", "PS");
        cpuIntensiveResults.put("pausetime", "ZGC");
        cpuIntensiveResults.put("memoryusage", "ZGC");
    }*/

    static {
        matrix_8192.put("g1_throughput", 1D);
        matrix_8192.put("g1_pause", 1D);
        matrix_8192.put("ps_throughput", 0.954);
        matrix_8192.put("ps_pause", 1.688);
        matrix_8192.put("shenandoah_throughput", 1.093);
        matrix_8192.put("shenandoah_pause", 0.148);
        //z_throughput
        matrix_8192.put("z_throughput", 1.098);
        matrix_8192.put("z_pause", 0.044);
    }

    static {
        matrix_4096.put("g1_throughput", 1D);
        matrix_4096.put("g1_pause", 1D);
        matrix_4096.put("ps_throughput", 0.956);
        matrix_4096.put("ps_pause", 1.167);
        matrix_4096.put("shenandoah_throughput", 1.114);
        matrix_4096.put("shenandoah_pause", 0.091);
        matrix_4096.put("z_throughput", 1.139);
        matrix_4096.put("z_pause", 0.073);
    }

    static {
        matrix_2048.put("g1_throughput", 1D);
        matrix_2048.put("g1_pause", 1D);
        matrix_2048.put("ps_throughput", 0.942);
        matrix_2048.put("ps_pause", 1.644);
        matrix_2048.put("shenandoah_throughput", 1.103);
        matrix_2048.put("shenandoah_pause", 0.309);
        matrix_2048.put("z_throughput", 1.171);
        matrix_2048.put("z_pause", 0.102);
    }

    static {
        matrix_1024.put("g1_throughput", 1D);
        matrix_1024.put("g1_pause", 1D);
        matrix_1024.put("ps_throughput", 0.914);
        matrix_1024.put("ps_pause", 1.499);
        matrix_1024.put("shenandoah_throughput", 1.153);
        matrix_1024.put("shenandoah_pause", 0.270);
        matrix_1024.put("z_throughput", 1.189);
        matrix_1024.put("z_pause", 0.102);
    }

    static {
        matrix_512.put("g1_throughput", 1D);
        matrix_512.put("g1_pause", 1D);
        matrix_512.put("ps_throughput", 0.908);
        matrix_512.put("ps_pause", 1.299);
        matrix_512.put("shenandoah_throughput", 1.222);
        matrix_512.put("shenandoah_pause", 0.244);
        matrix_512.put("z_throughput", 1.334);
        matrix_512.put("z_pause", 0.068);
    }

    static {
        matrix_256.put("g1_throughput", 1D);
        matrix_256.put("g1_pause", 1D);
        matrix_256.put("ps_throughput", 0.938);
        matrix_256.put("ps_pause", 1.091);
        matrix_256.put("shenandoah_throughput", 1.429);
        matrix_256.put("shenandoah_pause", 0.242);
        matrix_256.put("z_throughput", 1.841);
        matrix_256.put("z_pause", 0.092);
    }

    public Map<String, Double> getMatrix(String heap){
        Class<? extends CpuIntensiveResultsConstants> aClass = this.getClass();
        aClass.getDeclaredFields();
        return null;
    }
    public CpuIntensiveResultsConstants() {
    }

}
