// Vertical bar chart

let startDate = new Date ('2020-01-01').toJSON();
const Http = new XMLHttpRequest();
const url='/time_series_summary?startDate=' + startDate;
Http.open("GET", url);
Http.send();

Http.onreadystatechange = (e) => {
    if (Http.readyState === 4 && Http.status === 200) {
        let summaryEntryArr = JSON.parse(Http.responseText);
        let labels = [];
        let marketValues = [];
        let bookValues = [];
        for (let entry of summaryEntryArr) {
            marketValues.push(entry.marketValue);
            bookValues.push(entry.bookValue);
            labels.push(entry.entryDate);
        }

        let ctx = document.getElementById('myChart').getContext('2d');
        let myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                        label: 'Market Value',
                        data: marketValues
                    },
                    {
                        label: "Book Value",
                        data: bookValues
                    }
                    ]
            }
        });

    }
}

