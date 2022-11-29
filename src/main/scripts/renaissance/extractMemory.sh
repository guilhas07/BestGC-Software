#!/bin/bash

dir=/Users/sanazt/Documents/bestGc/maxHeap/renaissance/results-copy

for heap in 8192 4096 2048 1024 512 256 default
do
for f in $dir/*
do
for GC in ps g1
do

for FILE in $f/$GC
do
if [[ -f "$FILE/"jvm-$heap".log" ]]
then
	cat $FILE/"jvm-$heap".log | grep Pause | grep "\->" | awk '{print $(NF-1)}' | awk -F"[->]" '{print $1}' | awk -F"M" '{print $1}' > /$FILE/"mem-$heap".mem
fi
done
done

for GC in zgc
do
for FILE in $f/$GC
do
if [[ -f "$FILE/"jvm-$heap".log" ]]
then
	cat $FILE/"jvm-$heap".log | grep 'GC(' | grep "\->" | awk '{print $(NF)}' | awk -F"[->]" '{print $1}' | awk -F"[(]" '{print $1}' | awk -F"M" '{print $1}'> /$FILE/"mem-$heap".mem
fi
done
done

for GC in shenandoah
do
for FILE in $f/$GC
do
if [[ -f "$FILE/"jvm-$heap".log" ]]
then
	cat $FILE/"jvm-$heap".log | grep 'GC(' | grep "\->" | awk '{print $(NF-1)}' | awk -F"[->]" '{print $1}' | awk -F"M" '{print $1}'> /$FILE/"mem-$heap".mem
fi
done
done
done
done
