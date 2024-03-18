package com.uio.bestgc.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import com.uio.bestgc.model.Statistics;
import com.uio.bestgc.model.UserInputs;
import com.uio.bestgc.service.MainService;
import com.uio.bestgc.service.MatrixService;
import com.uio.bestgc.service.ResultsService;

@RestController
public class MainController {
    private final MainService mainService;
    private final ResultsService resultsService;
    private final ApplicationContext context;
    private final MatrixService matrixService;

    public MainController(MainService mainService, MatrixService matrixService, ResultsService resultsService1,
            ApplicationContext context) {
        this.mainService = mainService;
        this.matrixService = matrixService;
        this.resultsService = resultsService1;
        this.context = context;
    }

    public void main(UserInputs inputs) {
        mainService.run(inputs);
        // run the command
        Statistics statistics = mainService.findStatistics(inputs.getSamplingTime());
        System.out.println(
                "The average CPU usage per core by the user's application is: " + statistics.getAvgCpuPerCore() + "%");
        // resultsService.fetchMatrix(inputs, statistics);
        Double maxHeapUsage = statistics.getMaxHeapUsage() * 1.2 / 1024;
        System.out.println("Heap suggested by BestGC: " + maxHeapUsage);
        String heap = resultsService.findHeap(maxHeapUsage);

        String gc = this.matrixService.getBestGC(statistics.getIsCpuIntensive(), heap, inputs.getWeightThroughput(),
                inputs.getWeightPause());

        // heap = findHeap(Double.parseDouble(ui.getUserAvailableMemory()));

        String executableJar = resultsService.getExecutableJar(gc, heap, inputs.getUserAppToRun());
        System.out.println(executableJar);
        if (inputs.getRunAppWithBestGC() == null || inputs.getRunAppWithBestGC()) {
            try {
                mainService.getUserappProcess().destroy();
                System.out.println("Running app with the best GC...");
                System.out.println("Executing: " + executableJar);

                Process userAppWithBestGc = Runtime.getRuntime().exec(executableJar.split(" "));

                InputStream is = userAppWithBestGc.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int exitCode = userAppWithBestGc.waitFor();
                System.out.println("Process exited with code " + exitCode);
            } catch (InterruptedException | IOException e) {
                System.out.println("EXCEPTION");
                e.printStackTrace();
            }
        } else {
            System.out.println("The command to run user app with the best GC is: ");
            System.out.println(executableJar);
            SpringApplication.exit(context, new ExitCodeGenerator() {
                @Override
                public int getExitCode() {
                    return 0;
                }
            });
        }
    }
}
