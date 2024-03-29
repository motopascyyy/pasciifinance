let accounts;
let form;
let modal;

function getAccounts () {
    const Http = new XMLHttpRequest();
    const url='/latestEntries';
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        if (Http.readyState === 4 && Http.status === 200) {
            let accountEntries = JSON.parse(Http.responseText);
            accounts = getAccountsFromEntries(accountEntries);
            let formEntryDiv = document.getElementById("form_entry_area");
            formEntryDiv.innerHTML = '';
            for (let acc of accounts) {
                let accEntryDiv = document.createElement("DIV");
                accEntryDiv.setAttribute("class", "account_entry_item");

                let chartButton = document.createElement("button");
                chartButton.innerText = String.fromCodePoint(0x1F4C8);
                chartButton.addEventListener('click', function(evt) {
                    const accId = acc.id;
                    getBalanceString(accId);
                });
                let label = document.createElement("LABEL");
                label.setAttribute("class", "entry_label");
                label.appendChild(chartButton);
                let bvId = acc.institution + "_" + acc.id + "_bv";
                let mvId = acc.institution + "_" + acc.id + "_mv";
                let growthId = acc.institution + "_" + acc.id + "_gv";
                label.setAttribute("for", bvId);
                let labelTextNode = document.createTextNode(acc.institution + " " + acc.accountLabel);
                label.appendChild(labelTextNode);
                let labelContainerDiv = createEntryContainerDiv();
                // labelContainerDiv.appendChild(chartButton);
                labelContainerDiv.appendChild(label);

                let bvField = createInput("Book Value: " + acc.bookValue, "bv_input", bvId);
                let bvContainerDiv = createEntryContainerDiv();
                bvContainerDiv.appendChild(bvField);

                let mvField = createInput("Market Value: " + acc.marketValue, "mv_input", mvId);
                mvField.addEventListener('input', function (evt) {
                    updateBookValue(bvId, mvField.value, growthField.value);
                });
                let mvContainerDiv = createEntryContainerDiv();
                mvContainerDiv.appendChild(mvField);

                let growthField = createInput("Growth", "growth_input", growthId);
                growthField.addEventListener('input', function (evt) {
                    updateBookValue(bvId, mvField.value, growthField.value);
                });
                let growthContainerDiv = createEntryContainerDiv();
                growthContainerDiv.appendChild(growthField);


                accEntryDiv.appendChild(labelContainerDiv);
                accEntryDiv.appendChild(bvContainerDiv);
                accEntryDiv.appendChild(mvContainerDiv);
                accEntryDiv.appendChild(growthContainerDiv);
                formEntryDiv.appendChild(accEntryDiv);
            }
            form = document.getElementById("my_form");
        }
    }
}

function getAccountsFromEntries (accountEntries) {
    let result = [];
    for (let entry of accountEntries) {
        let account = entry.account;
        account.bookValue = entry.bookValue;
        account.marketValue = entry.marketValue;
        result.push(account);
    }
    return result;
}

function createInput (placeholderText, className, elementId) {
    let field = document.createElement("INPUT");
    field.setAttribute("placeholder", placeholderText);
    field.setAttribute("class", className);
    field.setAttribute("autocomplete", "off");
    field.setAttribute("type", "text");
    field.setAttribute("id", elementId);
    field.setAttribute("name", elementId);
    field.setAttribute("step", "0.01");
    field.setAttribute("form", "my_form");
    return field;
}

function updateBookValue (fieldId, marketValue, growthValue) {
    if (growthValue !== "" && marketValue !== "") {
        if (Number.isNaN(parseFloat(marketValue)) || Number.isNaN(parseFloat(growthValue))) {
            document.getElementById(fieldId).value = 0.00;
        } else {
            let bookVal = parseFloat(marketValue) - parseFloat(growthValue);
            document.getElementById(fieldId).value = (round(bookVal, 2));
        }
    }
}

function round(value, decimals) {
    return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}

function parseNumber(value, locales = navigator.languages) {
    const example = Intl.NumberFormat(locales).format('1.1');
    const alphaCharPattern = new RegExp(`[a-zA-Z]+`, 'g');
    let alphaArr = alphaCharPattern.exec(value);
    if (alphaArr === null || alphaArr.length === 0) {
        const cleanPattern = new RegExp(`[^-+0-9${ example.charAt( 1 ) }]`, 'g');
        const cleaned = value.replace(cleanPattern, '');
        const normalized = cleaned.replace(example.charAt(1), '.');

        return parseFloat(normalized);
    } else {
        return NaN;
    }
}

function createEntryContainerDiv () {
    let containerDiv = document.createElement("DIV");
    containerDiv.setAttribute("class", "entry_container");
    return containerDiv;
}

function getBalanceString (accountId) {
    const Http = new XMLHttpRequest();
    let url;
    if (accountId == null) {
        url = '/currentValue';
    } else if (!isNaN(accountId) && accountId > 0) {
        url = '/currentValue/' + accountId;
    } else {
        console.error("accountId variable past was not a valid number.");
    }
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        if (Http.readyState === 4 && Http.status === 200) {
            let div = document.getElementById("final_number")
            div.innerHTML = '';
            let newHeader = document.createElement("H2");
            newHeader.setAttribute("id", "balance_header")
            let headerText = document.createTextNode(Http.responseText);
            newHeader.appendChild(headerText);
            div.appendChild(newHeader);
            loadModal();
            loadChart(accountId);
        }
    }
}

