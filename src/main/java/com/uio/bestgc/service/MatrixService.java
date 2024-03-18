package com.uio.bestgc.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class MatrixService {

    Matrix matrix;

    public void loadMatrix() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("matrix.json")) {
            // Parse JSON file into Metrics object
            this.matrix = gson.fromJson(reader, Matrix.class);

            // Print the parsed object
            System.out.println(matrix);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBestGC(boolean cpuIntensive, String heap, Double weightThroughput, Double weightPause) {
        loadMatrix();
        var local_matrix = cpuIntensive ? matrix.cpu_intensive_matrix() : matrix.non_cpu_intensive_matrix();
        var gcMetrics = local_matrix.get(heap);

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
        return gc;
    }
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
