import os
import numpy as np
import math
import sys

data='/Users/sanazt/Documents/bestGc/maxHeap/renaissance/results-copy'
heap = sys.argv[1];

def add_means_to_gc_list(gc_name, Per):
    if (gc_name == "g1"):
        g1_90Pauses.append(float(Per))
    elif (gc_name == "ps"):
        ps_90Pauses.append(float(Per))
    elif (gc_name == "zgc"):
        zgc_90Pauses.append(float(Per))
    elif (gc_name == "shenandoah"):
        shenandoah_90Pauses.append(float(Per))

g1_90Pauses = []
ps_90Pauses= []
shenandoah_90Pauses = []
zgc_90Pauses = []

for gc in ["g1", "ps", "shenandoah", "zgc"]:
    pauseFile = "gc-" + str(heap) + ".lat"

    for file in os.listdir(data):
        path = data + "/" + file + "/" + gc +  "/" + pauseFile
        if (os.path.exists(path)):
            with open(path, 'r') as f:
                allPause = []
                lines = f.readlines()
                for line in lines:
                    if (line != '' and line != "\n"):
                        allPause.append(float(line))
                if allPause:
                    percentile = np.percentile(allPause, 90)
                    r = "{:.3f}".format(percentile)
                    add_means_to_gc_list(gc, r)
        else:
            add_means_to_gc_list(gc, float(1000000))

print ("90th percentile of Pause time of the all the 90th percentile Pause times for all benchmarks:")

g1Per = "{:.3f}".format(np.mean(g1_90Pauses))
print ("G1 : ", g1Per)
psPer = "{:.3f}".format(np.mean(ps_90Pauses))
print ("PS : ", psPer)
shenandoahPer = "{:.3f}".format(np.mean(shenandoah_90Pauses))
print ("Shenandoah : ", shenandoahPer)
zgcPer = "{:.3f}".format(np.mean(zgc_90Pauses))
print ("ZGC : ", zgcPer)
#print(zgc_90Pauses)


