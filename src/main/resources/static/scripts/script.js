const currentPage = window.location.pathname;

switch (currentPage) {
    case "/":
        showProfilePage();
        break;
    case "/run_app":
        showRunAppPage();
        break;
    default:
}

function showRunAppPage() {
    let form = document.getElementById("form");
    let heapSizeSelection = form["heapSizeSelection"];
    let heapSize = form["heapSize"];

    function handleHeapSizeDisplay() {
        if (heapSizeSelection.value == "Custom") {
            heapSize.style.display = "block";
            heapSize.labels[0].style.display = "block";
        } else {
            heapSize.style.display = "none";
            heapSize.labels[0].style.display = "none";
            heapSize.value = heapSizeSelection.value;
        }
    }

    form["jar"].addEventListener("change", handleCustomFileDisplay);
    form["heapSizeSelection"].addEventListener("change", handleHeapSizeDisplay);

    handleHeapSizeDisplay();
    handleCustomFileDisplay();

    // NOTE: show body now to prevent jitters in display
    document.body.style.display = "block";
}

function showProfilePage() {
    let form = document.getElementById("form");
    let automaticMode = form["automaticMode"];
    let tw_input = form["throughputWeight"];
    let pw_input = form["pauseTimeWeight"];
    let jar = form["jar"];

    function handleAutomaticModeDisplay() {
        if (!automaticMode.checked) {
            tw_input.type = "number";
            pw_input.type = "number";
            tw_input.labels[0].style.display = "block";
            pw_input.labels[0].style.display = "block";
            return;
        }
        // NOTE: changing input type first would not allow to access labels.
        tw_input.labels[0].style.display = "none";
        pw_input.labels[0].style.display = "none";
        tw_input.type = "hidden";
        pw_input.type = "hidden";
    }

    automaticMode.addEventListener("change", handleAutomaticModeDisplay);

    jar.addEventListener("change", handleCustomFileDisplay);

    tw_input.addEventListener("input", function () {
        let tw = tw_input.value;
        if (tw > 1 || tw < 0) return;
        pw_input.value = (1 - tw).toFixed(2);
    });

    pw_input.addEventListener("input", function () {
        let pw = pw_input.value;
        if (pw > 1 || pw < 0) return;
        tw_input.value = (1 - pw).toFixed(2);
    });

    handleCustomFileDisplay();
    handleAutomaticModeDisplay();

    // NOTE: show body now to prevent jitters in display
    document.body.style.display = "block";
}

// handle profile request
htmx.onLoad(function (target) {
    console.log(target);
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
});

function handleCustomFileDisplay() {
    let form = document.getElementById("form");
    let jar = form["jar"];
    let file = form["file"];
    if (jar.value == "Custom") {
        file.style.display = "block";
        file.required = true;
        return;
    }
    file.required = false;
    file.style.display = "none";
}
