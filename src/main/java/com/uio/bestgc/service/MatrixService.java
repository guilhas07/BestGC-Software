package com.uio.bestgc.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MatrixService {

    Matrix matrix;

    public MatrixService() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var name = "HotSpot_11_10_0_19.json";
            // var name ="Graal_11_10_0_19.json";
            System.out.println("Loading matrix file: " + name);
            var matrixData = objectMapper.readValue(new File(name), FullMatrixData.class);

            this.matrix = matrixData.matrix();
            System.out.println("Matrix: " + matrix);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getHeapSizes() {
        return matrix.keySet().stream().mapToInt(Integer::valueOf).sorted().toArray();
    }

    public BestGC getBestGC(boolean cpuIntensive, float cpuAvgPercentage, float maxHeapUsed) {

        int min = Integer.MAX_VALUE;
        for (String key : this.matrix.keySet()) {
            var heapSize = Integer.valueOf(key);
            if (heapSize >= maxHeapUsed && heapSize < min)
                min = heapSize;
        }
        System.out.println("Selected Heap Size: " + min);

        float throughputWeight = 0;
        float pauseTimeWeight = 0;

        // NOTE: Mapping the range [30,90] to [0,1] with this equation: y = x/60 - 0.5
        // and clamping values <30 to 0 and >90 to 1
        if (cpuAvgPercentage < 30)
            throughputWeight = 0;
        else if (cpuAvgPercentage > 90)
            throughputWeight = 1;
        else
            throughputWeight = (float) Math.round(cpuAvgPercentage / 60 * 0.5f * 100) / 100;

        pauseTimeWeight = 1 - throughputWeight;
        System.out.printf("CpuAvgPercentage=%s\tCalculated weights: throughput weight=%s, pause_time weight=%s\n",
                cpuAvgPercentage, throughputWeight,
                pauseTimeWeight);

        var gcMetrics = this.matrix.get(String.valueOf(min));

        String bestgc = "";
        double value = Double.MAX_VALUE;
        System.out.println("Selecting best gc:");
        for (var entry : gcMetrics.entrySet()) {
            var perfMetric = entry.getValue();
            var gc = entry.getKey();
            double score = perfMetric.throughput() * throughputWeight + perfMetric.pause_time() * pauseTimeWeight;
            System.out.printf("GC %s score: %s\n", gc, score);
            if (score < value) {
                bestgc = entry.getKey();
                value = score;
            }

        }
        return new BestGC(bestgc, min);
    }

    public BestGC getBestGC(boolean cpuIntensive, float maxHeapUsed, double throughputWeight, double pauseTimeWeight) {
        int min = Integer.MAX_VALUE;
        for (String key : this.matrix.keySet()) {
            var heapSize = Integer.valueOf(key);
            if (heapSize >= maxHeapUsed && heapSize < min)
                min = heapSize;
        }

        var gcMetrics = this.matrix.get(String.valueOf(min));

        String gc = "";
        double value = Double.MAX_VALUE;
        for (var entry : gcMetrics.entrySet()) {
            var perfMetric = entry.getValue();
            double score = perfMetric.throughput() * throughputWeight + perfMetric.pause_time() * pauseTimeWeight;
            System.out.println(score);
            if (score < value) {
                gc = entry.getKey();
                value = score;
            }

        }
        return new BestGC(gc, min);
    }
}

record BestGC(String gc, int heapSize) {
}

record FullMatrixData(Matrix matrix, List<String> garbage_collectors, Map<String, List<String>> benchmarks) {
}

class Matrix extends HashMap<String, Map<String, PerformanceMetrics>> {
};

record PerformanceMetrics(double throughput, double pause_time) {
}

record GCValue(String gc, double value) {
}
