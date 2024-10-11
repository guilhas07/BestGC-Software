#!/usr/bin/bash

BENCHMARK_PATH=./benchmark_app/benchmark_apps
hide(){
    mvn clean install &>/dev/null
    java -jar ./target/bestGC-0.0.1-SNAPSHOT.jar --user-app="$BENCHMARK_PATH/renaissance-gpl-0.15.0.jar als" --monitoring-time=40 --wp=0.5 &> log.txt &
    exit 0
}

# mvn clean install 
# java -jar ./target/bestGC-0.0.1-SNAPSHOT.jar --user-app="$BENCHMARK_PATH/renaissance-gpl-0.15.0.jar als" --monitoring-time=1 --wp=0.5
# java -jar ./target/bestGC-0.0.1-SNAPSHOT.jar --user-app="$BENCHMARK_PATH/myproject-1.0-SNAPSHOT.jar" --monitoring-time=1 --wp=0.5
# java -jar "$BENCHMARK_PATH/renaissance-gpl-0.15.0.jar" als

# java -jar ./target/bestGC-0.0.1-SNAPSHOT.jar --user-app="path to the user’s application’s jar file + its input options" --monitoring-time=40
# --wp=0.5
profile=""
if [[ "$1" == "console" ]]; then
    profile="-Pconsole"
fi
    
echo $profile
echo "${@:2}"
# echo mvn clean package $profile -Dmaven.test.skip && echo java --enable-preview -jar ./target/bestGC-0.0.1-SNAPSHOT.jar "${@:2}"
# mvn clean package $profile -Dmaven.test.skip && java --enable-preview -jar ./target/bestGC-0.0.1-SNAPSHOT.jar "${@:2}"
mvn -Pconsole spring-boot:run -Dspring-boot.run.arguments="./benchmark_gcs/benchmark_apps/dacapo-23.11-chopin.jar --args=\"spring -n 10 --no-pre-iteration-gc\" --automatic --monitoringTime=50"


