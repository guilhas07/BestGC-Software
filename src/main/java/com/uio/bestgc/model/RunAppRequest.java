package com.uio.bestgc.model;

import org.springframework.web.multipart.MultipartFile;

public record RunAppRequest(
        String garbageCollector,
        String gcArgs,
        int heapSize,
        String customHeapGCPolicy,
        String jar,
        String args,
        boolean enableLog,
        MultipartFile file) {

    public RunAppRequest {
        if (customHeapGCPolicy != null && customHeapGCPolicy.isEmpty())
            customHeapGCPolicy = null;

        if (gcArgs != null && gcArgs.isEmpty())
            gcArgs = null;

        if (args != null && args.isEmpty())
            args = null;

        if (heapSize <= 0) {
            throw new IllegalArgumentException(
                    "Heap size sould be a value greater than 0.");
        }
    }
}
