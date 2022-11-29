package com.uio.bestgc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public abstract class Results {
    private float weight_pause;
    private float weight_throughput;
    private String GC;
}
