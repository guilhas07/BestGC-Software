package com.uio.bestgc.model;

import org.springframework.web.multipart.MultipartFile;

public record RunAppRequest(
        String jar,
        String gc,
        String args,
        int heapSize,
        MultipartFile file) {

    public RunAppRequest {
    }
}
