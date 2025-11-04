let Extrainfo= document.getElementById("extrainfo");
let leftup=document.querySelector("#leftup");
let rightup=document.querySelector("#rightup");




leftup.addEventListener('click',() => {
    Extrainfo.style.display = "none";
});

rightup.addEventListener('click',() => {
    Extrainfo.style.display = "block";
});

let Center_id;




function setup(){
    let Today=new Date();
    let Todaysplit= Today.toISOString().split("T")[0];
    let Todaydate=document.getElementById("datum");
    Todaydate.value= Todaysplit;
    Todaydate.setAttribute("min",Todaysplit);
}


function impfzentren(){
    let request=new XMLHttpRequest();

    request.onreadystatechange= function() {
        if(request.readyState==4 && request.status==200){
            if(request.responseURL.indexOf("user_login.html") > -1) {
            location.reload(true);
        }

let all=JSON.parse(request.responseText);
let impfzentren= document.getElementById("ort");

impfzentren.innerHTML = '<option value="">Impfzentrum auswählen</option>';
let Selected="";

for(selectEntry of all){
Selected +=  "<option value=" + selectEntry.id + "> " +
    selectEntry.name + ", " + selectEntry.strasse + ", " +
    selectEntry.postal + ", " + selectEntry.stadt +
    "</option>";
}
            if (all.length === 0) {
                Selected += '<option disabled>Keine verfügbaren Impfzentren</option>';
            }

impfzentren.innerHTML += Selected;
            document.getElementById("ort").addEventListener("change", function() {
                Center_id = impfzentren.value;
                impfung();
                timeslot();
            });

        }
    }
request.open("Get","Centerliste");
    request.send();

}

function impfung(){
    let request= new XMLHttpRequest();
    request.onreadystatechange= function (){
        if(request.readyState==4 && request.status==200){
            if(request.responseURL.indexOf("login.html") > -1) {
                location.reload(true);

            }
            let alle=JSON.parse(request.responseText);
            let inhalt=document.getElementById("impfung");
            inhalt.innerHTML = '<option value="">Impfstoff auswählen</option>';
            let inhalte="";

            for(selectEntry of alle) {
                inhalte += "<option value=" + selectEntry.id + "> " +
                    selectEntry.name +
                    "</option>";
            }
            if (alle.length === 0) {
                inhalte += '<option disabled>Keine verfügbaren Impfungen</option>';
            }

inhalt.innerHTML+=inhalte;

            }
        }

    request.open("Get","VerfügbareVaccineslist?Center_id="+Center_id);

        request.send();

    }
function timeslot() {
    let date = document.getElementById("datum").value;
    let req = new XMLHttpRequest();

    req.onreadystatechange = function () {
        if (req.readyState == 4 && req.status == 200) {
            if (req.responseURL.indexOf("user_login.html") > -1) {
                location.reload(true);
                return;
            }



            let loadresponsetext = JSON.parse(req.responseText);
            let inhalt = document.getElementById("zeit");


            inhalt.innerHTML = '<option value="">Zeitslot auswählen</option>';

            let inhalte = "";
            for(let selectEntry of loadresponsetext) {


                const startTime = selectEntry.start_time.split("T")[1].substring(0, 5);

                inhalte += `<option value="${selectEntry.id}">${startTime}</option>`;
            }


            if (loadresponsetext.length === 0) {
                inhalte += '<option disabled>Keine verfügbaren ZeitSlots</option>';
            }

            inhalt.innerHTML += inhalte;
        }
    }
    console.log("Sende Anfrage mit Center_id: " + Center_id);
    console.log("Go Datum"+date);

    req.open("GET", `Zeitslot?date=${date}&Center_id=${Center_id}`);
    req.send();
}


function Buchen(){
    let Buchen={};   //JS OBJEKT
    Buchen.vorname=document.getElementById("vname").value;
    Buchen.nachname= document.getElementById("nname").value;
    Buchen.addresse= document.getElementById("address").value;
    Buchen.centerId= document.getElementById("ort").value;
    Buchen.vaccineId = document.getElementById("impfung").value;
    Buchen.timeslotId=document.getElementById("zeit").value;


    console.log(Buchen.vorname);
    console.log(Buchen.nachname);
    console.log(Buchen.addresse);
    console.log(Buchen.centerId);
    console.log(Buchen.vaccineId);
    console.log(Buchen.timeslotId);


let request=new XMLHttpRequest();
request.onreadystatechange=function (){
if(request.readyState == 4) {
    if (request.status == 200) {
        if (request.responseURL.indexOf("user_login.html") > -1) {
            //location.reload(true);
            document.querySelector(".container").innerHTML = "<h2 style='color:green'>Buchung Erfolgreich!</h2> <h2 style='color:orchid'> Sie bekommen eine Mail und da erhalten Sie Ihre Terminbestätigung mit PDF und QR-Code </h2><h2 style='color:orchid'>Klicken Sie hier, um mehr Termin zu Buchen</h2><br><a href='buchen.html'><button>Termin Buchen</button></a>";

    }
    }

    } else if(request.status === 409) {//Conflict
    const responseText = request.responseText.trim();

    if (responseText === "personal_data_conflict") {
        document.querySelector(".container").innerHTML = `
            <h2 style='color:red'>Ein Termin mit diesen Daten existiert bereits.</h2>
            <button onclick="window.location.reload()">Neue Termin Buchen</button>
        `;
    }
    else if(responseText === "center_capacity_full") {
        document.querySelector(".container").innerHTML = `
        <h2 style='color:red'>Das Center hat die Kapazität schon voll!</h2>
        <button onclick="window.location.reload()">Neue Termin Buchen</button>
    `;
    }
    else if (responseText === "timeslot_already_booked") {
        document.querySelector(".container").innerHTML = `
            <h2 style='color:red'>Ausgewähltes Zeitslot ist bereits gebucht</h2>
            <button onclick="window.location.href='buchen.html'">Neue Termin Buchen</button>`;}



}else if(request.status === 403) {  //Forbidden
    document.querySelector(".container").innerHTML = `<h2 style='color:red'>${request.responseText}</h2><br><br><a href='buchen.html'><button>Neue Termin Buchen</button></a>`;

}
else if (request.status === 500) {
    console.log("Fehler Response: " + request.responseText);
    document.querySelector(".container").innerHTML = "<h2 style='color:red'>Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.</h2>";
}
else {document.querySelector(".container").innerHTML = "<h2>Warten Sie bitte! Dauert..</h2>";}
}
 request.open("POST","Buchen");
 request.setRequestHeader("Content-Type","application/json")
 request.send(JSON.stringify(Buchen));
    console.log("Sende Daten:", JSON.stringify(Buchen));

    request.onerror = function() {
        console.error("Request fehlgeschlagen");
    };

}


window.onload=function load(){
    setup();
    impfzentren();


}
function all(){
    Center_id= document.getElementById("ort").value;
    console.log("Aktueller Center_id: " + Center_id);
    impfung();
   timeslot();

}




