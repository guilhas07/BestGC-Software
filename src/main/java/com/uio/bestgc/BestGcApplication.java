package com.uio.bestgc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.uio.bestgc.model.ProfileAppRequest;
import com.uio.bestgc.service.MainService;

@SpringBootApplication
public class BestGcApplication {

    @Autowired
    private MainService mainService;

    @Value("${monitoring-time}")
    private int monitoringTime;

    public static void main(String... args) {
        var env = SpringApplication.run(BestGcApplication.class, args).getEnvironment();
        System.out.println("message from application.properties "
                + env.getProperty("spring.thymeleaf.prefix"));

        System.out.println("GIRO "
                + env.getProperty("classpath"));
    }

    @Component
    @ConditionalOnNotWebApplication
    class ConsoleRunner implements CommandLineRunner {

        @Override
        public void run(String... args) {
            // Implement your console runner logic here
            // java -jar BestGC.jar pathToJar --wp="weight for pause time"
            // its input options" --monitoring-time=40
            // --wp="weight for pause time

            // System.out.printf("prefix: {}", env.get("spring.thymeleaf.prefix"));
            // System.out.printf("monitor: {}", env.getProperty("monitoring-time"));
            if (args.length < 2) {
                System.out.println("Please specify the application jar and the --wp or --wt");
                return;
            }

            String pathToJar = args[0];
            float throughputWeight = -1;
            float pauseTimeWeight = -1;
            String jarArgs = "";

            for (int i = 1; i < args.length; i++) {
                switch (args[i]) {
                    // case String str when str.contains("--wt"):
                    case String s when s.contains("--wt=") -> {
                        throughputWeight = Float.valueOf(s.substring(5));
                        pauseTimeWeight = 1 - throughputWeight;
                    }
                    case String s when s.contains("--wp=") -> {
                        pauseTimeWeight = Float.valueOf(s.substring(5));
                        throughputWeight = 1 - pauseTimeWeight;
                    }
                    case String s when s.contains("--monitoringTime=") ->
                        monitoringTime = Integer.valueOf(s.substring("monitoringTime".length()));
                    case String s when s.contains("--args=") ->
                        jarArgs = s.substring("--args=".length());
                    default -> System.out.println("Option " + args[i] + " not recognized");
                }
            }

            if (pauseTimeWeight < 0 || throughputWeight < 0 || throughputWeight + pauseTimeWeight != 1) {
                System.out.println("The sum of throughputWeight and pauseTimeWeight should be equal to 1");
                return;
            }

            var response = mainService.profileApp(
                    new ProfileAppRequest(throughputWeight, pauseTimeWeight, monitoringTime, pathToJar, jarArgs, null),
                    pathToJar);
            System.out.println(response);
        }
    }
}
