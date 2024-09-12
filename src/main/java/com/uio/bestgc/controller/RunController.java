package com.uio.bestgc.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uio.bestgc.model.PollAppResponse;
import com.uio.bestgc.model.ProfileAppRequest;
import com.uio.bestgc.model.ProfileAppResponse;
import com.uio.bestgc.model.RunAppRequest;
import com.uio.bestgc.service.MatrixService;
import com.uio.bestgc.service.ProfileService;
import com.uio.bestgc.service.RunService;

@RestController
public class RunController {

    @Autowired
    ProfileService profileService;

    @Autowired
    RunService runService;

    @Autowired
    MatrixService matrixService;

    public RunController() {
    }

    @GetMapping("/poll")
    public Map<Long, PollAppResponse> pollApps(long[] ids) {
        return runService.pollApps(ids);
    }
}
