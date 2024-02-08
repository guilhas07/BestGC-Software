#!/bin/bash

BENCHMARK_PATH=./benchmark_apps
hide(){
    mvn clean install &>/dev/null
    java -jar ./target/bestGC-0.0.1-SNAPSHOT.jar --user-app="$BENCHMARK_PATH/renaissance-gpl-0.15.0.jar als" --monitoring-time=40 --wp=0.5 &> log.txt &
    exit 0
}
mvn clean install 
java -jar ./target/bestGC-0.0.1-SNAPSHOT.jar --user-app="$BENCHMARK_PATH/renaissance-gpl-0.15.0.jar als" --monitoring-time=40 --wp=0.5
# java -jar "$BENCHMARK_PATH/renaissance-gpl-0.15.0.jar" als

# java -jar ./target/bestGC-0.0.1-SNAPSHOT.jar --user-app="path to the user’s application’s jar file + its input options" --monitoring-time=40
# --wp=0.5
