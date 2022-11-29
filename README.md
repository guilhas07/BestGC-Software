GC-Selector offers the best GC solution (among four well known GCs: CMS, G1, Shenandoah, and ZGC) based on the CPU usage of your software.
You can choose the performance metric that you want to select the best GC accordingly.
To run the software please:
- choose the performance metric that matters to you the most. The performance metrics can be on off the followings:
    Throughput
    Pause time
    Memory usage
- run your software and let it initialize and put it under load
- in a terminal type the command below:
    java -jar software-jar-file your-software-name performance-metric sampling-time

- type your application name carefully
- performance-metric can be:
    t for throughput
    p for pause time
    m for memory usage
    a for all the above metrics
    
- sampling-time is the time in SECONDS that GC-Selector records statistics of your software. 
  It is an optional input and by default, it is set to 30 seconds.
  change it based on your software's execution time, ...
