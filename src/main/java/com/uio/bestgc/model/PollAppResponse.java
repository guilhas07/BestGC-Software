package com.uio.bestgc.model;

public record PollAppResponse(
        String name,
        String command,
        float cpuUsage,
        float ioTime,
        float heapSize) {
}
