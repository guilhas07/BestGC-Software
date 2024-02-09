package com.uio.bestgc.service;

import com.uio.bestgc.model.Stat;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EngagedCoresService extends Profiler {
    List<Stat> cpuStatList = new ArrayList<>();
    List<List<Double>> coreCpuUsage = new ArrayList<>();// a list of cpu cores, each entry has a list that keeps the cpu
                                                        // usage per core each second
    List<Integer> engagedCoresPerSecond = new ArrayList<>();
    private String userAppName;
    List<String> printList = new ArrayList<>();

    public void captureCpu() {
        printList.add("***************" + userAppName != null ? userAppName : "" + "***************");
        try {
            List<String> cpuPerCores = new ArrayList<>();
            Process p = Runtime.getRuntime().exec("cat /proc/stat");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str;
            while ((str = input.readLine()) != null) {
                if (str.contains("cpu")) {
                    cpuPerCores.add(str);
                } else
                    break;
            }
            calculateCpu(cpuPerCores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long finalAvgEngagedCores() {
        //engagedCoresPerSecond.remove(0); //it is the record for initializing and it is 0
        double cores = engagedCoresPerSecond.stream().mapToInt(a -> a).average().orElse(0);
        //System.out.println("The number of engaged cores:" + cores);
        Long c = Math.round(cores);//it rounds the results
        printList.add("");
        printList.add("Number of engaged cores by the user's application is :" + cores + " ~" + c);

        // FIX: this only handles apps with the followign structure: ./app benchmark name options...
        //                                                           ./app
        // Meaning ./app options... will generate a log folder with the first option.
        String[] s = userAppName.split(" ");
        profileLogs("cpu-per-core", s.length == 1 ? s[0] : s[1], printList);
        return c;
    }

    private void calculateCpu(List<String> cpuUsage) {
        printList.add("************************");
        printList.add("Start recording at " + LocalDateTime.now());
        // skip the cpuUsage[0] since it is the record related to the total cpu usage
        // not each core
        cpuUsage.remove(0);
        // below loop creates CPU stat list for all the cores
        if (cpuStatList.size() == 0) {
            for (int i = 0; i < cpuUsage.size(); i++) {
                String[] s = cpuUsage.get(i).split(" ");
                Stat stat = new Stat();
                stat.setCpuNum(i);
                // createOrUpdateStats splits the string from cat /proc/stat cpu lines and puts
                // them into the Stat fields
                cpuStatList.add(createOrUpdateStats(stat, s));
                double newCpuSum = cpuStatList.get(i).getUser() + cpuStatList.get(i).getNice()
                        + cpuStatList.get(i).getSystem() +
                        cpuStatList.get(i).getIowait() + cpuStatList.get(i).getIrq() + cpuStatList.get(i).getSoftirq() +
                        cpuStatList.get(i).getSteal() + cpuStatList.get(i).getGuest()
                        + cpuStatList.get(i).getGuestNice() + cpuStatList.get(i).getIdle();
                cpuStatList.get(i).setLastCpuSum(newCpuSum);
                cpuStatList.get(i).setLastIdel(cpuStatList.get(i).getIdle());
            }
        } else {
            for (int j = 0; j < cpuUsage.size(); j++) {
                String[] s = cpuUsage.get(j).split(" ");
                Stat stat = new Stat();
                // it gets the stats for CPU core j
                Stat st = cpuStatList.get(j);
                // it puts new values for each CPU core not the LastCpuSum and lastCpuIdle
                createOrUpdateStats(st, s);
            }
            // now updating LastCpuSum and lastCpuIdle and getting the differences to
            // calculate CPU usage at this second compared to the previous second
            for (Stat stat : cpuStatList) {
                double newCpuSum = 0;
                newCpuSum = stat.getUser() + stat.getNice() + stat.getSystem() + stat.getIowait() + stat.getIrq()
                        + stat.getSoftirq() +
                        stat.getSteal() + stat.getGuest() + stat.getGuestNice() + stat.getIdle();
                if (stat.getLastCpuSum() > 0) {
                    double cpuSumDelta = newCpuSum - stat.getLastCpuSum(); // difference between new cpu sum and the
                                                                           // last one
                    double cpuIdleDelta = stat.getIdle() - stat.getLastIdel();
                    if (cpuSumDelta != 0)
                        stat.setCpuUsage(100 * (cpuSumDelta - cpuIdleDelta) / cpuSumDelta);
                    else
                        stat.setCpuUsage(0);
                }
                stat.setLastCpuSum(newCpuSum);
                stat.setLastIdel(stat.getIdle());
            }
        }
        int engagedCores = 0;
        for (int j = 0; j < cpuStatList.size(); j++) {
            printList.add(
                    "core " + cpuStatList.get(j).getCpuNum() + " CPU usage: " + cpuStatList.get(j).getCpuUsage() + "%");
            // System.out.println("core " + cpuStatList.get(j).getCpuNum() + " CPU usage: "
            // + cpuStatList.get(j).getCpuUsage() + "%");
            // to add cpu usage for every iteration
            /*
             * if (coreCpuUsage.size() < cpuStatList.size()) {//if the list is empty
             * List<Double> eachCoreUsage = new ArrayList<>();
             * eachCoreUsage.add(cpuStatList.get(j).getCpuUsage());
             * coreCpuUsage.add(eachCoreUsage);
             * } else if (coreCpuUsage.size() > 0) {//add new cpu usage to the previous ones
             * double currentCpuUsage = cpuStatList.get(j).getCpuUsage();
             * coreCpuUsage.get(j).add(currentCpuUsage);
             * // if the current cpu usage in core# is more than 50%,
             * // then increase the number of engaged cores and add it to the list of number
             * of engaged cores per second
             * if (currentCpuUsage > 50) {
             * engagedCores++;
             * }
             * }
             * }
             * if (coreCpuUsage.size() > 1)//the first one is not valid because it is just
             * initializing the list
             * engagedCoresPerSecond.add(engagedCores);// they engaged cores may be
             * increased if there are any cores used more than 50% or it can be zero if any
             * is used
             */
            if (cpuStatList.get(j).getCpuUsage() > 50) {
                engagedCores++;
            }
        }
        engagedCoresPerSecond.add(engagedCores);
    }

    public Stat createOrUpdateStats(Stat stat, String[] s) {
        stat.setUser(s[1] != null ? Double.parseDouble(s[1]) : 0);
        stat.setNice(s[2] != null ? Double.parseDouble(s[2]) : 0);
        stat.setSystem(s[3] != null ? Double.parseDouble(s[3]) : 0);
        stat.setIdle(s[4] != null ? Double.parseDouble(s[4]) : 0);
        stat.setIowait(s[5] != null ? Double.parseDouble(s[5]) : 0);
        stat.setIrq(s[6] != null ? Double.parseDouble(s[6]) : 0);
        stat.setSoftirq(s[7] != null ? Double.parseDouble(s[7]) : 0);
        stat.setSteal(s[8] != null ? Double.parseDouble(s[8]) : 0);
        stat.setGuest(s[9] != null ? Double.parseDouble(s[9]) : 0);
        stat.setGuestNice(s[10] != null ? Double.parseDouble(s[10]) : 0);
        return stat;
    }

    public String getUserAppName() {
        return userAppName;
    }

    public void setUserAppName(String userAppName) {
        this.userAppName = userAppName;
    }
}
