window.onload = function () {
    console.log("AQUI");
    console.log(cpu_usage);
    console.log(io_time);
    console.log(cpu_time);

    const timePercentageCtx = document.getElementById("timePercentageChart");
    const cpuUsageCtx = document.getElementById("cpuUsageChart");
    try {
        new Chart(timePercentageCtx, {
            type: "line",
            data: {
                labels: Array.from(
                    { length: cpu_time.length },
                    (value, index) => index
                ),
                datasets: [
                    {
                        label: "I/O Time Percentage",
                        data: io_time,
                        borderColor: "green",
                        fill: false,
                    },
                    {
                        label: "CPU Time Percentage",
                        data: cpu_time,
                        borderColor: "blue",
                        fill: false,
                    },
                ],
            },
            options: {
                legend: { display: true },
            },
        });

        new Chart(cpuUsageCtx, {
            type: "line",
            data: {
                labels: Array.from(
                    { length: cpu_usage.length },
                    (value, index) => index
                ),
                datasets: [
                    {
                        label: "CPU Usage Percentage",
                        data: cpu_usage,
                        borderColor: "red",
                        fill: false,
                    },
                ],
            },
            options: {
                legend: { display: true },
            },
        });
    } catch (error) {
        console.log(error);
    }
};
