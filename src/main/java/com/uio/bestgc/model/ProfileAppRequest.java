package com.uio.bestgc.model;

import org.springframework.web.multipart.MultipartFile;

// @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProfileAppRequest(
        double throughputWeight,
        double pauseTimeWeight,
        // String userAvailableMemory,
        int monitoringTime,
        String args,
        MultipartFile file) {

    public ProfileAppRequest {
        // if (throughputWeight == null && pauseTimeWeight == null) {
        // throw new IllegalArgumentException("Throughput Weight and Pause Weight can't
        // both be null.");
        // }
        if (throughputWeight + pauseTimeWeight != 1) {
            throw new IllegalArgumentException("Sum of Throughput Weight and Pause Weight should be equal to 1.");
        }
    }

}
