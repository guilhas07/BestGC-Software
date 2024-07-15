package com.uio.bestgc.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MatrixService {

    Matrix matrix;

    public MatrixService() {
        // Gson gson = new Gson();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse JSON file into Person record
            this.matrix = objectMapper.readValue(new File("matrix.json"), Matrix.class);

            // Print the parsed object
            System.out.println(matrix);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getHeapSizes() {
        return matrix.cpu_intensive_matrix().keySet().stream().mapToInt(Integer::valueOf).sorted().toArray();
    }

    public int[] getHeapSizes() {
        var matrix = matrix.cpu_intensive_matrix();
    }

    public BestGC getBestGC(boolean cpuIntensive, float cpuAvgPercentage, float maxHeapUsed) {
        var local_matrix = cpuIntensive ? matrix.cpu_intensive_matrix() : matrix.non_cpu_intensive_matrix();

        int min = Integer.MAX_VALUE;
        for (String key : local_matrix.keySet()) {
            var heapSize = Integer.valueOf(key);
            if (heapSize >= maxHeapUsed && heapSize < min)
                min = heapSize;
        }

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

        var gcMetrics = local_matrix.get(String.valueOf(min));

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

    public BestGC getBestGC(boolean cpuIntensive, float maxHeapUsed, double throughputWeight, double pauseTimeWeight) {
        var local_matrix = cpuIntensive ? matrix.cpu_intensive_matrix() : matrix.non_cpu_intensive_matrix();

        int min = Integer.MAX_VALUE;
        for (String key : local_matrix.keySet()) {
            var heapSize = Integer.valueOf(key);
            if (heapSize >= maxHeapUsed && heapSize < min)
                min = heapSize;
        }

        var gcMetrics = local_matrix.get(String.valueOf(min));

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

// Define Metrics record
record Matrix(Map<String, Map<String, PerformanceMetrics>> cpu_intensive_matrix,
        Map<String, Map<String, PerformanceMetrics>> non_cpu_intensive_matrix,
        List<String> garbage_collectors) {
}

// Define PerformanceMetrics record
record PerformanceMetrics(double throughput, double pause_time) {
}

record GCValue(String gc, double value) {
}
