package com.uio.bestgc.service;

import com.uio.bestgc.model.Statistics;
import com.uio.bestgc.model.UserInputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Service
public class MainService extends Profiler {
    private static final Logger logger = LoggerFactory.getLogger(OldResultsService.class);
    private final EngagedCoresService engagedCoresService;
    @Value("${cpu.threshold}")
    private String cpuIntensiveThreshold;
    @Value("${top.capture.number}")
    private String topCaptureNumber;
    @Value("${sampling.time}")
    private String samplingTime;
    private Statistics statistics = new Statistics();
    private ExecutorService executorService;
    private UserInputs userInputs;
    private List<String> printList = new ArrayList<>();
    private Process userappProcess = null;

    public MainService(EngagedCoresService engagedCoresService) {
        this.engagedCoresService = engagedCoresService;
    }

    public void run(UserInputs inputs) {
        userInputs = inputs;
        // run the user's app if there is a path to the jar file
        if (userInputs.getUserAppToRun() != null) {
            try {
                String java = System.getProperty("java.home") + "/bin/java";
                String jar = java + " -jar " + userInputs.getUserAppToRun();
                // System.out.println("JARRRRR: " + jar);
                userappProcess = Runtime.getRuntime().exec(jar);
                printList.add("********* " + "PID of the process is: " + userappProcess.pid() + "*********");
                // System.out.println("********* " + "PID of the process is: " +
                // userappProcess.pid() + "*********");
                // System.out.println("*************" + userInputs.getUserAppToRun() +
                // "*************");
                // wait for 5 second until the app is run
                Thread.sleep(5000);
                userInputs.setApplicationName(
                        userInputs.getUserAppToRun().contains(" ") ? userInputs.getUserAppToRun().split(" ")[0]
                                : userInputs.getUserAppToRun());
                // userInputs.setPId(String.valueOf(prs.pid()));
                // statistics.setPid(String.valueOf(prs.pid()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("Unable to run the Jar file!");
                e.printStackTrace();
            }
        }
        // Find the application pid when user didn't pass the pid
        if (userInputs.getPId() == null || userInputs.getPId() == "") {
            try {
                String process;
                // getRuntime: Returns the runtime object associated with the current Java
                // application.
                // exec: Executes the specified string command in a separate process.
                Process p = Runtime.getRuntime().exec("ps -axo pid,command");
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((process = input.readLine()) != null) {
                    if (process.contains(userInputs.getApplicationName())) {
                        String[] s = process.split(" ");
                        statistics.setPid(s[0] != "" ? s[0] : s[1]);
                        userInputs.setPId(s[0] != "" ? s[0] : s[1]);
                    }
                    // TODO use the logger class if there is no process with the given name
                }
                // System.out.println("The Process Id is : " + statistics.getPid());
                input.close();
                p.destroy();
                if (p.isAlive())
                    p.destroyForcibly();
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        System.out.println(userInputs.getUserAppToRun());
    }

    public void top(int time) {
        // To capture heap usage using jstat
        List<String> heapUsageList = new ArrayList<>();
        // Just for testing renaissance and dacapo benchmark I have bellow line
        engagedCoresService.setUserAppName(userInputs.getUserAppToRun());
        // To capture rss usage per second for the user app
        // List<String> rssList = new ArrayList<>();
        /*
         * engagedCoresService.setUserAppName(userInputs.getApplicationName() != null ?
         * userInputs.getApplicationName() : "");
         */
        CountDownLatch latch = new CountDownLatch(1);
        // a list to hold data from atop command, each batch run is seperated by "new"
        List<Process> topOutputProcessList = new ArrayList<>();
        Integer timer = (time > 0 ? time : Integer.parseInt(samplingTime)); // time to collect atop in second
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                // top -l 2 -pid 66336 -stats cpu >> it is better to set -l to 3.in the man top
                // it is said that son't trust to the first output using -l
                try {
                    if (userInputs.getUserOS().toLowerCase().contains("mac")) {
                        Process p = Runtime.getRuntime().exec("top -l " + Integer.parseInt(topCaptureNumber) + " -pid "
                                + statistics.getPid() + " -stats cpu");
                        topOutputProcessList.add(p);
                    } else if (userInputs.getUserOS().toLowerCase().contains("linux")) {
                        // String[] cmd = {"top -bn 1 -p ", statistics.getPid(), " awk 'NR>6
                        // {printf($9\"\\n\");'}" };
                        // ProcessBuilder pb = new ProcessBuilder("top", "-bn", "1", "-p",
                        // statistics.getPid());
                        // Process p = pb.start();
                        Process p = Runtime.getRuntime().exec("top -bn 1 -p " + statistics.getPid());
                        topOutputProcessList.add(p);
                        engagedCoresService.captureCpu();
                        ProcessBuilder processBuilder = new ProcessBuilder();

                        // processBuilder.command("bash", "-c", "ps -p " + statistics.getPid() + " -o
                        // pid,rss | awk 'END {print $2}'");
                        /*
                         * because we run the app with g1 all the $2(Current survivor space 1
                         * capacity),$5(Current eden space capacity),
                         * $7(Current old space capacity) and $11(Compressed class committed size) are
                         * not null-- result in KB
                         */
                        processBuilder.command("bash", "-c", "jstat -gc " + statistics.getPid()
                                + " | awk 'END {print $0}' | awk '{sum = $4 + $6 + $8 + $10+ $12; printf \"%.2f\", sum }' ");
                        // processBuilder.command("bash", "-c", "jstat -gc " + statistics.getPid() + " |
                        // awk 'END {print $0}' | awk '{sum = $2 + $5 + $7 + $11; printf \"%.2f\", sum
                        // }' ");
                        Process getJstat = processBuilder.start();
                        BufferedReader jstatHeapValue = new BufferedReader(
                                new InputStreamReader(getJstat.getInputStream()));
                        heapUsageList.add(jstatHeapValue.readLine());
                    }

                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        executor.schedule(new Runnable() {
            @Override
            public void run() {
                executor.shutdown();

                if (executor.isShutdown()) {
                    latch.countDown();
                }
            }
        }, timer, TimeUnit.SECONDS);
        boolean done = false;
        try {
            latch.await();
            done = executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (done) {
            parseTopOutputs(topOutputProcessList);
            calculateHeapUsage(heapUsageList);
            // Only kills the user app when the user give the jar file address to run and
            // test the app
            if (userInputs.getUserAppToRun() != null) {
                try {
                    Runtime.getRuntime().exec("kill " + userInputs.getPId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void calculateHeapUsage(List<String> heaps) {
        OptionalDouble max = heaps.stream().mapToDouble(Double::parseDouble).max();
        OptionalDouble average = heaps.stream().mapToDouble(Double::parseDouble).average();
        statistics.setMaxHeapUsage(max.getAsDouble());
        statistics.setAvgHeapUsage(average.getAsDouble());
    }

    public void parseTopOutputs(List<Process> processes) {
        System.out.println("** Parsing the outputs **");
        int counter = 0;
        statistics.setCpuUsage(new ArrayList<>());
        try {
            if (userInputs.getUserOS().toLowerCase().contains("mac")) {
                for (Process p : processes) {
                    String process;
                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((process = input.readLine()) != null) {
                        if (process.contains("%CPU")) {
                            counter++;
                            if (counter == Integer.parseInt(topCaptureNumber)) {// to get the last top result
                                Double usage = Double.parseDouble(input.readLine());
                                if (usage > 0)
                                    statistics.getCpuUsage().add(usage);
                                counter = 0;
                            }
                        }
                    }
                    input.close();
                    p.destroy();
                    if (p.isAlive())
                        p.destroyForcibly();
                }
            } else if (userInputs.getUserOS().toLowerCase().contains("linux")) {
                for (Process pp : processes) {
                    String prs;
                    BufferedReader input = new BufferedReader(new InputStreamReader(pp.getInputStream()));
                    while ((prs = input.readLine()) != null) {
                        if (prs.contains("%CPU")) {
                            String res = input.readLine();
                            if (res != null) {
                                Double usage = Double.parseDouble(Arrays.asList(res.split(" ")).stream()
                                        .filter(a -> !a.equals("")).collect(Collectors.toList()).get(8));
                                if (usage > 0)
                                    statistics.getCpuUsage().add(usage);
                            }
                        }
                    }
                    input.close();
                    pp.destroy();
                    if (pp.isAlive())
                        pp.destroyForcibly();
                }
            }
            averageCpuUsage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double averageCpuUsage() {
        double avgCpu = statistics.getCpuUsage().stream().mapToDouble(d -> d).average().orElse(0.0);
        printList.add("*************************" + userInputs.getApplicationName() + "*************************");
        printList.addAll(statistics.getCpuUsage().stream().map(a -> a.toString()).collect(Collectors.toList()));
        // System.out.println(statistics.getCpuUsage());
        double avgCpuPerCore = 0;
        if (userInputs.getUserOS().toLowerCase().contains("mac"))
            avgCpuPerCore = avgCpu / Runtime.getRuntime().availableProcessors();
        else if (userInputs.getUserOS().toLowerCase().contains("linux")) {
            long engCores = engagedCoresService.finalAvgEngagedCores() == 0 ? 1
                    : engagedCoresService.finalAvgEngagedCores();
            avgCpuPerCore = avgCpu / engCores;
            statistics.setAvgCpuPerCore(avgCpuPerCore);
            printList.add("AVG CPU:" + avgCpu + " AVG CPU usage per core:" + avgCpuPerCore);
            // System.out.println("AVG CPU used by the user's application:" + avgCpu + " AVG
            // CPU usage per core:" + avgCpuPerCore);

            if (avgCpuPerCore > Double.parseDouble(cpuIntensiveThreshold)) {
                statistics.setIsCpuIntensive(true);
            } else {
                statistics.setIsCpuIntensive(false);
            }
            if (engagedCoresService.finalAvgEngagedCores() < 1) {
                statistics.setIsCpuIntensive(false);
            }
        }

        // profileLogs("avg-cpu", userInputs.getApplicationName() != null ?
        // userInputs.getApplicationName() : "", printList);
        profileLogs("avg-cpu", userInputs.getUserAppToRun().split(" ")[1], printList);
        return avgCpuPerCore;
    }

    public Statistics findStatistics(int time) {
        top(time);
        return statistics;
    }

    public Process getUserappProcess() {
        return userappProcess;
    }

}
