package com.uio.bestgc.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uio.bestgc.model.ProfileAppRequest;
import com.uio.bestgc.model.ProfileAppResponse;
import com.uio.bestgc.model.RunAppRequest;
import com.uio.bestgc.model.RunAppResponse;

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

        final int monitoringTime = profileAppRequest.monitoringTime();

        // metrics
        List<Float> cpuUsages;
        List<Float> ioTimes;
        List<Float> cpuTimes;
        List<Float> heapSizes;

        int[] matrixHeapSizes = matrixService.getHeapSizes();
        for (int i = 0; i < matrixHeapSizes.length; i++)
            System.out.println("Aqui : " + matrixHeapSizes[i]);

        int runId = -1;
        while (true) {
            runId++;
            int heapSize = matrixHeapSizes[runId];
            try {
                cpuUsages = new ArrayList<>();
                ioTimes = new ArrayList<>();
                cpuTimes = new ArrayList<>();
                heapSizes = new ArrayList<>();

                appProcess = Runtime.getRuntime()
                        .exec(getProfileJarCommand(appPath, profileAppRequest.args(), heapSize));
                long pid = appProcess.pid();
                System.out.println(pid);
                var heapCommand = getHeapCommand(pid);
                var topCommand = getTopCommand(pid);
                System.out.println(String.join(" ", heapCommand));
                System.out.println(String.join(" ", topCommand));

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

                        float cpuUsage = (float) Math.round(Float.valueOf(lines.get(1).trim().split("\\s+")[8]) * 100)
                                / 100
                                / CPU_CORES;

                        // is_cpu_intensive += us > wa ? 1 : us == wa ? 0 : -1;
                        cpuTimes.add(us);
                        ioTimes.add(wa);
                        cpuUsages.add(cpuUsage);
                    }

                    p = Runtime.getRuntime().exec(heapCommand);
                    try (var b = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        var line = b.readLine();
                        heapSizes.add(Float.valueOf(line));
                    }
                    // Double maxHeapUsage = statistics.getMaxHeapUsage() * 1.2 / 1024;
                }

                // if process is still executing or its return code is 0 i.e., success
                if (appProcess.isAlive() || appProcess.exitValue() == 0) {
                    System.out.println("A SAIRRRRRRRRRR");
                    break;
                }

                // process executed successfully
                if (appProcess.exitValue() != 0 && runId == matrixHeapSizes.length) {
                    System.out.println("App failed when using: " + matrixHeapSizes[runId]);
                    // TODO: create better exceptions
                    return null;
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        float maxHeap = heapSizes.stream().max(Float::compare).orElseThrow() / 1024;

        float maxHeapUsage = maxHeap * 1.2f; // NOTE: recommend heapSize

        float totalCpuUsage = 0;
        float totalCpuTime = 0;
        float totalIoTime = 0;
        for (int i = 0; i < cpuUsages.size(); i++) {
            totalCpuUsage += cpuUsages.get(i);
            totalCpuTime += cpuTimes.get(i);
            totalIoTime += ioTimes.get(i);
        }
        float avgCpuUsage = (float)Math.round(totalCpuUsage / cpuUsages.size() * 100) / 100; 
        float avgCpuTime =  (float)Math.round(totalCpuTime / cpuUsages.size() * 100) / 100; 
        float avgIoTime =  (float)Math.round(totalIoTime / cpuUsages.size() * 100) / 100;

        boolean isCpuIntensive = totalCpuUsage / cpuUsages.size() >= 60;
        BestGC bestGC = matrixService.getBestGC(isCpuIntensive, maxHeapUsage,
                profileAppRequest.throughputWeight(), profileAppRequest.pauseTimeWeight());

        response = new ProfileAppResponse(bestGC.gc(), bestGC.heapSize(), maxHeap, cpuUsages, ioTimes,
                cpuTimes,  avgCpuUsage, avgCpuTime, avgIoTime, isCpuIntensive);
        System.out.println(response);

        if (appProcess != null && appProcess.isAlive())
            appProcess.destroy();

        return response;

    }

    public synchronized RunAppResponse runApp(RunAppRequest runAppRequest, String appPath) {

        Process appProcess = null;
        try {
            appProcess = Runtime.getRuntime()
                    .exec(getExecJarCommand(runAppRequest, appPath));

            // TODO: some kind of status return
            System.out.println("App process running with pid: " + appProcess.pid());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return new RunAppResponse();

        // if (appProcess != null && appProcess.isAlive())
        // appProcess.destroy();

        // return response;

    }

    public String[] getAvailableGCs() {
        // TODO: find / implement a way to get available GCs
        return new String[] { "G1", "Parallel", "Z" };
    }

    public String[] getJars() {
        File jarsFolder = new File("jars");
        FilenameFilter filter = (dir, name) -> name.endsWith(".jar");
        File[] files = jarsFolder.listFiles(filter);
        List<String> fileNames = new ArrayList<>();

        Arrays.stream(files).forEach(f -> fileNames.add(f.getName()));

        return fileNames.toArray(String[]::new);
    }

    private String[] getProfileJarCommand(String app, String args, int heapSize) {
        String minHeapSize = "-Xms" + heapSize + "m";
        String maxHeapSize = "-Xmx" + heapSize + "m";
        System.out.println("java -jar " + maxHeapSize + " " + minHeapSize + app + " " + args);

        return ("java -jar " + maxHeapSize + " " + minHeapSize + " " + app + " " + args).split(" ");
    }

    private String[] getExecJarCommand(RunAppRequest request, String appPath) {
        // TODO: use string builder
        String command = "java";
        String gc = request.garbageCollector();
        command += gc != null ? " -XX:+Use" + gc + "GC" : "";

        // Heap size and custom policy
        if (request.customHeapGCPolicy() != null) {
            command += " " + request.customHeapGCPolicy();
        } else {
            command += " -Xms" + request.heapSize() + "m";
            command += " -Xmx" + request.heapSize() + "m";
        }

        if (request.enableLog()) {

            String baseName = Paths.get(appPath).getFileName().toString().split(".")[0];
            System.out.println("File name: " + baseName);
            command += " -Xlog:gc*,safepoint:file=" + baseName + ".log::filecount=0";
        }

        command += request.gcArgs() != null ? " " + request.gcArgs() : "";
        command += " -jar " + appPath + " " + request.args();

        System.out.println("Command: " + command);
        // System.out.println("java" + "-XX:+Use{gc}GC" + "-Xms{heap_size}m" +
        // "-Xmx{heap_size}m" +
        // "-Xlog:gc*,safepoint:file={utils.get_benchmark_log_path(gc,
        // benchmark_group.value, benchmark, heap_size)}::filecount=0"
        // +
        // "-jar" +
        // app +
        // args);
        return command.split(" ");
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
