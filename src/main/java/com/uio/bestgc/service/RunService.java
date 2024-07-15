package com.uio.bestgc.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.uio.bestgc.model.RunAppRequest;
import com.uio.bestgc.model.RunAppResponse;

@Service
public class RunService {

    Map<UUID, AppData> runningAppsData = new ConcurrentHashMap<>();

    Object lock = new Object();

    public RunService() {
    }

    public RunAppResponse runApp(RunAppRequest runAppRequest, String appPath) {

        var id = java.util.UUID.randomUUID();
        var currentValue = runningAppsData.putIfAbsent(id, new AppData());

        // NOTE: Handle *extremely* rare case of a possible UUID collision
        while (currentValue != null) {
            id = UUID.randomUUID();
            currentValue = runningAppsData.putIfAbsent(id, new AppData());
        }

        Thread.startVirtualThread(() -> {
            try {
                Process appProcess = Runtime.getRuntime().exec(getExecJarCommand(runAppRequest, appPath));

                while (appProcess.isAlive()) {

                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        return new RunAppResponse();
    }

    private String[] getExecJarCommand(RunAppRequest request, String appPath) {
        // TODO: use string builder
        String command = "java";
        String gc = request.gc();
        command += gc != null ? " -XX:+Use" + gc + "GC" : "";

        command += " -Xms" + request.heapSize() + "m";
        command += " -Xmx" + request.heapSize() + "m";

        // enable log
        String baseName = Paths.get(appPath).getFileName().toString().split(".")[0];
        System.out.println("File name: " + baseName);
        command += " -Xlog:gc*,safepoint:file=" + baseName + ".log::filecount=0";

        command += " -jar " + appPath + " " + request.args();

        System.out.println("Command: " + command);
        return command.split(" ");
    }

}

class AppData {
    List<Float> cpuUsage = new ArrayList<>();
    List<Float> ioTime = new ArrayList<>();
    boolean isRunning = true;
    int returnCode = -1;

    public AppData() {
    }
}
