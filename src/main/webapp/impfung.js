function Impfung() {

    let Name = document.getElementById("Name").value;

    if (!Name) {
        alert("Bitte füllen Sie alle Felder aus!");
        return false;

    } else {

    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (request.readyState == 4 && request.status == 200) {
            location.reload(true);
        } else if (request.status == 400) {
            alert(request.responseText);

        } else {
            document.getElementById("container").innerHTML = "<p> Etwas schief gelaufen. Bitte Seite neue laden</p>";
        }
    }
    request.open("POST", "Vaccines", true);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send("Name=" + Name);
}
    }

function Alleimpfungen() {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (request.readyState == 4) {
            if (request.status == 200) {
                if (request.responseURL.indexOf("user_login.html") > -1) {
                    location.reload(true);
                }

                let Center = JSON.parse(request.responseText);
                let table = document.getElementById("inhalt");
                let inhalte = "";
                for (tableRow of Center) {
                    inhalte += "<tr> <td> " + tableRow.name + "</td> </tr>";
                }
                table.innerHTML += inhalte;
            }
        }else {
            console.error("Fehler:", req.status, req.statusText); // Protokolliere Fehler
            document.querySelector(".container").innerHTML="<h2 style='color:red'>Fehler beim Hinzufügen des Impfungs</h2><br><a href='impfung.html'><button>Neue Vaccine Hinzufügen</button></button></a>";

               // document.getElementById("container").innerHTML = "<p>Interner Fehler,Bitte Laden Sie die Seite erneut.</p>";
            }
        }

    request.open("GET", "Vaccineslist");
    request.send();
}




window.onload = Alleimpfungen;

