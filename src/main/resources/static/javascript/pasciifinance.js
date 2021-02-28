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
                label.setAttribute("for", bvId);
                let labelTextNode = document.createTextNode(acc.institution + " " + acc.accountLabel);
                label.appendChild(labelTextNode);
                let labelContainerDiv = createEntryContainerDiv();
                labelContainerDiv.appendChild(label);

                let bvField = document.createElement("INPUT");
                bvField.setAttribute("placeholder", "Book Value");
                bvField.setAttribute("class", "bv_input");
                bvField.setAttribute("autocomplete", "off");
                bvField.setAttribute("type", "number");
                bvField.setAttribute("id", bvId);
                bvField.setAttribute("name", bvId);
                bvField.setAttribute("form", "my_form");
                let bvContainerDiv = createEntryContainerDiv();
                bvContainerDiv.appendChild(bvField);


                let mvField = document.createElement("INPUT");
                mvField.setAttribute("class", "mv_input");
                mvField.setAttribute("placeholder", "Market Value");
                mvField.setAttribute("autocomplete", "off");
                mvField.setAttribute("type", "number");
                mvField.setAttribute("id", mvId);
                mvField.setAttribute("name", mvId);
                mvField.setAttribute("form", "my_form");
                let mvContainerDiv = createEntryContainerDiv();
                mvContainerDiv.appendChild(mvField);


                accEntryDiv.appendChild(labelContainerDiv);
                accEntryDiv.appendChild(bvContainerDiv);
                accEntryDiv.appendChild(mvContainerDiv);
                formEntryDiv.appendChild(accEntryDiv);
            }
            form = document.getElementById("my_form");
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
        console.log (formField);
        let accKey = formField[0];
        let inputValue = formField[1];
        let accountId = parseInt(accKey.split("_")[1]);
        let valueType = accKey.split("_")[2];
        console.log(accountId);
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
