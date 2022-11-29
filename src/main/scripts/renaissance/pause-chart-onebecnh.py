import sys
import os
import numpy as np
import math
import matplotlib.pyplot as plt

bench= sys.argv[1]

savePlotPath="/Users/sanazt/Documents/bestGc/maxHeap/renaissance/plots/pause"
data='/Users/sanazt/Documents/bestGc/maxHeap/renaissance/results-copy'
data = data + "/" + bench
#directory = os.fsencode(data)
#print (sys.argv[1])
gcs = []
pause_8192 = []
pause_4096 = []
pause_2048= []
pause_1024 = []
pause_512 = []
pause_256 = []
pause_default = []
heaps = [8192, 4096, 2048, 1024, 512, 256, "default"]
def add_to_heap(l, val):
    if (l == 8192):
        pause_8192.extend(val)
    if (l == 4096):
        pause_4096.extend(val)
    elif (l == 2048):
        pause_2048.extend(val)
    elif (l == 1024):
        pause_1024.extend(val)
    elif (l == 512):
        pause_512.extend(val)
    elif (l == 256):
        pause_256.extend(val)
    elif (l == "default"):
        pause_default.extend(val)
for file in os.listdir(data):
    if not file.startswith('.'):
        gcs.append(file)
        for heap in heaps:
            lat = "gc-" + str(heap) + ".lat"
            newdir = data + "/" + file + "/" + lat
            if os.path.exists(newdir):
                with open(newdir, 'r') as f:
                    lines = f.readlines()
                    heapNum = 0 #number of heap sizes
                    pauses = []
                    for line in lines:
                        pauses.append(float(line))            
                    if (pauses):
                        heapNum = heapNum+1
                        percentile = np.percentile(pauses, 90)
                        m = []
                        m.append(round(percentile,3))
                        add_to_heap(heap,m)
                    if not pauses:
                        m = []
                        m.append(0)
                        add_to_heap(heap,m)       
            if not os.path.exists(newdir):
                m = []
                m.append(0)
                add_to_heap(heap,m)    
               # while heapNum < 6:
                  # heapNum = heapNum+1
                   # m = []
                   # m.append(0)
                   # add_to_heap(heaps[heapNum-1],m)    

x = np.arange(len(gcs))
#len(gcs)  # the label locations
width = 0.1  # the width of the bars
#fig, ax = plt.subplots()
fig, ax =plt.subplots(figsize=(20,12))
rects1 = ax.bar(x - (2*width+width/2), pause_8192, width, label='8192')
rects2 = ax.bar(x - (width+width/2), pause_4096, width, label='4096')
rects3 = ax.bar(x - width/2, pause_2048, width, label='2048')
rects4 = ax.bar(x + width/2, pause_1024, width, label='1024')
rects5 = ax.bar(x + width + width/2, pause_512, width, label='512')
rects6 = ax.bar(x + 2*width + width/2, pause_256, width, label='256')
rects7 = ax.bar(x + 3*width + width/2, pause_default, width, label='default')
# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Pause Time (ms)')
ax.set_title(bench)
ax.set_xticks(x, gcs)
ax.legend()

ax.bar_label(rects1, padding=3,rotation=90)
ax.bar_label(rects2, padding=3,rotation=90)
ax.bar_label(rects3, padding=3,rotation=90)
ax.bar_label(rects4, padding=3,rotation=90)
ax.bar_label(rects5, padding=3,rotation=90)
ax.bar_label(rects6, padding=3,rotation=90)
ax.bar_label(rects7, padding=3,rotation=90)


#fig.tight_layout()
#plt.xticks(rotation=90)
#plt.show()
savePath = savePlotPath + "/" + bench
fig.savefig(savePath)


