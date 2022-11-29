#!/bin/bash
JAVA_HOME=/usr/lib/jvm/java-15-openjdk-amd64
bestgc_jar=/home/stavakoli/bestGc

function run {
    bench=$1
    size=$2
    $JAVA_HOME/bin/java -jar $bestgc_jar/bestGC-0.0.1-SNAPSHOT.jar \
        --userapp="/home/stavakoli/dacapo-9.12-MR1-bach.jar $bench -n 20 -s $size" \
        --samplingtime=20
}


for BENCH in avrora jython lusearch-fix pmd sunflow xalan
do
 run $BENCH large
done
for BENCH in fop luindex
do
 run $BENCH default
done
for BENCH in h2 tomcat
do
 run $BENCH huge
done








