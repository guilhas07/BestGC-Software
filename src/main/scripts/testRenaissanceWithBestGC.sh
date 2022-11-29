
#!/bin/bash
JAVA_HOME=/usr/lib/jvm/java-15-openjdk-amd64
bestgc_jar=/home/stavakoli/bestGc

function run {
    bench=$1

    $JAVA_HOME/bin/java -jar $bestgc_jar/bestGC-0.0.1-SNAPSHOT.jar \
        --userapp="/home/stavakoli/renaissance/renaissance-gpl-0.11.0.jar $bench --repetitions 20 --no-forced-gc" \
        --samplingtime=20
}


for BENCH in akka-uct reactors als chi-square dec-tree gauss-mix logg-regression \
        movie-lens naive-bayes page-rank db-shootout \
        dummy-empty dummy-failing dummy-param dummy-setup-failing \
        fj-kmeans future-genetic mnemonics par-mnemonics scrabble \
        neo4j-analytics rx-scrabble dotty scala-doku scala-kmeans \
        philosophers scala-etm-bench7 finagle-chirper finagle-http
do
 run $BENCH
done


