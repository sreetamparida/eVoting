var ctx = document.getElementById('myChart').getContext('2d');
var chart = new Chart(ctx, {
    // The type of chart we want to create
    type: 'bar',

    // The data for our dataset
    data: {
        labels: [],
        datasets: [{
            label: 'No of votes',
            backgroundColor: 'rgb(255, 99, 132)',
            borderColor: 'rgb(255, 99, 132)',
            data: []
        }]
    },

    // Configuration options go here
    options : {
        responsive: true,
        title: {
            display: true,
            text: 'Result'
        },
        scales: {
            xAxes: [{
                display: true,
                barPercentage: 0.2
            }],
            yAxes: [{
                display: true,
                ticks: {
                    suggestedMin: 0,    // minimum will be 0, unless there is a lower value.
                    // OR //
                    beginAtZero: true   // minimum value will be 0.
                }
            }]
        }
    }
});

$.ajax({
    type: "GET",
    url: "/resultdata"
    // data: data,
}).done(function(data) {
    console.log(data)
    data = JSON.parse(data);
    var keys = []
    var vals = []
    for (var key in data){
        // keys.push(key);
        // vals.push(data[key])
        chart.data.labels.push(key)
        chart.data.datasets[0].data.push(data[key]);
    }
    chart.update()
});