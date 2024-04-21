package com.uio.bestgc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uio.bestgc.model.ProfileAppRequest;
import com.uio.bestgc.model.ProfileAppResponse;
import com.uio.bestgc.service.MainService;

@Controller
public class MainController {

    private static final String PROFILE_ENDPOINT = "/profile_app";

    @Autowired
    MainService mainService;

    public MainController() {
    }

    @GetMapping("/")
    public String index() {
        return "index";
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
