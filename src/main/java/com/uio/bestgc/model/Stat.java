package com.uio.bestgc.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Stat {
    int cpuNum; // number of core
    double lastCpuSum = 0;//the last sum of the cpu usage
    double lastIdel = 0;//the last cpu idle time
    double user;
    double nice;
    double system;
    double idle;
    double iowait;
    double irq;
    double softirq;
    double steal;
    double guest;
    double guestNice;
    double cpuUsage;

    public Stat() {
        this.lastCpuSum = 0;
        this.lastIdel= 0;
    }
}
