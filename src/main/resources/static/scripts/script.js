const currentPage = window.location.pathname;

let apps = null;
switch (currentPage) {
    case "/":
        showProfilePage();
        break;
    case "/run_app":
        showRunAppPage();
        break;
    case "/apps":
        loadApps();
        showAppsPage();
        break;
    default:
}
// NOTE: show body now to prevent jitters in display
document.body.style.display = "block";

function getCommand(id) {
    return document.querySelector(`[id^=cmd-${id}]`);
}

function getName(id) {
    return document.querySelector(`[id^=name-${id}]`);
}

function loadApps() {
    apps = {};
    for (el of document.querySelectorAll("[id^=details-]")) {
        let id = /\d+/.exec(el.id)[0];
        let name = /App: (.*)/.exec(getName(id).innerText)[1];

        let cmd = /Command: (.*)/.exec(getCommand(id).innerText)[1];
        apps[id] = { name: name, command: cmd };
    }
}

function updateAppGraph(id, appInfo) {
    let app = apps[id];
    //console.log(app);
    //console.log(appInfo);

    if (appInfo == null) {
        // TODO: handle app disappeared
        return;
    }

    if (app == null) {
        apps[id] = appInfo;
        alert("new");
        // TODO: Create Graph
    } else if (app.name != appInfo.name || app.command != appInfo.command) {
        // TODO: clear graph
        apps[id] = appInfo;
        let cmd = getCommand(id);
        cmd.innerText = `Command: ${appInfo.command}`;

        let name = getName(id);
        name.innerText = `App: ${appInfo.name}`;
    }
    // TODO: Append to graph
}

function showAppsPage() {
    setInterval(pollApps, 1000);
}
async function pollApps() {
    let ids = Array.from(
        document.querySelectorAll("[id^=details-].collapse.show")
    )
        .map((el) => /\d+/.exec(el.id)[0])
        .join(",");

    if (ids.length == 0) return;

    let r = await fetch(`/poll?ids=${ids}`);
    let j = await r.json();
    console.log(j);
    for (let [k, v] of Object.entries(j)) {
        updateAppGraph(k, v);
    }
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

    tw_input.addEventListener("input", function() {
        let tw = tw_input.value;
        if (tw > 1 || tw < 0) return;
        pw_input.value = (1 - tw).toFixed(2);
    });

    pw_input.addEventListener("input", function() {
        let pw = pw_input.value;
        if (pw > 1 || pw < 0) return;
        tw_input.value = (1 - pw).toFixed(2);
    });

    handleCustomFileDisplay();
    handleAutomaticModeDisplay();
}

// handle profile request
htmx.onLoad(function(target) {
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
