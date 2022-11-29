GC-Selector offers the best GC solution (among four well known GCs: G1, Parallel, Shenandoah, and ZGC) based on the CPU and heap usage of your software and based on user's preferences regarding Throughput and Pause Time.

- You may run BestGc using a command like bellow:
   java -jar BestGC.jar --user-app="path to the user’s application’s jar file + its input options" --monitoring-time=40
--wp="weight for pause time


There are several switches available to run BestGC:
Mandatory switches:
wt : a weight for throughput (a number between 0 and 1).
wp : a weight for pause time (also between 0 and 1). (Note that you just need to define one of wt or wp since wt+wp=1).
user-app : path to the user’s application’s jar file + all its input options that are used to run this application

Optional switches:
Monitoring-time: the time BestGC monitor the user's application to measure its heap and CPU usage (in seconds); default value is 30 seconds.

pid: Process ID. PID of the running user’s application.
run-best-gc: a boolean value(true / false) to define if the user need to his/her application to run automatically by BestGC with the suggested GC;default is true. 

