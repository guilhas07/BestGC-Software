window.onload = function(){
    let form = document.getElementById("form");
    let tw_input = form["throughput_weight"]
    tw_input.addEventListener('change', function(){

        let tw = tw_input.valueAsNumber;
        if (isNaN(tw) || tw > 1 || tw < 0)
            return
        tw = tw * 100

        pw_input.value = (100 - tw) / 100; 
    })

    let pw_input = form["pause_time_weight"]
    pw_input.addEventListener('change', function(){
        console.log("ALI")
        let pw = pw_input.valueAsNumber;
        if (isNaN(pw) || pw > 1 || pw < 0)
            return

        pw = pw * 100
        tw_input.value = (100 - pw) / 100; 

    })
}
