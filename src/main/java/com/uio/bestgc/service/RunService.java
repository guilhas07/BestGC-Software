package com.uio.bestgc.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uio.bestgc.model.PollAppResponse;
import com.uio.bestgc.model.RunAppRequest;
import com.uio.bestgc.model.RunAppResponse;

@Service
public class RunService {

    Map<Long, AppInfo> runningApps = new HashMap<>();

    @Autowired
    ProfileService profileService;

    public RunService() {
        // TODO: remove
        runningApps.put(0L, new AppInfo("teste", "teste 1 2 3 4"));
        runningApps.put(1L, new AppInfo("teste2", "giro hello"));
    }

    public RunAppResponse runApp(RunAppRequest runAppRequest, String appPath) {

        try {
            var cmdArray = getExecJarCommand(runAppRequest, appPath);
            Process appProcess = Runtime.getRuntime().exec(cmdArray);

            long pid = appProcess.pid();
            String command = String.join(" ", cmdArray);

            // NOTE: Updating AppInfo if its present due to the operating system guarantee of
            // process Id uniqueness i.e.,
            // a previous app with this same pid is alread finished.
            runningApps.put(pid, new AppInfo( runAppRequest.jar(), command));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new RunAppResponse();
    }

    public Map<Long, AppInfo> getApps() {
        return runningApps;
    }

    public Map<Long, PollAppResponse> pollApps(long[] pids) {
        Map<Long,PollAppResponse> response = new HashMap<>();
        for (long id : pids){
            response.put(id, pollApp(id));
        }
        return response;
    }

    public PollAppResponse pollApp(long pid) {

        if (!runningApps.containsKey(pid)) 
            return null;

        // TODO: remove
        // NOTE: switching to see if working
        if (pid == 1){
            var val = runningApps.get(pid);
            runningApps.put(pid, new AppInfo(val.command(), val.appName()));
        }

        TopCmdResponse top = profileService.executeTop(pid);
        HeapCmdResponse heap = profileService.executeHeapCommand(pid);

        var info = runningApps.get(pid);
        if (top == null || heap == null)
            //TODO: remove
            //runningApps.remove(pid);
            //return null;
            return new PollAppResponse(info.appName(), info.command(), 0, 0, 0);

        //var info = runningApps.get(pid);
        return new PollAppResponse(info.appName(), info.command(), top.cpuUsage(), top.ioTime(), heap.heapSize());
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

record AppInfo(String appName, String command){}
