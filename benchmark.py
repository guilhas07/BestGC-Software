#!/usr/bin/python3
from __future__ import annotations

import argparse
import glob
import json
import os
import re
import subprocess
import time
from dataclasses import asdict, dataclass

import numpy

BENCHMARK_PATH = "./benchmark_apps"
BENCHMARK_STATS_PATH = "./benchmark_stats"
BENCHMARK_LOG_PATH = "./benchmark_logs"
CPU_COUNT = os.cpu_count()
CPU_THRESHOLD = 60


Stats = tuple[float, int]
"""(average_cpu_percentage, throughput)
average_cpu_percentage: float -> average benchmark cpu percentage
throughput: int -> Time in nanoseconds. Equivalent to program execution time"""


@dataclass
class GarbageCollectorResult:
    garbage_collector: str
    number_of_pauses: int
    total_pause_time: int
    avg_pause_time: float
    p90_avg_pause_time: float
    avg_throughput: float

    def save_to_json(self):
        with open(
            f"{BENCHMARK_STATS_PATH}/global_stats/{self.garbage_collector}.json",
            "w",
        ) as f:
            f.write(json.dumps(asdict(self), indent=4))


@dataclass
class BenchmarkResult:
    garbage_collector: str
    benchmark_group: str
    benchmark_name: str
    cpu_intensive: bool
    number_of_pauses: int
    total_pause_time: int
    avg_pause_time: float
    pauses_per_category: dict[str, int]
    total_pause_time_per_category: dict[str, int]
    avg_pause_time_per_category: dict[str, float]
    p90_pause_time: float
    throughput: int

    @staticmethod
    def load_from_json(file_path: str) -> BenchmarkResult:
        with open(file_path) as f:
            return BenchmarkResult(**json.loads(f.read()))

    @staticmethod
    def build_benchmark_result(
        gc: str,
        benchmark_group: str,
        benchmark_name: str,
        cpu_intensive: bool,
        throughput: int,
    ) -> BenchmarkResult:
        log_file = get_benchmark_log_path(gc, benchmark_group, benchmark_name)
        number_of_pauses = 0

        total_pause_time = 0
        avg_pause_time = 0.0
        pauses_per_category = {}
        total_pause_time_per_category = {}
        avg_pause_time_per_category = {}
        pause_times = []

        with open(log_file) as f:
            for line in f:
                if "safepoint" in line:
                    # use () to group
                    pause_category = re.findall('Safepoint "(.*)"', line)[0]
                    pause_time = int(re.findall("Total: (\\d+) ns", line)[0])
                    pause_times.append(pause_time)

                    if pause_category not in pauses_per_category:
                        pauses_per_category[pause_category] = 0
                        total_pause_time_per_category[pause_category] = 0

                    pauses_per_category[pause_category] += 1
                    total_pause_time_per_category[pause_category] += pause_time
                    total_pause_time += pause_time
                    number_of_pauses += 1

        p90_pause_time = round(numpy.percentile(pause_times, 90), 2)
        avg_pause_time = round(total_pause_time / number_of_pauses, 2)

        for category, pause_time in total_pause_time_per_category.items():
            avg_pause_time_per_category[category] = round(
                pause_time / pauses_per_category[category], 2
            )

        return BenchmarkResult(
            gc,
            benchmark_group,
            benchmark_name,
            cpu_intensive,
            number_of_pauses,
            total_pause_time,
            avg_pause_time,
            pauses_per_category,
            total_pause_time_per_category,
            avg_pause_time_per_category,
            p90_pause_time,
            throughput,
        )

    def save_to_json(self):
        with open(
            get_benchmark_stats_path(
                self.garbage_collector, self.benchmark_group, self.benchmark_name
            ),
            "w",
        ) as f:
            f.write(json.dumps(asdict(self), indent=4))


