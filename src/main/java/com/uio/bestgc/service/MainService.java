package com.uio.bestgc.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uio.bestgc.model.ProfileAppRequest;
import com.uio.bestgc.model.ProfileAppResponse;

@Service
public class MainService {

    @Autowired
    MatrixService matrixService;

    // @Value("${monitoring-time}")
    // private int monitoringTime;

    final private int profileInterval = 1;

    final private int CPU_CORES;

    public MainService() {
        CPU_CORES = Runtime.getRuntime().availableProcessors();
        System.out.println("CPU CORES: " + CPU_CORES);
    }

    public synchronized ProfileAppResponse profileApp(ProfileAppRequest profileAppRequest, String appPath) {

        Process appProcess = null;
        ProfileAppResponse response = null;
        try {
            int monitoringTime = profileAppRequest.monitoringTime();

            appProcess = Runtime.getRuntime().exec(getExecJarCommand(appPath, profileAppRequest.args()));
            long pid = appProcess.pid();
            var heapCommand = getHeapCommand(pid);
            var topCommand = getTopCommand(pid);
            System.out.println(pid);
            System.out.println(String.join(" ", heapCommand));
            System.out.println(String.join(" ", topCommand));

            int is_cpu_intensive = 0; // if < 0 then its I/O intensive
            List<Float> cpuUsage = new ArrayList<>();
            List<Float> ioTime = new ArrayList<>();
            List<Float> cpuTime = new ArrayList<>();
            List<Float> heapSizes = new ArrayList<>();

            long startTime = System.currentTimeMillis();
            Thread.sleep(1000);
            while ((System.currentTimeMillis() - startTime) / 1000 < monitoringTime && appProcess.isAlive()) {

                var p = Runtime.getRuntime().exec(topCommand);
                try (var b = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    var lines = b.lines().collect(Collectors.toList());
                    if (lines.size() != 8) {
                        // NOTE: correct top output has 8 lines. When it doesn't probably means the
                        // process already died
                        System.out.println("[Error]: Top failed");
                        break;
                    }

                    // NOTE: from man top(1)
                    // us, user : time running un-niced user processes
                    // wa, IO-wait : time waiting for I/O completion
                    // %Cpu(s): 15.1 us, 2.2 sy, 0.0 ni, 81.2 id, 0.0 wa, 0.0 hi, 1.6 si, 0.0 st
                    Pattern pattern = Pattern.compile("(\\d+.\\d+) us.*(\\d+.\\d+) wa");
                    Matcher matcher = pattern.matcher(lines.get(2));
                    if (!matcher.find()) {
                        System.out.println("Error: couldn't match cpu us time and IO-wait time in " + lines.get(2));
                        break;
                    }

                    float us = (float) Math.round(Float.valueOf(matcher.group(1)) * 100) / 100;
                    float wa = (float) Math.round(Float.valueOf(matcher.group(2)) * 100) / 100;

                    lines = lines.subList(lines.size() - 2, lines.size());
                    if (!lines.get(0).trim().split("\\s+")[8].equals("%CPU")) {
                        System.out.println("[ERROR]: Top command with wrong format");
                        break;
                    }

                    float cpuAvg = (float) Math.round(Float.valueOf(lines.get(1).trim().split("\\s+")[8]) * 100) / 100
                            / CPU_CORES;

                    is_cpu_intensive += us > wa ? 1 : -1;
                    cpuTime.add(us);
                    ioTime.add(wa);
                    cpuUsage.add(cpuAvg);
                }

                p = Runtime.getRuntime().exec(heapCommand);
                try (var b = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    var line = b.readLine();
                    heapSizes.add(Float.valueOf(line));
                }
                // Double maxHeapUsage = statistics.getMaxHeapUsage() * 1.2 / 1024;
            }

            float maxHeap = heapSizes.stream().max(Float::compare).orElseThrow() / 1024;

            float maxHeapUsage = maxHeap * 1.2f; // NOTE: recommend heapSize

            BestGC bestGC = matrixService.getBestGC(is_cpu_intensive >= 0, maxHeapUsage,
                    profileAppRequest.throughputWeight(), profileAppRequest.pauseTimeWeight());

            response = new ProfileAppResponse(bestGC.gc(), bestGC.heapSize(), maxHeap, cpuUsage, ioTime,
                    cpuTime, is_cpu_intensive >= 0);
            System.out.println(response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (appProcess != null && appProcess.isAlive())
            appProcess.destroy();

        return response;

    }

    public String[] getJars() {
        File jarsFolder = new File("jars");
        FilenameFilter filter = (dir, name) -> name.endsWith(".jar");
        File[] files = jarsFolder.listFiles(filter);
        List<String> fileNames = new ArrayList<>();

        Arrays.stream(files).forEach(f -> fileNames.add(f.getName()));

        return fileNames.toArray(String[]::new);
    }

    private String[] getExecJarCommand(String app, String args) {
        System.out.println("java -jar " + app + " " + args);
        return ("java -jar " + app + " " + args).split(" ");
    }

    private String[] getTopCommand(long pid) {
        return ("top -bn 1 -p " + pid).split(" ");
    }

    private String[] getHeapCommand(long pid) throws Exception {
        // NOTE: Using /bin/bash -c to use pipes. \\n to prevent early expansion.
        return new String[] { "/bin/bash", "-c",
                "jstat -gc " + pid + " | awk 'END {sum = $4 + $6 + $8 + $10 + $12; printf \"%.2f\\n\", sum}'" };
    }

}
