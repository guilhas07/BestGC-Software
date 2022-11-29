package com.uio.bestgc.controller;

import com.uio.bestgc.model.Statistics;
import com.uio.bestgc.model.UserInputs;
import com.uio.bestgc.service.MainService;
import com.uio.bestgc.service.OldResultsService;
import com.uio.bestgc.service.ResultsService;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class MainController {
    private final MainService mainService;
    private final OldResultsService oldResultsService;
    private final ResultsService resultsService;
    private final ApplicationContext context;

    public MainController(MainService mainService, OldResultsService resultsService, ResultsService resultsService1, ApplicationContext context) {
        this.mainService = mainService;
        this.oldResultsService = resultsService;
        this.resultsService = resultsService1;
        this.context = context;
    }

    /*@GetMapping("/hello")
    public void aa(){
        UserInputs ui = new UserInputs();
        ui.setWeightPause(0.9);
        ui.setWeightThroughput(0.1);
        ui.setUserAvailableMemory("2048");
        Statistics s= new Statistics();
        s.setIsCpuIntensive(true);
        resultsService.fetchMatrix(ui,s);
    }*/
    public void main(UserInputs inputs) {
        mainService.run(inputs);
        //run the command
        Statistics statistics = mainService.findStatistics(inputs.getSamplingTime());
        System.out.println("The average CPU usage per core by the user's application is: " + statistics.getAvgCpuPerCore() + "%");
//        oldResultsService.findResults(inputs, statistics);
        resultsService.fetchMatrix(inputs, statistics);
        String executableJar = resultsService.getExecutableJar();
        if (inputs.getRunAppWithBestGC() == null || inputs.getRunAppWithBestGC()) {
            try {
                mainService.getUserappProcess().destroy();
                System.out.println("Running app with the best GC...");
                Process userAppWithBestGc = Runtime.getRuntime().exec(executableJar);
            } catch (IOException e) {
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




