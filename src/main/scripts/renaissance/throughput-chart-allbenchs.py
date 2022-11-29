import sys
import os
import numpy as np
import math
import matplotlib.pyplot as plt

savePlotPath="/Users/sanazt/Documents/bestGc/maxHeap/renaissance/plots/throughput"

# bench= sys.argv[1]
data='/Users/sanazt/Documents/bestGc/maxHeap/renaissance/results-copy'
for bench in os.listdir(data):    
    #data = data + "/" + bench
        
    #print (sys.argv[1])
    gcs = []
    throughput_8192 = []
    throughput_4096 = []
    throughput_2048= []
    throughput_1024 = []
    throughput_512 = []
    throughput_256 = []
    throughput_default = []
    heaps = [8192, 4096, 2048, 1024, 512, 256, "default"]
    def add_to_heap(l, val):
        if (l == '8192'):
            throughput_8192.extend(val)
        if (l == '4096'):
            throughput_4096.extend(val)
        elif (l == '2048'):
            throughput_2048.extend(val)
        elif (l == '1024'):
            throughput_1024.extend(val)
        elif (l == '512'):
            throughput_512.extend(val)
        elif (l == '256'):
            throughput_256.extend(val)
        elif (l == "default"):
            throughput_default.extend(val)

    for file in os.listdir(data + "/" + bench):
        if not file.startswith('.'):
            gcs.append(file)
            newdir = str(data + "/" + bench + "/" + file + "/avg_throughput.avg")
            if os.path.exists(newdir):
                existingHeap = []
                with open(newdir, 'r') as f:
                    lines = f.readlines()
                    heapNum = 0 #number of heap sizes
                    for line in lines:
                        val = line.split()
                        if (len(val) > 2 ):
                            if (val[2] != ''):
                                heapNum = heapNum+1
                                m = []
                                m.append(float(val[2]))
                                add_to_heap(val[0],m)
                                existingHeap.append(str(val[0]))
                for heap in heaps:
                    if str(heap) not in existingHeap: 
                        m = []
                        m.append(0)
                        add_to_heap(str(heap),m)       
                existingHeap.clear()

    x = np.arange(len(gcs))
    #len(gcs)  # the label locations
    width = 0.1  # the width of the bars
    #fig, ax = plt.subplots()
    fig, ax =plt.subplots(figsize=(20,12))
    rects1 = ax.bar(x - (2*width+width/2), throughput_8192, width, label='8192')
    rects2 = ax.bar(x - (width+width/2), throughput_4096, width, label='4096')
    rects3 = ax.bar(x - width/2, throughput_2048, width, label='2048')
    rects4 = ax.bar(x + width/2, throughput_1024, width, label='1024')
    rects5 = ax.bar(x + width + width/2, throughput_512, width, label='512')
    rects6 = ax.bar(x + 2*width + width/2, throughput_256, width, label='256')
    rects7 = ax.bar(x + 3*width + width/2, throughput_default, width, label='default')
    # Add some text for labels, title and custom x-axis tick labels, etc.
    ax.set_ylabel('Throughput (ms)')
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
