var ctx = document.getElementById('myChart').getContext('2d');
var chart = new Chart(ctx, {
    // The type of chart we want to create
    type: 'bar',

    // The data for our dataset
    data: {
        labels: [],
        datasets: [{
            label: 'No of votes',
            backgroundColor: '#90ee90',
            borderColor: '#90ee90',
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
    var keys = [];
    var vals = [];
    var vote_count = 0;
    for (var key in data){
        if (key != "keyCount"){
            vote_count += data[key];
            chart.data.labels.push(key)
            chart.data.datasets[0].data.push(data[key]);
        }
    }
    if(vote_count == data["keyCount"]){
        $("#count").removeClass("d-none")
    }
    chart.update()
});