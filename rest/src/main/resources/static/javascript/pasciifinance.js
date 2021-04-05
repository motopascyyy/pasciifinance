let accounts;
let form;

function getAccounts () {
    const Http = new XMLHttpRequest();
    const url='/accounts?isActive=true';
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        if (Http.readyState === 4 && Http.status === 200) {
            accounts = JSON.parse(Http.responseText);
            let formEntryDiv = document.getElementById("form_entry_area");
            formEntryDiv.innerHTML = '';
            for (let acc of accounts) {
                let accEntryDiv = document.createElement("DIV");
                accEntryDiv.setAttribute("class", "account_entry_item");

                let label = document.createElement("LABEL");
                label.setAttribute("class", "entry_label");
                let bvId = acc.institution + "_" + acc.id + "_bv";
                let mvId = acc.institution + "_" + acc.id + "_mv";
                let growthId = acc.institution + "_" + acc.id + "_gv";
                label.setAttribute("for", bvId);
                let labelTextNode = document.createTextNode(acc.institution + " " + acc.accountLabel);
                label.appendChild(labelTextNode);
                let labelContainerDiv = createEntryContainerDiv();
                labelContainerDiv.appendChild(label);

                let bvField = createInput("Book Value", "bv_input", bvId);
                let bvContainerDiv = createEntryContainerDiv();
                bvContainerDiv.appendChild(bvField);

                let mvField = createInput("Market Value", "mv_input", mvId);
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

function createInput (placeholderText, className, elementId) {
    let field = document.createElement("INPUT");
    field.setAttribute("placeholder", placeholderText);
    field.setAttribute("class", className);
    field.setAttribute("autocomplete", "off");
    field.setAttribute("type", "number");
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
            document.getElementById(fieldId).value = (parseFloat(marketValue) - parseFloat(growthValue));
        }
    }
}

function createEntryContainerDiv () {
    let containerDiv = document.createElement("DIV");
    containerDiv.setAttribute("class", "entry_container");
    return containerDiv;
}

function getBalanceString () {
    const Http = new XMLHttpRequest();
    const url='/currentValue';
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
        }
    }
}

function submitEntries ()  {
    collectEntries();
    let entriesToSubmit = [];
    for (let i = 0; i < accounts.length; i++) {
        let entry = accounts[i].entry;
        if (entry !== undefined && entry != null) {
            if (entry.bookValue !== undefined && entry.marketValue !== undefined){
                entriesToSubmit.push(entry);
            } else {
                console.log("Entry for account " + accounts[i].id + " is either missing a book value or market value.\n\t" + JSON.stringify(entry));
            }
        } else {
            console.log("No entry found for account " + accounts[i].id + "\n\t" + accounts[i]);
        }
    }

    const xhr = new XMLHttpRequest();
    const url='/entries';
    xhr.open("POST", url);
    xhr.setRequestHeader("Content-Type", "application/json");
//    Http.data = entriesToSubmit;
    xhr.send(JSON.stringify(entriesToSubmit));
    xhr.onreadystatechange = (e) => {
        if (xhr.readyState === 4 && xhr.status === 200) {
            getBalanceString();
        }
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
            let acc = getAccount(accountId);
            let accEntry = acc.entry;
            if (accEntry == null) {
                accEntry = createNewEntry(accountId);
            }
            if (valueType === "mv"){accEntry.marketValue = inputValue}
            else {accEntry.bookValue = inputValue}
            acc.entry = accEntry;
            updateAccountWithEntry(acc);
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

let submitBtn;

window.addEventListener( "load", function () {
    getAccounts();
    submitBtn = document.getElementById("submit");
    submitBtn.addEventListener( 'click', function()
      { submitEntries();
    } )
})