function submitEntries ()  {
    collectEntries();
    let entriesToSubmit = [];
    for (let i = 0; i < accounts.length; i++) {
        let entry = accounts[i].entry;
        if (entry !== undefined && entry != null) {
            if (entry.bookValue !== undefined && entry.marketValue !== undefined && !Object.is(entry.bookValue,NaN) && !Object.is(entry.marketValue,NaN)){
                entriesToSubmit.push(entry);
            } else {
                console.log("Entry for account " + accounts[i].id + " is either missing a book value or market value.\n\t" + JSON.stringify(entry));
            }
        } else {
            console.log("No entry found for account " + accounts[i].id + "\n\t" + accounts[i]);
        }
    }

    if (entriesToSubmit.length > 0) {
        const xhr = new XMLHttpRequest();
        const url = '/entries';
        xhr.open("POST", url);
        xhr.setRequestHeader("Content-Type", "application/json");
        //    Http.data = entriesToSubmit;
        xhr.send(JSON.stringify(entriesToSubmit));
        xhr.onreadystatechange = (e) => {
            if (xhr.readyState === 4 && xhr.status === 200) {
                getBalanceString(null);
                form.reset();
            }
        }
    } else {
        let div = document.getElementById("final_number")
        div.innerHTML = '';
        let newHeader = document.createElement("H2");
        newHeader.setAttribute("id", "balance_header")
        let headerText = document.createTextNode("Nothing submitted. No valid entries");
        newHeader.appendChild(headerText);
        div.appendChild(newHeader);
    }
}

function collectEntries () {
    clearOldEntries();
    const FD = new FormData(form);
    console.log(FD);
    for (let formField of FD) {
        let accKey = formField[0];
        let inputValue = formField[1];
        let accountId = parseInt(accKey.split("_")[1]);
        let valueType = accKey.split("_")[2];
        if (inputValue !== undefined && inputValue != null && inputValue.trim() !== "" ){
            let inputValueNum = parseNumber(inputValue, "en-CA");
            let acc = getAccount(accountId);
            let accEntry = acc.entry;
            if (accEntry == null) {
                accEntry = createNewEntry(accountId);
            }
            if (!isNaN(inputValueNum)) {
                if (valueType === "mv") {
                    accEntry.marketValue = inputValueNum
                } else if (valueType === "bv") {
                    accEntry.bookValue = inputValueNum
                }
                acc.entry = accEntry;
                updateAccountWithEntry(acc);
            } else {
                console.log("Input value is NaN for account " + accountId + ". Skipping entry");
            }
        }
    }
}

function clearOldEntries () {
    for (let acc of accounts) {
        acc.entry = null;
    }
}

function createNewEntry (accountId) {
    return {
        entryDate: new Date(),
        marketValue: null,
        bookValue: null,
        account: {
            id: accountId
        }
    };
}

function getAccount (accountId) {
    for (let acc of accounts) {
        if (acc.id === accountId) { return acc}
    }
    return null;
}

function updateAccountWithEntry (account) {
    let idToUpdate = account.id;
    for (let i = 0; i < accounts.length; i++) {
        let acc = accounts[i];
        if (acc.id === idToUpdate){
            accounts[i] = account;
        }

    }
}


function loadModal () {
    if (modal == null) {modal = document.getElementById("myModal");}
    modal.style.display = "block";
}

function closeModal () {
    if (modal == null) {modal = document.getElementById("myModal");}
    modal.style.display = "none";
}


function loadChart (accountId) {
    let startDate = new Date ('2020-01-01').toJSON();
    let Http = new XMLHttpRequest();
    let url;
    if (accountId == null) {
        url = '/time_series_summary?startDate=' + startDate;
    } else if (accountId == NaN) {
        url = '/time_series_summary?startDate=' + startDate;
    } else {
        url = "/account_time_series_summary/" + accountId;
    }
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        if (Http.readyState === 4 && Http.status === 200) {
            let summaryEntryArr = JSON.parse(Http.responseText);
            let labels = [];
            let marketValues = [];
            let bookValues = [];
            for (let entry of summaryEntryArr) {
                if (entry == null || entry.marketValue == null){
                    console.error("There was a null value for this entry: " + entry);
                } else {
                    marketValues.push(entry.marketValue);
                    bookValues.push(entry.bookValue);
                    labels.push(entry.entryDate);
                }
            }

            let ctx = document.getElementById('myChart').getContext('2d');
            if (myChart) {myChart.destroy();}
            myChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Market Value',
                        data: marketValues,
                        fill: true,
                        borderColor: 'red',
                        tension: 0.3
                    },
                        {
                            label: "Book Value",
                            data: bookValues,
                            borderColor: 'rgb(75, 192, 192)',
                            fill: true
                        }
                    ]
                }
            });

        }
    }
}

function fetchAllData () {
    let Http = new XMLHttpRequest();
    let url='/pullAllData';
    let spinnerButton = document.getElementById("fetch_data");
    spinnerButton.classList.add("button--loading");
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        if (Http.readyState === 4 && Http.status === 200) {
            spinnerButton.classList.remove("button--loading");

        } else if (Http.readyState === 4 && Http.status !== 200){
            spinnerButton.classList.remove("button--loading");
            spinnerButton.textContent = "X - FAIL";
        }
    }
}


let submitBtn;
var myChart;
window.addEventListener( "load", function () {
    getAccounts();
    submitBtn = document.getElementById("submit");
    submitBtn.addEventListener( 'click', function()
      { submitEntries();
    } )
})

window.onclick = function(event) {
    if (event.target === modal) {
        closeModal();
    }
}
