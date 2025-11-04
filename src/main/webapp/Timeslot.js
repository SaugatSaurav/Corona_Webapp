window.onload=function load(){
    setup();
    Timeslot();

}

//document.addEventListener("DOMContentLoaded", ()=> {

    function setup() {

        let Starthour = document.getElementById("startHours");
        let Startminute = document.getElementById("startMinutes");
        let Endhour = document.getElementById("endHours");
        let Endminute = document.getElementById("endMinutes");
        let Capacity = document.getElementById("capacity");
        let ImpfId = document.getElementById("impfid");

        for (let i = 0; i < 60; i += 15) {
            Startminute.innerHTML += "<option value=" + i + ">" + i + "</option>"
            Endminute.innerHTML += "<option value=" + i + ">" + i + "</option>"
        }
        for (let i = 0; i < 24; i++) {
            Starthour.innerHTML += "<option value=" + i + ">" + i + "</option>"
            Endhour.innerHTML += "<option value=" + i + ">" + i + "</option>"
        }
        for (let i = 1; i <=60; i += 1) {
            Capacity.innerHTML += "<option value=" + i + ">" + i + "</option>"
        }

        for (let i = 1; i <=10; i += 1) {
            ImpfId.innerHTML += "<option value=" + i + ">" + i + "</option>"
        }


        let today = new Date();
        let todayString = today.toISOString().split("T")[0];
        let Datum = document.getElementById("date");
        Datum.value = todayString;
        Datum.setAttribute("min", todayString);
    }


function Timeslot() {
    let Center=document.getElementById("impfid").value;
  console.log("Center_id in Timeslot() ist",Center)
    console.log("Timeslot-Funktion wird aufgerufen!");
    let date = document.getElementById("date").value;
    console.log("Ausgewähltes Datum:", date);

    let req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            console.log("Response Status:", req.status);
            if (req.status === 200) {

                console.log("Rohdaten:", req.responseText);
                let timeslots = JSON.parse(req.responseText);
                console.log("Parsed Data:", timeslots);

            let inhalt = document.getElementById("inhalt");
            let inhalte = "";

            for (let slot of timeslots) {
                // Extrahiere Uhrzeit aus ISO-Strings
                const startTime = slot.start_time.split("T")[1].substring(0, 5); // "10:00"
                const endTime = slot.end_time.split("T")[1].substring(0, 5);     // "10:15"

                inhalte += `
                    <tr>
                        <td>${startTime}</td>
                        <td>${endTime}</td>
                        <td>${slot.capacity}</td>
                    </tr>
                `;
            }

            inhalt.innerHTML = inhalte;
        }
            }
        else{

            document.querySelector("#inhalt").innerHTML="<h2 style='color:red'>Gibts Nichts zu zeigen</h2>";

        }



    }
    req.open("GET", "Zeitslot?date=" + date
        +"&Center_id="+Center);
    req.send();
}

function alltimeslot() {
    let date = document.getElementById("date").value;
    let startTimeHour = parseInt(document.getElementById("startHours").value);
    let startTimeMinute = parseInt(document.getElementById("startMinutes").value);
    let endTimeHour = parseInt(document.getElementById("endHours").value);
    let endTimeMinute = parseInt(document.getElementById("endMinutes").value);
    let capacity = parseInt(document.getElementById("capacity").value);
    let Center=document.getElementById("center_id").value;
    console.log("Center Id in alltimeslot() ist"+Center);

    console.log("Eingaben:", { startTimeHour, startTimeMinute, endTimeHour, endTimeMinute });

    // Berechnung in Minuten
    let startTotal = startTimeHour * 60 + startTimeMinute;
    let endTotal = endTimeHour * 60 + endTimeMinute;

    console.log("StartTotal (Minuten):", startTotal);
    console.log("EndTotal (Minuten):", endTotal);



    if (endTotal <= startTotal) {
        alert("Endzeit muss nach der Startzeit liegen!");
        return;
    }

    let now = new Date();
    let selectedDate = new Date(date);
    let isToday = selectedDate.toDateString() === now.toDateString();

    if (isToday) {

        let startDateTime = new Date(selectedDate);
        startDateTime.setHours(startTimeHour, startTimeMinute, 0, 0);


        if (startDateTime < now) {
            alert("Fehler: Die Startzeit liegt in der Vergangenheit!");
            return;
        }
    }


    // Slots generieren (jeweils 15 Minuten)
    for (let slotStart = startTotal; slotStart < endTotal; slotStart += 15) {
        let slotEnd = slotStart + 15;

        // Umrechnung zurück in Stunden/Minuten
        let startH = Math.floor(slotStart / 60);
        let startM = slotStart % 60;
        let endH = Math.floor(slotEnd / 60);
        let endM = slotEnd % 60;

        // Request an Backend
        let req = new XMLHttpRequest();
        req.open("POST", "add-timeslot", true);
        req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        req.send(
            "start_time=" + buildDateString(date, startH, startM) +
            "&end_time=" + buildDateString(date, endH, endM) +
            "&capacity=" + capacity +
            "&Center_id="+Center
        );

        req.onreadystatechange = function () {
            if (req.readyState === 4) {
                console.log("Statuscode:", req.status); // Debug-Info
                if (req.status === 201) {

                    document.querySelector(".container").innerHTML="<h2 style='color:green'>Zeitslot erfolgreich hinzugefügt</h2><br><a href='Timeslot.html'><button>Neue Zeitslot Hinzufügen</button></button></a>";
                    Timeslot();
                } else if (req.status === 409) {
                    document.querySelector(".container").innerHTML = "<h2 style='color:red'>Zeitslot existiert bereits!</h2><br><a href='Timeslot.html'><button>Neue Zeitslot Hinzufügen</button></button></a>";

                } else if (req.status === 400) {
                    console.error("Ungültige Eingabe:",req.responseText);
                    alert("Ungültige Eingabe: " + req.responseText); // Zeige Serverantwort
                } else {
                    console.error("Fehler:", req.status, req.statusText); // Protokolliere Fehler
                    document.querySelector(".container").innerHTML="<h2 style='color:red'>Fehler beim Hinzufügen des Zeitslots</h2><br><a href='Timeslot.html'><button>Neue Zeitslot Hinzufügen</button></button></a>";

                }
            }
        };
    }
}

      function buildDateString(date, hour, minute) {
          return date + "T" + ((hour < 10) ? "0" + hour : hour) + ":" + ((minute < 10) ? "0" + minute : minute) + ":00";
      }



