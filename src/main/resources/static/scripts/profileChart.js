window.onload = function(){

    console.log("AQUI")
    console.log(cpu_usage)
    console.log(io_time)
    console.log(cpu_time)

    const ctx = document.getElementById("myChart")
    try {
    new Chart(ctx, {
      type: "line",
      data: {
        labels: Array.from({ length: cpu_usage.length }, (value, index) => index),
        datasets: [{
            label: "CPU Usage Percentage",
          data: cpu_usage,
          borderColor: "red",
          fill: false
        },{

          label: "I/O Time Percentage",
          data: io_time,
          borderColor: "green",
          fill: false
        },{
          label: "CPU Time Percentage",
          data: cpu_time,
          borderColor: "blue",
          fill: false
        }]
      },
      options: {
        legend: {display: true}
      }
    });                         
    }catch(error)
        { console.log(error)}
  //  var giro = new Chart(ctx, {
  //  type: 'bar',
  //  data: {
  //    labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'],
  //    datasets: [{
  //      label: '# of Votes',
  //      data: [12, 19, 3, 5, 2, 3],
  //      borderWidth: 1
  //    }]
  //  },
  //  options: {
  //    scales: {
  //      y: {
  //        beginAtZero: true
  //      }
  //    }
  //  }
  //});
    //console.log(giro)
}
