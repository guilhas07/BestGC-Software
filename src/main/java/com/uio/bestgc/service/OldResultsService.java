package com.uio.bestgc.service;

import com.uio.bestgc.model.PerformanceMetric;
import com.uio.bestgc.model.Statistics;
import com.uio.bestgc.model.UserInputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OldResultsService extends Profiler {
    UserInputs userInputs = new UserInputs();
    Statistics statistics = new Statistics();
//    Map<String, String> bestGC = new HashMap<>(CpuIntensiveResultsConstants.cpuIntensiveResults.size());
    private static final Logger logger = LoggerFactory.getLogger(OldResultsService.class);

    public static final String WHITE_BACKGROUND = "\u001B[47m";
    public static final String GREEN = "\033[0;32m";
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    public void findResults(UserInputs userInputs, Statistics statistics) {
        this.userInputs = userInputs;
        this.statistics = statistics;

        /*if (statistics.getIsCpuIntensive()) {
            if (!userInputs.getMetric().equals(PerformanceMetric.ALL))
                bestGC.put(userInputs.getMetric().getLabel()
                        , CpuIntensiveResultsConstants.cpuIntensiveResults.get(userInputs.getMetric().name().toLowerCase()));
            else
                bestGC.putAll(CpuIntensiveResultsConstants.cpuIntensiveResults);
        } else {
            if (!userInputs.getMetric().equals(PerformanceMetric.ALL))
                bestGC.put(userInputs.getMetric().getLabel()
                        , CpuIntensiveResultsConstants.nonCpuIntensiveResults.get(userInputs.getMetric().name().toLowerCase()));
            else
                bestGC.putAll(CpuIntensiveResultsConstants.nonCpuIntensiveResults);
        }
        printResults();*/
    }

    public void printResults() {
        List<String> printList = new ArrayList<>();
        /*String str1 = "User's application with PID " + GREEN + userInputs.getPId() *//*userInputs.getApplicationName()*//* + RESET + " is running on " +
                GREEN + userInputs.getUserOS() + RESET + " with " + GREEN + Runtime.getRuntime().availableProcessors() + RESET + " CPU cores.";
        System.out.println(str1);*/
        printList.add("User's application with PID " + userInputs.getPId() + " is running on " +
                userInputs.getUserOS() + " with " + Runtime.getRuntime().availableProcessors() + " CPU cores.");

        /*String str2 = "The software is " + RED + (statistics.getIsCpuIntensive() ? "CPU-Intensive" : "Not CPU-Intensive");
        System.out.println(str2);*/
        printList.add("The software is " + (statistics.getIsCpuIntensive() ? "CPU-Intensive" : "Not CPU-Intensive"));

        /*if (!userInputs.getMetric().equals(PerformanceMetric.ALL)) {
            String str3 = RESET + "Recommended GC regarding " + GREEN + userInputs.getMetric().getLabel() + RESET + " is: "
                    + RED + bestGC.get(userInputs.getMetric().getLabel()) + RESET;
            System.out.println(str3);
            printList.add("Recommended GC regarding " + userInputs.getMetric().getLabel() + " is: "
                    + bestGC.get(userInputs.getMetric().getLabel()));
        } else {
            String str4 = RESET + "Recommended GCs regarding Throughput, Pause Time, and Memory Usage are: ";
            System.out.println(str4);
            printList.add("Recommended GCs regarding Throughput, Pause Time, and Memory Usage are: ");
            for (Map.Entry<String, String> e : bestGC.entrySet()) {
                System.out.println(GREEN + e.getKey() + ": "
                        + RED + e.getValue() + RESET);
                printList.add(e.getKey() + ": " + e.getValue());
            }
        }*/
//        profileLogs("avg-cpu", userInputs.getApplicationName() != null ? userInputs.getApplicationName() : "", printList);
        profileLogs("avg-cpu", userInputs.getUserAppToRun().split(" ")[1], printList);
    }
}
