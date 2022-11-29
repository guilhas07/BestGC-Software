import os
import numpy as np
import math
import sys

data='/Users/sanazt/Documents/bestGc/maxHeap/renaissance/results-copy'
heap = sys.argv[1];

def add_means_to_gc_list(gc_name, avg):
    if (gc_name == "g1"):
        g1_avgThroughputs.append(float(avg))
    elif (gc_name == "ps"):
        ps_avgThroughputs.append(float(avg))
    elif (gc_name == "zgc"):
        zgc_avgThroughputs.append(float(avg))
    elif (gc_name == "shenandoah"):
        shenandoah_avgThroughputs.append(float(avg))

g1_avgThroughputs = []
ps_avgThroughputs= []
shenandoah_avgThroughputs = []
zgc_avgThroughputs = []

for gc in ["g1", "ps", "shenandoah", "zgc"]:
    throughputFile = "throughput-" + str(heap) + ".ms"

    for file in os.listdir(data):
        path = data + "/" + file + "/" + gc +  "/" + throughputFile
        if (os.path.exists(path)):
            with open(path, 'r') as f:        
                allThroughputs = []
                lines = f.readlines()
                for line in lines:
                    if (line != '' and line != "\n"):
                        allThroughputs.append(float(line))
                if allThroughputs:
                    mean = np.mean(allThroughputs)
                    r = "{:.3f}".format(mean)
                    add_means_to_gc_list(gc, r)    
        else:
            add_means_to_gc_list(gc, float(1000000))

print ("AVG throughput of the all the AVG throughputs for all benchmarks:")

g1Avg = "{:.3f}".format(np.mean(g1_avgThroughputs))
print ("G1 : ", g1Avg)
psAvg = "{:.3f}".format(np.mean(ps_avgThroughputs))
print ("PS : ", psAvg)
shenandoahAvg = "{:.3f}".format(np.mean(shenandoah_avgThroughputs))
print ("Shenandoah : ", shenandoahAvg)
zgcAvg = "{:.3f}".format(np.mean(zgc_avgThroughputs))
print ("ZGC : ", zgcAvg)
# print(g1_avgThroughputs)
# print(ps_avgThroughputs)
# print(shenandoah_avgThroughputs)
# print(zgc_avgThroughputs)

