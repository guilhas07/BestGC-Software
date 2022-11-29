package com.uio.bestgc.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class Profiler {
    public void profileLogs(String fileName, String appName, List<String> printList) {
        String userAppName = appName;
        if (appName.contains("/")) {
            String[] s = appName.split("/");
            int length = s.length;
            String s1 = s[length - 1];
            if (s1.contains("."))
                userAppName = s1.split("\\.")[0];
            else
                userAppName = s1;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String fileDirectory = System.getProperty("user.dir") + "/resultsBestGC";
        String file = fileName + " " + LocalDateTime.now().format(formatter) + ".txt";
        Path path = Paths.get(fileDirectory + "/" + userAppName + "/" + file);
        try {
            if (!Files.exists(path)) {
                if (!Files.isDirectory(Paths.get(fileDirectory)))
                    Files.createDirectory(Paths.get(fileDirectory));
                if (!Files.isDirectory(Paths.get(fileDirectory + "/" + userAppName)))
                    Files.createDirectory(Paths.get(fileDirectory + "/" + userAppName));
                //Files.createFile(path);
            }
            Files.write(path, printList, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
