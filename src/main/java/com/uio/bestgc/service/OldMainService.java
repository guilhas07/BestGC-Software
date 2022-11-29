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
import java.util.List;
import java.util.concurrent.*;

@Service
public class OldMainService {
    @Value("${cpu.threshold}")
    private String cpuIntensiveThreshold;
    @Value("${top.capture.number}")
    private String topCaptureNumber;
    @Value("${sampling.time}")
    private String samplingTime;
    private Statistics statistics = new Statistics();
    private static final Logger logger = LoggerFactory.getLogger(OldResultsService.class);
    private ExecutorService executorService;
    private UserInputs userInputs;

    public void run(UserInputs inputs) {
        userInputs =new UserInputs();
        userInputs =inputs;
        //run the user's app if there is a path to the jar file
        if (userInputs.getUserAppToRun() != null) {
            try {
                String java = System.getProperty("java.home") + "/bin/java";
                /*ProcessBuilder builder = new ProcessBuilder(java, "-jar", userInputs.getUserAppToRun());

                Process process = builder.start();*/
                Process p = Runtime.getRuntime().exec(java +" -jar "+ userInputs.getUserAppToRun());
//                Runtime.getRuntime().exec("java -jar " + userInputs.getUserAppToRun());
                //wait for 5 second until the app is run
                Thread.sleep(5000);
                userInputs.setApplicationName(userInputs.getUserAppToRun().split(" ")[0]);
                userInputs.setPId("");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("Unable to run the Jar file!");
                e.printStackTrace();
            }
        }
        //Find the application pid when user didn't pass the pid
        if (userInputs.getPId() == null || userInputs.getPId() == "") try {
            String process;
            // getRuntime: Returns the runtime object associated with the current Java application.
            // exec: Executes the specified string command in a separate process.
            Process p = Runtime.getRuntime().exec("ps -axo pid,command");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((process = input.readLine()) != null) {
                if (process.contains(userInputs.getApplicationName())) {
                    String[] s = process.split(" ");
                    statistics.setPid(s[0] != "" ? s[0] : s[1]);
                    userInputs.setPId(s[0] != "" ? s[0] : s[1]);
                }
                //TODO use the logger class if there is no process with the given name
            }
            //System.out.println("The Process Id is : " + statistics.getPid());
            input.close();
            p.destroy();
            if(p.isAlive())
                p.destroyForcibly();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void top(int time) {
        CountDownLatch latch = new CountDownLatch(1);
        //a list to hold data from atop command, each batch run is seperated by "new"
        List<Process> topOutputProcessList = new ArrayList<>();
        Integer timer = (time > 0 ? time : Integer.parseInt(samplingTime)); // time to collect atop in second
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                //top -l 2 -pid 66336 -stats cpu >> it is better to set -l to 3.in the man top it is said that son't trust to the first output using -l
                try {
                    Process p = Runtime.getRuntime().exec("top -l " + Integer.parseInt(topCaptureNumber) + " -pid " + statistics.getPid() + " -stats cpu");
                    topOutputProcessList.add(p);

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
            //Only kills the user app when the user give the jar file address to run and test the app
            if(userInputs.getUserAppToRun()!= null){
                try {
                    Runtime.getRuntime().exec("kill "+userInputs.getPId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void parseTopOutputs(List<Process> processes) {
        System.out.println("** Parsing the outputs **");
        int counter = 0;
        statistics.setCpuUsage(new ArrayList<>());
        try {
            for (Process p : processes) {
                String process;
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((process = input.readLine()) != null) {
                    if (process.contains("%CPU")) {
                        counter++;
                        if (counter == Integer.parseInt(topCaptureNumber)) {//to get the last top result
                            Double usage = Double.parseDouble(input.readLine());
                            if (usage > 0) statistics.getCpuUsage().add(usage);
                            counter = 0;
                        }
                    }
                }
                input.close();
                p.destroy();
                if(p.isAlive())
                    p.destroyForcibly();
            }
            averageCpuUsage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double averageCpuUsage() {
        double avgCpu = statistics.getCpuUsage().stream().mapToDouble(d -> d).average().orElse(0.0);
        double avgCpuPerCore = avgCpu / Runtime.getRuntime().availableProcessors();
        if (avgCpuPerCore > Double.parseDouble(cpuIntensiveThreshold)) {
            statistics.setIsCpuIntensive(true);
        } else {
            statistics.setIsCpuIntensive(false);
        }
        return avgCpuPerCore;
    }

    public Statistics findStatistics(int time) {
        top(time);
        return statistics;
    }
}
