    function Impfzentren() {

        let Name = document.getElementById("Name").value.trim();
        let Strasse = document.getElementById("Strasse").value.trim();
        let Stadt = document.getElementById("stadt").value.trim();
        let Plz = document.getElementById("postal").value.trim();


        if (!Name  || !Stadt  || !Strasse  || !Plz ) {
            alert("Bitte füllen Sie alle Felder aus!");
            return false;

        } else {
            let request = new XMLHttpRequest();
            request.onreadystatechange = function () {
                if (request.readyState == 4) {
                    if (request.status == 200) {

                        location.reload(true);
                    } else if (request.status == 400) {
                        alert(request.responseText);
                    } else {
                        document.getElementsByClassName("container").innerHTML = "<h2>Etwas Schief gelaufen</h2>";
                    }
                }
            }
            request.open("POST", "Centers", true);
            request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
            request.send("name=" + Name + "&strasse=" + Strasse + "&stadt=" + Stadt + "&postal=" + Plz);
        }
        document.querySelector('form').reset();
        return true;
    }
    document.querySelector("form").addEventListener("submit", Impfzentren);

function loadVaccinationCenters() {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
    if (request.readyState == 4) {
        if (request.status == 200) {
            if (request.responseURL.indexOf("user_login.html") > -1) {
                location.reload(true);
            }
            let centers = JSON.parse(request.responseText);


            let table = document.getElementById("inhalt");
            let inhalte = "";
            for (tableRow of centers) {
                inhalte += "<tr> <td> " + tableRow.name + "</td> <td> " + tableRow.stadt + " </td><td>" +
                    tableRow.strasse + "</td><td>" + tableRow.postal + "</td></tr>";
            }
            table.innerHTML += inhalte;
        }
    }    else {
        console.error("Fehler:", req.status, req.statusText); // Protokolliere Fehler
        document.querySelector(".container").innerHTML="<h2 style='color:red'>Fehler beim Hinzufügen des Impfzentrums</h2><br><a href='impfzentren.html'><button>Neue Impfzentrum Hinzufügen</button></button></a>";

    //document.getElementsByClassName("container").innerHTML = "<p>Etwas schief gelaufen. Bitte Seite neue laden</p>";
     }
  }

 request.open("GET", "Centerliste");
    request.send();
}


    window.onload = loadVaccinationCenters;





