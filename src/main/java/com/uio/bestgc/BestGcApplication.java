package com.uio.bestgc;

import com.uio.bestgc.controller.MainController;
import com.uio.bestgc.model.PerformanceMetric;
import com.uio.bestgc.model.UserInputs;
import com.uio.bestgc.service.OldResultsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class BestGcApplication implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BestGcApplication.class);
    private UserInputs userInputs;
    @Autowired
    private MainController mainController;

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(BestGcApplication.class, args);
        run.close();
        //MainController bean = run.getBean(MainController.class);
    }

    public static void logError(String logging) {
        logger.error("\u001B[31m" + logging + "\u001B[0m");
    }
//    public static void endRun(){
//        SpringApplication.exit(context, new ExitCodeGenerator() {
//            @Override
//            public int getExitCode() {
//                return 0;
//            }
//        });
//    }


    @Override
    public void run(ApplicationArguments args) throws Exception {

        userInputs = new UserInputs();
        userInputs.setUserOS(System.getProperty("os.name"));
        System.out.println(args.getOptionNames());
        if (args.getOptionNames().size() > 0) {
            List<String> optionNames = args.getOptionNames().stream().map(a -> a.toLowerCase()).collect(Collectors.toList());
            if (optionNames.contains("name"))
                userInputs.setApplicationName(args.getOptionValues("name").get(0));
            if (optionNames.contains("user-app"))
                userInputs.setUserAppToRun(args.getOptionValues("user-app").get(0));
            else if (!optionNames.contains("user-app"))
                logError("Path to the user's app should be defined.");
            /*if (optionNames.contains("metric")) {
                switch (args.getOptionValues("metric").get(0).toLowerCase()) {
                    case "t":
                        userInputs.setMetric(PerformanceMetric.THROUGHPUT);
                        break;
                    case "m":
                        userInputs.setMetric(PerformanceMetric.MEMORYUSAGE);
                        break;
                    case "p":
                        userInputs.setMetric(PerformanceMetric.PAUSETIME);
                        break;
                    case "a":
                        userInputs.setMetric(PerformanceMetric.ALL);
                        break;
                    default:
                        userInputs.setMetric(PerformanceMetric.ALL);
                }
            }
            else {
                userInputs.setMetric(PerformanceMetric.ALL);
            }*/
            Double wt =0D;
            Double wp= 0D;
            if (optionNames.contains("we")) {
                 wt = Double.parseDouble(args.getOptionValues("we").get(0).toString());
                if (wt > 1D) {
                    logError("Weight for Throughput should be between 0 and 1");
                    return;
                } else {
                    userInputs.setWeightThroughput(wt);
                    Float f= 1- Double.valueOf(wt).floatValue();

                    userInputs.setWeightPause(Double.parseDouble(f.toString()));
                }
            }
            if (optionNames.contains("wp")) {
                wp = Double.parseDouble(args.getOptionValues("wp").get(0).toString());
                if(wt >0D){
                    if(wt+wp!= 1D){
                        logError("Weight for Pause Time is calculated based on Wt, it is:" + wp);
                        return;
                    }
                } else if (wt == 0D){
                    if (wp > 1D) {
                        logError( "Weight for Pause Time should be between 0 and 1");
                        return;
                    } else {
                        userInputs.setWeightPause(wp);
                        Float f= 1- Double.valueOf(wp).floatValue();
                        userInputs.setWeightThroughput(Double.parseDouble(f.toString()));
                    }
                }

            }
            if (wt == 0D && wp == 0D){
                logError("Wp or Wt is not defined.");
                return;
            }
            if (optionNames.contains("memory"))
                userInputs.setUserAvailableMemory(args.getOptionValues("memory").get(0));

            if (optionNames.contains("monitoring-time")) {
                userInputs.setSamplingTime(Integer.parseInt(args.getOptionValues("monitoring-time").get(0)));
            } else
                userInputs.setSamplingTime(0);
            if (optionNames.contains("pid")) {
                userInputs.setPId(args.getOptionValues("pid").get(0));
            }
            if (optionNames.contains("run-best-gc")) {
                userInputs.setRunAppWithBestGC(Boolean.valueOf(args.getOptionValues("run-best-gc").get(0)));
            }

            mainController.main(userInputs);
        } else logger.error("Please pass the software's name and desired performance metric to the application.");

    }
}
