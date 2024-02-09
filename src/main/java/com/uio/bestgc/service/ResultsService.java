package com.uio.bestgc.service;

import com.uio.bestgc.model.Statistics;
import com.uio.bestgc.model.UserInputs;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class ResultsService extends Profiler {
    List<GcValue> finalOrderedResults = new ArrayList<>();
    private String heap = "";
    private UserInputs userInputs = new UserInputs();
    private Statistics statistics = new Statistics();
    private Map<String, Double> results = new HashMap<>();
    // private List<String> gcs = Arrays.asList("G1", "PS", "Shenandoah", "ZGC");
    private List<String> gcs = Arrays.asList("G1", "PS", "Shenandoah", "Z");
    private String executableJar = "";

    @SuppressWarnings("unchecked")
    public void fetchMatrix(UserInputs ui, Statistics st) {
        userInputs = ui;
        statistics = st;
        System.out.println("Maximum heap used by the user's application: " + statistics.getMaxHeapUsage());
        Double maxHeapUsage = statistics.getMaxHeapUsage() * 1.2 / 1024;
        System.out.println("Heap suggested by BestGC: " + maxHeapUsage);
        // heap = findHeap(Double.parseDouble(ui.getUserAvailableMemory()));
        heap = findHeap(maxHeapUsage);
        // System.out.println("matrix_" + heap);
        String matrixName = "matrix_" + heap;

        Map<String, Double> matrix = new HashMap<>();
        if (st.getIsCpuIntensive()) {
            try {
                Class<CpuIntensiveResultsConstants> cpuClass = CpuIntensiveResultsConstants.class;
                Field matrixField = cpuClass.getDeclaredField(matrixName);

                matrix = (Map<String, Double>) matrixField.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else if (!st.getIsCpuIntensive()) {
            try {
                Class<NonCpuIntensiveResultsConstants> nonCpuClass = NonCpuIntensiveResultsConstants.class;
                Field matrixField = nonCpuClass.getDeclaredField(matrixName);
                matrix = (Map<String, Double>) matrixField.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        calculateResults(matrix, ui.getWeightThroughput(), ui.getWeightPause());
        createOrderedResults();
        executableJar = createExecutionCommand();
        printResults();
        System.out.println("AQUIII");
    }

    public String findHeap(Double heap) {
        List<Integer> heapList = Arrays.asList(256, 512, 1024, 2048, 4096, 8192);
        if (heapList.contains(heap))
            return heap.toString();
        else if (heap <= heapList.get(0))
            return heapList.get(0).toString();
        else if (heap >= heapList.get(heapList.size() - 1))
            return heapList.get(heapList.size() - 1).toString();
        else {
            int i = 0;
            while (i < heapList.size()) {
                if (heap > heapList.get(i))
                    i++;
                else
                    break;
            }
            return heapList.get(i).toString();
        }
    }

    public void calculateResults(Map<String, Double> matrix, Double weightThroughput, Double weightPause) {
        for (String gc : gcs) {
            String t = gc + "_throughput";
            String p = gc + "_pause";

            Double x = (matrix.get(t.toLowerCase()) * weightThroughput)
                    + (matrix.get(p.toLowerCase()) * weightPause);

            results.put(gc, x);
        }
    }

    public void createOrderedResults() {
        Map<String, Double> r = results;
        results.forEach((key, value) -> System.out.println("Resultado: Key: " + key + "Value: " + value));

        String gc = r.keySet().toArray()[0].toString();
        Double min = r.get(gc);
        for (Map.Entry e : r.entrySet()) {
            if ((Double) e.getValue() < min) {
                min = (Double) e.getValue();
                gc = e.getKey().toString();
            }
        }
        finalOrderedResults.add(new GcValue(gc, min));
        if (r.entrySet().size() > 1) {
            r.remove(gc);
            createOrderedResults();
        }
    }

    public void printResults() {
        // System.out.println(finalOrderedResults);
        List<String> printList = new ArrayList<>();
        String str1 = "User's application with PID " + userInputs.getPId() + " is running on " +
                userInputs.getUserOS() + " with " + Runtime.getRuntime().availableProcessors() + " CPU cores.";
        System.out.println(str1);
        printList.add(str1);
        String str2 = "The software is " + (statistics.getIsCpuIntensive() ? "CPU-Intensive" : "Not CPU-Intensive");
        System.out.println(str2);
        printList.add(str2);
        String str3 = "Throughput Weight is: " + userInputs.getWeightThroughput();
        System.out.println(str3);
        printList.add(str3);
        String str4 = "Pause Time Weight is: " + userInputs.getWeightPause();
        System.out.println(str4);
        printList.add(str4);
        String str5 = "Available memory is: " + userInputs.getUserAvailableMemory();
        System.out.println(str5);
        printList.add(str5);
        String str6 = "Recommended GC is: " + finalOrderedResults.get(0).getGc();
        System.out.println(str6);
        printList.add(str6);
        // profileLogs("avg-cpu", userInputs.getUserAppToRun().split(" ")[1],
        // printList);
        finalOrderedResults.clear();
    }

    public String createExecutionCommand() {
        StringBuilder executableCommand = new StringBuilder();
        String java = System.getProperty("java.home") + "/bin/java";
        System.out.println("Giro: " + finalOrderedResults.get(0));
        System.out.println("Giro 2: " + finalOrderedResults.get(0).getGc());
        // String gc = finalOrderedResults.get(0).getGc().equals("ZGC") ? "ZGC"
        // : finalOrderedResults.get(0).getGc() + "GC";
        String gc = finalOrderedResults.get(0).getGc() + "GC";
        String appToRun = "";
        if (statistics.getPid() != null) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("bash", "-c",
                        "jcmd | grep \":" + statistics.getPid() + "\" | awk '{print $2}' ");
                Process getApp = null;

                getApp = processBuilder.start();

                appToRun = new BufferedReader(new InputStreamReader(getApp.getInputStream())).readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String app = userInputs.getUserAppToRun() != "" ? userInputs.getUserAppToRun() : appToRun;
        Double maxHeap = (statistics.getMaxHeapUsage() * 1.2) / 1024;

        executableCommand.append(java).append(" ").append("-Xmx").append(findHeap(maxHeap))
                .append("m ").append("-XX:+Use").append(gc).append(" -jar ").append(app);

        return executableCommand.toString();
    }

    public String getExecutableJar() {
        return executableJar;
    }

    @Getter
    @Setter
    class GcValue {
        String gc;
        Double value;

        public GcValue(String gc, Double value) {
            this.gc = gc;
            this.value = value;
        }
    }
}
