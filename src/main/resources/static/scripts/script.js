window.onload = function(){
    let form = document.getElementById("form");
    let tw_input = form["throughputWeight"]
    let pw_input = form["pauseTimeWeight"]
    let jar = form["jar"]
    let file = form["file"]

    jar.addEventListener('change', function(){
        if (jar.value == "Custom"){
            file.style.display = "block";
            file.required = true;
            return;
        }
        file.required = false;
        file.style.display = "none"
    })

    tw_input.addEventListener('input', function(){

        let tw = tw_input.valueAsNumber;
        if (isNaN(tw) || tw > 1 || tw < 0)
            return
        tw = tw * 100

        pw_input.value = (100 - tw) / 100; 
    })

    pw_input.addEventListener('input', function(){
        console.log("ALI")
        let pw = pw_input.valueAsNumber;
        if (isNaN(pw) || pw > 1 || pw < 0)
            return

        pw = pw * 100
        tw_input.value = (100 - pw) / 100; 

    })
}
