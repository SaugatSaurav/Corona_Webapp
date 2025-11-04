window.onload=load;

function load(){
    loadbuchungen();
}


function loadbuchungen() {

    let request = new XMLHttpRequest();

    request.onreadystatechange = function () {

        if (request.readyState == 4 && request.status == 200) {
            if (request.responseURL.indexOf("user_login.html") > -1) {
                location.reload(true);

            }

            try {
                let table = JSON.parse(request.responseText);
                console.log(request.responseText);

                let inhalt = document.getElementById("inhalt");
                let inhalte = "";

                for (tableRow of table) {
                    let datetime_start = tableRow.startTime.split("T");
                    let datetime_end = tableRow.endTime.split("T");

                    inhalte += "<tr><td>"
                        + tableRow.id + "</td><td>"
                        + tableRow.vorname + "</td><td>"
                        + tableRow.nachname + "</td><td>"
                        + tableRow.addresse + "</td><td>"
                        + tableRow.impfstoff + "</td><td>"
                        + tableRow.impfzentrum + "</td><td>"
                        + datetime_start[0] + "</td><td>"
                        + datetime_start[1].substring(0, 5) + " bis " + datetime_end[1].substring(0, 5) + "</td>";

                    inhalte += "<td><button onclick='deleteAppointment(" + tableRow.id + ")'>Stornierung</button></td>";
                    inhalte += "<td><button onclick='downloadAppointment(" + tableRow.id + ")'>Download</button></td>";
                    inhalte += "</tr>";
                }
                inhalt.innerHTML += inhalte;
                console.log("Anzahl der gefundenen Buchungen:", table.length);
            } catch (error) {
                console.error("Fehler beim Verarbeiten der Antwort: ", error);


            }
        }
  //        else if (request.readyState == 4){
//               console.error("Fehler bei der Anfrage: " + request.statusText);
              // alert("Fehler beim Laden der Daten: " + request.statusText);

           // document.getElementById("content").innerHTML = "<p>Etwas Schief gelaufen</p>";}
    }


    request.open("GET", "appointment");
    request.send();
}



    function deleteAppointment(appointmentId) {
        console.log("Lösche Termin mit ID: " + appointmentId);

        let request = new XMLHttpRequest();
        request.onreadystatechange = function () {
            if (request.readyState == 4 && request.status == 200) {
                if (request.responseURL.indexOf("user_login.html") > -1) {
                    location.reload(true);


                }
                if (request.status == 400) {
                    location.reload(true);
                    alert(request.responseText);

                }
                let Data = JSON.parse(request.responseText);
                console.log(Data.status);

                alert(Data.message);

                loadbuchungen();
            }

        }
        request.open("GET", "DeleteAppointment?id=" + appointmentId, true);
        request.send();

    }

// Beispiel für das Downloaden eines Termins (Download-Link oder PDF)
    function downloadAppointment(appointmentId) {
        let url = "PDF?id=" + appointmentId;

        // Die Datei wird direkt im Browser heruntergeladen, indem die URL im "window.location" gesetzt wird
        window.location.href = url;

    }