def run_benchmark(
    gc: str, benchmark: str, benchmark_group: str, iterations: int
) -> Stats:
    process = subprocess.Popen(
        [
            "java",
            f"-XX:+Use{gc}GC",
            f"-Xlog:gc*,safepoint:file={get_benchmark_log_path(gc, benchmark_group, benchmark)}::filecount=0",
            "-jar",
            f"{BENCHMARK_PATH}/renaissance-gpl-0.15.0.jar",
            benchmark,
            "-r",
            f"{iterations}",
            "--no-forced-gc",
        ]
    )
    time_start = time.time_ns()
    pid = process.pid
    cpu_stats = []

    # TODO: see better polling method than sleeping 1 seconds for throughput
    while process.poll() is None:
        # subprocess.run with capture_output doesn't seem to capture the whole output when
        # using top with -1 flag
        p = subprocess.run(
            ["top", "-bn", "1", "-p", f"{pid}"], capture_output=True, text=True
        )
        lines = p.stdout.splitlines()[-2:]
        assert lines[0].split()[8] == "%CPU"
        cpu_percentage = float(lines[1].split()[8])
        cpu_stat = round(float(cpu_percentage / CPU_COUNT), 1)
        print(f"{cpu_stat=}")
        cpu_stats.append(cpu_stat)
        time.sleep(1)

    return (round(float(numpy.mean(cpu_stats)), 1), time.time_ns() - time_start)


def run_renaissance(gc: str, iterations: int) -> list[BenchmarkResult]:
    benchmark_results: list[BenchmarkResult] = []
    benchmark_group = "Renaissance"

    result = subprocess.run(
        ["java", "-jar", f"{BENCHMARK_PATH}/renaissance-gpl-0.15.0.jar", "--raw-list"],
        capture_output=True,
        text=True,
    )
    renaissance_benchmarks = result.stdout.splitlines()

    # for benchmark in renaissance_benchmarks:
    for benchmark in renaissance_benchmarks[0:2]:
        print(f"Running benchmark {benchmark} with GC: {gc} and {iterations=}")
        (average_cpu, throughput) = run_benchmark(
            gc, benchmark, benchmark_group, iterations
        )
        print(f"{average_cpu=} and {throughput=}")
        print(f"{type(average_cpu)=} and {type(throughput)=}")
        result = BenchmarkResult.build_benchmark_result(
            gc, benchmark_group, benchmark, average_cpu >= CPU_THRESHOLD, throughput
        )
        result.save_to_json()
        benchmark_results.append(result)

    return benchmark_results


def get_benchmark_log_path(gc: str, benchmark_group: str, benchmark_name: str) -> str:
    return f"{BENCHMARK_LOG_PATH}/{benchmark_group}_{benchmark_name}_{gc}.log"


def get_benchmark_stats_path(gc: str, benchmark_group: str, benchmark_name: str) -> str:
    return f"{BENCHMARK_STATS_PATH}/{benchmark_group}_{benchmark_name}_{gc}.json"


# def get_jvm_opts(garbage_collector: str, benchmark_name: str):
#     return f"-Xlog:gc*,safepoint:file={BENCHMARK_LOG_PATH}/{benchmark_name}.log::filecount=0"


def load_benchmark_results():
    return [
        BenchmarkResult.load_from_json(i)
        for i in glob.glob(f"{BENCHMARK_STATS_PATH}/*.json")
    ]


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(
        description="Compute throughput and average pause time for benchmarks"
    )
    parser.add_argument(
        "-s",
        "--skip_benchmarks",
        dest="skip_benchmarks",
        action="store_true",
        help="Skip the benchmarks",
    )
    parser.add_argument(
        "-i",
        "--iterations",
        dest="iterations",
        default=10,
        help="Number of iterations to run benchmarks. Increase this number to achieve more reliable metrics.",
    )
    parser.print_help()

    args = parser.parse_args(argv)
    skip_benchmarks = args.skip_benchmarks
    iterations = args.iterations

    for gc in ["G1"]:
        benchmark_results: list[BenchmarkResult] = []
        if skip_benchmarks:
            benchmark_results = load_benchmark_results()
        else:
            benchmark_results.extend(run_renaissance(gc, iterations))

        total_gc_pauses = 0
        total_gc_pause_time = 0
        p90_gc_pause_time = []
        gc_throughput = []

        for result in benchmark_results:
            total_gc_pauses += result.number_of_pauses
            total_gc_pause_time += result.total_pause_time
            p90_gc_pause_time.append(result.p90_pause_time)
            gc_throughput.append(result.throughput)

        gc_result = GarbageCollectorResult(
            gc,
            total_gc_pauses,
            total_gc_pause_time,
            round(total_gc_pause_time / total_gc_pauses, 2),
            round(numpy.mean(p90_gc_pause_time), 2),
            round(numpy.mean(gc_throughput), 2),
        )
        print(f"{gc_result}")
        gc_result.save_to_json()
    return 0


if __name__ == "__main__":
    exit(main())
