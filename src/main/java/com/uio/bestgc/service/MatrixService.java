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

    public BestGC getBestGC(boolean cpuIntensive, float maxHeapUsed, double weightThroughput, double weightPause) {
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
            double score = perfMetric.throughput() * weightThroughput + perfMetric.pause_time() * weightPause;
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
