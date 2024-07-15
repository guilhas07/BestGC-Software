package com.uio.bestgc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.uio.bestgc.model.ProfileAppRequest;
import com.uio.bestgc.model.ProfileAppResponse;
import com.uio.bestgc.model.RunAppRequest;
import com.uio.bestgc.service.MatrixService;
import com.uio.bestgc.service.ProfileService;
import com.uio.bestgc.service.RunService;

@Controller
public class MainController {

    @Autowired
    ProfileService profileService;

    @Autowired
    RunService runService;

    @Autowired
    MatrixService matrixService;

    @Value("${monitoring-time}")
    int monitoringTime;

    public MainController() {
    }

    @GetMapping("/")
    public String index(Model model) {
        System.out.println(monitoringTime);
        model.addAttribute("profile", new ProfileAppRequest(true, true, 1, 0, monitoringTime, null, null, null));
        model.addAttribute("jars", profileService.getJars());
        return "index";
    }

    @PostMapping(value = "/profile_app", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String profileApplication(@ModelAttribute ProfileAppRequest profileRequest, Model model) {
        // try {
        // Thread.sleep(20_000);
        // return "giro";
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        System.out.println(profileRequest);
        try {
            var file = profileRequest.file();
            var dest = Paths.get("jars").resolve(profileRequest.jar());
            if (file != null && !file.isEmpty()) {
                dest = Paths.get("jars").resolve(file.getOriginalFilename());
                Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            }

            // TODO: uncomment
            // var response = profileService.profileApp(profileRequest, dest.toString());
            ProfileAppResponse response = null;
            // TODO: handle null response

            if (profileRequest.runApp()) {
                // var myobj = new Object() {
                // public String teste = "Boas";
                // };
                // System.out.println("Teste antes: " + myobj.teste);
                // var t = Thread.startVirtualThread(() -> {
                // myobj.teste = "fds";
                // // profileService.runApp(new RunAppRequest(response.bestGC(), null,
                // // response.heapSize(), null, null,
                // // profileRequest.args(), true, null), dest.toString());
                // System.out.println("Teste dentro: " + myobj.teste);
                //
                // });
                // t.join();
                // System.out.println("Teste fora: " + myobj.teste);
            }

            // model.addAttribute("profileAppResponse", response);
            // model.addAttribute("gcs", profileService.getAvailableGCs());
            // model.addAttribute("gc", response.bestGC());
            // model.addAttribute("runAppRequest",
            // new RunAppRequest(null, null, response.heapSize(), null, null,
            // profileRequest.args(), false, null));
            // model.addAttribute("jars",
            // Arrays.asList(mainService.getJars()).stream().filter(s -> s !=
            // profileRequest.jar()));
            // model.addAttribute("jars", profileService.getJars());
            // model.addAttribute("jar", profileRequest.jar());
            return "fragments/profileApp";

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping(value = "/run_app")
    public String runApp(Model model) {
        model.addAttribute("run", new RunAppRequest(null, null, null, 0, null));
        model.addAttribute("gcs", profileService.getGCs());
        model.addAttribute("jars", profileService.getJars());
        model.addAttribute("heapSizes", matrixService.getHeapSizes());
        return "runApp";
    }

    @PostMapping(value = "/run_app", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String runApplication(@ModelAttribute RunAppRequest runAppRequest, Model model) {
        System.out.println(runAppRequest);

        var file = runAppRequest.file();
        var dest = Paths.get("jars").resolve(runAppRequest.jar());
        if (file != null && !file.isEmpty()) {
            dest = Paths.get("jars").resolve(file.getOriginalFilename());
            try {
                Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        var response = runService.runApp(runAppRequest, dest.toString());
        model.addAttribute("runAppResponse", response);
        return "runAppResponse";
    }

}
