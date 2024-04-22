package com.uio.bestgc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
import com.uio.bestgc.service.MainService;

@Controller
public class MainController {

    @Autowired
    MainService mainService;

    @Value("${monitoring-time}")
    int monitoringTime;

    public MainController() {
    }

    @GetMapping("/")
    public String index(Model model) {
        System.out.println(monitoringTime);
        model.addAttribute("profile", new ProfileAppRequest(1, 0, monitoringTime, null, null, null));
        model.addAttribute("jars", mainService.getJars());
        return "index";
    }

    @PostMapping(value = "/profile_app", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String profileApplication(@ModelAttribute ProfileAppRequest profileRequest, Model model) {
        System.out.println("AQUIIIIIIIIIII");
        System.out.println(profileRequest);
        try {
            var file = profileRequest.file();
            var dest = Paths.get("jars").resolve(profileRequest.jar());
            if (!file.isEmpty()) {
                dest = Paths.get("jars").resolve(file.getOriginalFilename());
                Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            }

            var response = mainService.profileApp(profileRequest, dest.toString());
            model.addAttribute("profileAppResponse", response);
            return "profileApp";

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
