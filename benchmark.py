#!/usr/bin/python3
from __future__ import annotations
from dataclasses import dataclass, asdict
import subprocess
import re
import json
import glob
import argparse

BENCHMARK_PATH = "./benchmark_apps"
BENCHMARK_STATS_PATH = "./benchmark_stats"
BENCHMARK_LOG_PATH = "./benchmark_logs"


@dataclass
class GarbageCollectorResult:
    garbage_collector: str
    number_of_pauses: int
    total_pause_time: int
    avg_pause_time: int

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
    number_of_pauses: int
    total_pause_time: int
    avg_pause_time: int
    pauses_per_category: dict[str, int]
    total_pause_time_per_category: dict[str, int]
    avg_pause_time_per_category: dict[str, int]

    @staticmethod
    def load_from_json(file_path: str) -> BenchmarkResult:

        with open(file_path) as f:
            return BenchmarkResult(**json.loads(f.read()))

    @staticmethod
    def build_benchmark_result(
        gc: str, benchmark_group: str, benchmark_name: str
    ) -> BenchmarkResult:
        log_file = get_benchmark_log_path(gc, benchmark_group, benchmark_name)
        number_of_pauses = 0

        total_pause_time = 0
        avg_pause_time = 0
        pauses_per_category = {}
        total_pause_time_per_category = {}
        avg_pause_time_per_category = {}

        with open(log_file) as f:
            for line in f:
                if "safepoint" in line:
                    # use () to group
                    pause_category = re.findall('Safepoint "(.*)"', line)[0]
                    pause_time = int(re.findall("Total: (\\d+) ns", line)[0])

                    if pause_category not in pauses_per_category:
                        pauses_per_category[pause_category] = 0
                        total_pause_time_per_category[pause_category] = 0

                    pauses_per_category[pause_category] += 1
                    total_pause_time_per_category[pause_category] += pause_time
                    total_pause_time += pause_time
                    number_of_pauses += 1

        avg_pause_time = total_pause_time / number_of_pauses
        for category, pause_time in total_pause_time_per_category.items():
            avg_pause_time_per_category[category] = (
                pause_time / pauses_per_category[category]
            )
        return BenchmarkResult(
            gc,
            benchmark_group,
            benchmark_name,
            number_of_pauses,
            total_pause_time,
            avg_pause_time,
            pauses_per_category,
            total_pause_time_per_category,
            avg_pause_time_per_category,
        )

    def save_to_json(self):
        with open(
            get_benchmark_stats_path(
                self.garbage_collector, self.benchmark_group, self.benchmark_name
            ),
            "w",
        ) as f:
            f.write(json.dumps(asdict(self), indent=4))


def run_renaissance(gc: str, iterations: int) -> list[BenchmarkResult]:
    benchmark_results: list[BenchmarkResult] = []
    benchmark_group = "Renaissance"

    result = subprocess.run(
        ["java", "-jar",
            f"{BENCHMARK_PATH}/renaissance-gpl-0.15.0.jar", "--raw-list"],
        capture_output=True,
        text=True,
    )
    renaissance_benchmarks = result.stdout.splitlines()

    for benchmark in renaissance_benchmarks:
        print(f"Running benchmark {benchmark} with GC: {gc}")
        result = subprocess.run(
            [
                "java",
                f"-XX:+Use{gc}GC",
                f"-Xlog:gc*,safepoint:file={get_benchmark_log_path(gc, benchmark_group, benchmark)}::filecount=0",
                # f"-Xlog:gc*,safepoint:file={BENCHMARK_LOG_PATH}/{benchmark_group}_{benchmark}_{gc}.log::filecount=0",
                "-jar",
                f"{BENCHMARK_PATH}/renaissance-gpl-0.15.0.jar",
                benchmark,
                "-r",
                f"{iterations}",
                "--no-forced-gc",
            ]
        )

    for benchmark in renaissance_benchmarks:
        result = BenchmarkResult.build_benchmark_result(
            gc, benchmark_group, benchmark)
        result.save_to_json()
        benchmark_results.append(result)

    return benchmark_results


def get_benchmark_log_path(gc: str, benchmark_group: str, benchmark_name: str) -> str:
    return f"{BENCHMARK_LOG_PATH}/{benchmark_group}_{benchmark_name}_{gc}.log"


def get_benchmark_stats_path(gc: str, benchmark_group: str, benchmark_name: str) -> str:
    return f"{BENCHMARK_STATS_PATH}/{benchmark_group}_{benchmark_name}_{gc}.json"


# def get_jvm_opts(garbage_collector: str, benchmark_name: str):
#     return f"-Xlog:gc*,safepoint:file={BENCHMARK_LOG_PATH}/{benchmark_name}.log::filecount=0"


def load_benchmarks():
    return [
        BenchmarkResult.load_from_json(i)
        for i in glob.glob(f"{BENCHMARK_STATS_PATH}/*.json")
    ]


def main(argv=None) -> int:

    parser = argparse.ArgumentParser(
        description="Compute throughput and average pause time for benchmarks"
    )
    parser.add_argument(
        "-r",
        "--run_benchmarks",
        dest="run_benchmarks",
        action="store_true",
        help="Run benchmarks",
    )

    args = parser.parse_args(argv)
    run_benchmarks = args.run_benchmarks

    for gc in ["G1"]:
        benchmark_results: list[BenchmarkResult] = []
        if run_benchmarks:
            benchmark_results.append(run_renaissance(gc, 10))
        else:
            benchmark_results = load_benchmarks()

        total_gc_pause_time = 0
        total_gc_pauses = 0
        for result in benchmark_results:
            total_gc_pauses += result.number_of_pauses
            total_gc_pause_time += result.total_pause_time

        gc_result = GarbageCollectorResult(
            gc,
            total_gc_pauses,
            total_gc_pause_time,
            total_gc_pause_time / total_gc_pauses,
        )
        print(f"{gc_result}")
        gc_result.save_to_json()
    return 0


if __name__ == "__main__":
    # TODO: only for 90th percentile pause_times ?
    # TODO: add throughtput calculations too

    exit(main())
