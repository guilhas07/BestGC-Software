package com.uio.bestgc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uio.bestgc.model.ProfileAppRequest;
import com.uio.bestgc.model.ProfileAppResponse;
import com.uio.bestgc.service.MainService;

@RestController
public class MainController {

    private static final String PROFILE_ENDPOINT = "/profile_app";

    @Autowired
    MainService mainService;

    public MainController() {
    }

    @GetMapping("/")
    public String home() {
        return STR."""
                <html>
                <head>
                    <title>Parameter Form</title>
                </head>
                <body>
                    <h2>Enter Parameters</h2>
                    <form action="\{PROFILE_ENDPOINT}" method="POST" enctype="multipart/form-data">
                        <label for="throughput_weight">Throughput Weight:</label>
                        <input type="number" id="throughput_weight" min="0" max="1" step="0.01" name="throughputWeight" required><br><br>

                        <label for="pause_time_weight">Pause Time Weight:</label>
                        <input type="number" id="pause_time_weight" min="0" max="1" step="0.01" name="pauseTimeWeight" required><br><br>

                        <label for="file">App jar:</label>
                        <input type="file" id="file" name="file" accept=".jar" required><br><br>

                        <label for="app_args">App arguments:</label>
                        <input type="text" id="app_args" name="args"><br><br>

                        <label for="monitoring_time">Monitoring Time:</label>
                        <input type="text" id="monitoring_time" name="monitoringTime" required><br><br>

                        <input type="submit" value="Submit">
                    </form>
                </body>
                </html>
                """;
    }

    @PostMapping(value = PROFILE_ENDPOINT, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ProfileAppResponse profileApplication(@ModelAttribute ProfileAppRequest profileRequest) {
        System.out.println(profileRequest);
        try {
            var file = profileRequest.file();
            var dest = Paths.get("jars").resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            return mainService.profileApp(profileRequest, dest.toString());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
