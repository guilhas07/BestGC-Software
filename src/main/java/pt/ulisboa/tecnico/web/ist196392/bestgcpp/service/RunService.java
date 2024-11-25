package pt.ulisboa.tecnico.web.ist196392.bestgcpp.service;

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

import pt.ulisboa.tecnico.web.ist196392.bestgcpp.model.PollAppResponse;
import pt.ulisboa.tecnico.web.ist196392.bestgcpp.model.RunAppRequest;
import pt.ulisboa.tecnico.web.ist196392.bestgcpp.model.RunAppResponse;

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

    public RunAppResponse runApp(RunAppRequest runAppRequest) {

        try {
            var cmdArray = getExecJarCommand(runAppRequest);
            Process appProcess = Runtime.getRuntime().exec(cmdArray);

            long pid = appProcess.pid();
            String command = String.join(" ", cmdArray);

            // NOTE: Updating AppInfo if its present due to the operating system guarantee
            // of
            // process Id uniqueness i.e.,
            // a previous app with this same pid is alread finished.
            runningApps.put(pid, new AppInfo(runAppRequest.jar(), command));

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
        Map<Long, PollAppResponse> response = new HashMap<>();
        for (long id : pids) {
            response.put(id, pollApp(id));
        }
        return response;
    }

    public PollAppResponse pollApp(long pid) {

        if (!runningApps.containsKey(pid))
            return null;

        // TODO: remove
        // NOTE: switching to see if working
        if (pid == 1) {
            var val = runningApps.get(pid);
            runningApps.put(pid, new AppInfo(val.command(), val.appName()));
        }

        TopCmdResponse top = profileService.executeTop(pid);
        HeapCmdResponse heap = profileService.executeHeapCommand(pid);

        var info = runningApps.get(pid);
        if (top == null || heap == null)
            // TODO: remove
            // runningApps.remove(pid);
            // return null;
            return new PollAppResponse(info.appName(), info.command(), 0, 0, 0);

        // var info = runningApps.get(pid);
        return new PollAppResponse(info.appName(), info.command(), top.cpuUsage(), top.ioTime(), heap.heapSize());
    }

    private String[] getExecJarCommand(RunAppRequest request) {
        // TODO: can use string builder
        String command = "java";
        String gc = request.gc();
        command += gc != null ? " -XX:+Use" + gc + "GC" : "";

        command += " -Xms" + request.heapSize() + "m";
        command += " -Xmx" + request.heapSize() + "m";

        // enable log
        System.out.println("Jar: " + request.jar());

        var index = request.jar().lastIndexOf(".");

        command += " -Xlog:gc*,safepoint:file=" + request.jar().substring(0, index) + ".log::filecount=0";
        command += " -jar " + "jars/" + request.jar() + " " + request.args();

        System.out.println("Command: " + command);
        return command.split(" ");
    }

}

record AppInfo(String appName, String command) {
}
