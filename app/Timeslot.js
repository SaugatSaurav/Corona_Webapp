const OPENING_TIME = 8 * 60;
const CLOSING_TIME = 18 * 60;
const SLOT_DURATION = 15;

window.addEventListener("load", function () {
    const dateInput = document.getElementById("date");
    const today = formatLocalDate(new Date());

    dateInput.min = today;
    dateInput.value = today;

    dateInput.addEventListener("change", function () {
        createStartTimes();
        loadTimeslots();
    });

    document
        .getElementById("startTime")
        .addEventListener("change", createEndTimes);

    document
        .getElementById("impfid")
        .addEventListener("change", loadTimeslots);

    loadCenters();
    createStartTimes();

    /*
     * Buchungen und Stornierungen
     * automatisch aktualisieren.
     */
    setInterval(loadTimeslots, 15000);
});


function formatLocalDate(date) {
    const year = date.getFullYear();

    const month = String(
        date.getMonth() + 1
    ).padStart(2, "0");

    const day = String(
        date.getDate()
    ).padStart(2, "0");

    return year + "-" + month + "-" + day;
}


function formatTime(totalMinutes) {
    const hours =
        Math.floor(totalMinutes / 60);

    const minutes =
        totalMinutes % 60;

    return String(hours).padStart(2, "0")
        + ":"
        + String(minutes).padStart(2, "0");
}


function timeToMinutes(time) {
    if (!time) {
        return 0;
    }

    const parts = time.split(":");

    return parseInt(parts[0], 10) * 60
        + parseInt(parts[1], 10);
}


function getFirstStartTime() {
    const selectedDate =
        document.getElementById("date").value;

    const today =
        formatLocalDate(new Date());

    /*
     * Bei einem zukünftigen Datum
     * beginnt die Auswahl um 08:00 Uhr.
     */
    if (selectedDate !== today) {
        return OPENING_TIME;
    }

    /*
     * Beim heutigen Datum wird auf die
     * nächste Viertelstunde aufgerundet.
     *
     * 16:00 -> 16:00
     * 16:01 -> 16:15
     * 16:16 -> 16:30
     */
    const now = new Date();

    let currentMinutes =
        now.getHours() * 60
        + now.getMinutes();

    if (
        now.getSeconds() > 0
        || now.getMilliseconds() > 0
    ) {
        currentMinutes++;
    }

    const roundedTime =
        Math.ceil(
            currentMinutes / SLOT_DURATION
        ) * SLOT_DURATION;

    return Math.max(
        OPENING_TIME,
        roundedTime
    );
}


function createStartTimes() {
    const startSelect =
        document.getElementById("startTime");

    const endSelect =
        document.getElementById("endTime");

    startSelect.disabled = false;
    endSelect.disabled = false;

    /*
     * Standardtexte beim Öffnen
     * und beim Wechsel des Datums.
     */
    startSelect.innerHTML =
        '<option value="" selected disabled>'
        + 'Start-Uhrzeit auswählen'
        + '</option>';

    endSelect.innerHTML =
        '<option value="" selected disabled>'
        + 'End-Uhrzeit auswählen'
        + '</option>';

    const firstStartTime =
        getFirstStartTime();

    /*
     * 17:45 Uhr ist die letzte mögliche
     * Startzeit für einen 15-Minuten-Termin.
     */
    if (
        firstStartTime
        > CLOSING_TIME - SLOT_DURATION
    ) {
        startSelect.innerHTML =
            '<option value="" selected>'
            + 'Für heute können keine Zeitslots '
            + 'mehr hinzugefügt werden'
            + '</option>';

        endSelect.innerHTML =
            '<option value="" selected>'
            + 'Für heute können keine Zeitslots '
            + 'mehr hinzugefügt werden'
            + '</option>';

        startSelect.disabled = true;
        endSelect.disabled = true;

        return;
    }

    /*
     * Startzeiten vom frühesten erlaubten
     * Zeitpunkt bis 17:45 Uhr erstellen.
     */
    for (
        let minutes = firstStartTime;
        minutes <= CLOSING_TIME - SLOT_DURATION;
        minutes += SLOT_DURATION
    ) {
        const option =
            document.createElement("option");

        option.value =
            formatTime(minutes);

        option.textContent =
            formatTime(minutes);

        startSelect.appendChild(option);
    }
}


function createEndTimes() {
    const startTime =
        document.getElementById("startTime").value;

    const endSelect =
        document.getElementById("endTime");

    endSelect.disabled = false;

    /*
     * Standardtext anzeigen, bis eine
     * Startzeit ausgewählt wurde.
     */
    endSelect.innerHTML =
        '<option value="" selected disabled>'
        + 'End-Uhrzeit auswählen'
        + '</option>';

    if (!startTime) {
        return;
    }

    const startMinutes =
        timeToMinutes(startTime);

    /*
     * Die erste Endzeit ist 15 Minuten
     * nach der ausgewählten Startzeit.
     */
    for (
        let minutes = startMinutes + SLOT_DURATION;
        minutes <= CLOSING_TIME;
        minutes += SLOT_DURATION
    ) {
        const option =
            document.createElement("option");

        option.value =
            formatTime(minutes);

        option.textContent =
            formatTime(minutes);

        endSelect.appendChild(option);
    }
}


function loadCenters() {
    const request =
        new XMLHttpRequest();

    request.onreadystatechange = function () {
        if (request.readyState !== 4) {
            return;
        }

        if (request.status === 200) {
            const centers =
                JSON.parse(request.responseText);

            const addCenter =
                document.getElementById("center_id");

            const viewCenter =
                document.getElementById("impfid");

            addCenter.innerHTML =
                '<option value="">'
                + 'Impfzentrum auswählen'
                + '</option>';

            viewCenter.innerHTML =
                '<option value="">'
                + 'Impfzentrum auswählen'
                + '</option>';

            for (const center of centers) {
                const label =
                    center.id
                    + " – "
                    + center.name;

                addCenter.innerHTML +=
                    '<option value="'
                    + center.id
                    + '">'
                    + label
                    + '</option>';

                viewCenter.innerHTML +=
                    '<option value="'
                    + center.id
                    + '">'
                    + label
                    + '</option>';
            }
        }
    };

    request.open(
        "GET",
        "Centerliste",
        true
    );

    request.send();
}


function addTimeslots() {
    const date =
        document.getElementById("date").value;

    const startTime =
        document.getElementById("startTime").value;

    const endTime =
        document.getElementById("endTime").value;

    const centerId =
        document.getElementById("center_id").value;

    if (
        !date
        || !startTime
        || !endTime
        || !centerId
    ) {
        alert(
            "Bitte füllen Sie alle Felder aus."
        );
        return;
    }

    const startMinutes =
        timeToMinutes(startTime);

    const endMinutes =
        timeToMinutes(endTime);

    if (startMinutes < OPENING_TIME) {
        alert(
            "Die Startzeit darf nicht vor "
            + "08:00 Uhr liegen."
        );
        return;
    }

    if (endMinutes > CLOSING_TIME) {
        alert(
            "Die Endzeit darf nicht nach "
            + "18:00 Uhr liegen."
        );
        return;
    }

    if (endMinutes <= startMinutes) {
        alert(
            "Die Endzeit muss nach der "
            + "Startzeit liegen."
        );
        return;
    }

    const request =
        new XMLHttpRequest();

    request.onreadystatechange = function () {
        if (request.readyState !== 4) {
            return;
        }

        if (request.status === 201) {
            alert(request.responseText);

            /*
             * Das verwendete Impfzentrum
             * automatisch für die Tabelle auswählen.
             */
            document.getElementById("impfid").value =
                centerId;

            loadTimeslots();
            createStartTimes();

        } else if (
            request.status === 409
            || request.status === 400
        ) {
            alert(request.responseText);

        } else if (request.status !== 401) {
            alert(
                "Die Zeitslots konnten nicht "
                + "gespeichert werden."
            );
        }
    };

    request.open(
        "POST",
        "add-timeslot",
        true
    );

    request.setRequestHeader(
        "Content-Type",
        "application/x-www-form-urlencoded"
    );

    const body =
        new URLSearchParams();

    body.append(
        "start_time",
        date + "T" + startTime + ":00"
    );

    body.append(
        "end_time",
        date + "T" + endTime + ":00"
    );

    body.append(
        "Center_id",
        centerId
    );

    request.send(body.toString());
}


function loadTimeslots() {
    const centerId =
        document.getElementById("impfid").value;

    const date =
        document.getElementById("date").value;

    const table =
        document.getElementById("inhalt");

    const summary =
        document.getElementById("availableSummary");

    if (!centerId || !date) {
        table.innerHTML = "";

        summary.textContent =
            "Freie Kapazität: 0";

        return;
    }

    const request =
        new XMLHttpRequest();

    request.onreadystatechange = function () {
        if (request.readyState !== 4) {
            return;
        }

        if (request.status === 200) {
            const timeslots =
                JSON.parse(request.responseText);

            /*
             * Anzahl der freien Zeitslots
             * entspricht der freien Kapazität.
             */
            summary.textContent =
                "Freie Kapazität: "
                + timeslots.length;

            if (timeslots.length === 0) {
                table.innerHTML =
                    "<tr>"
                    + '<td colspan="3">'
                    + "Keine freien Zeitslots vorhanden"
                    + "</td>"
                    + "</tr>";

                return;
            }

            let rows = "";

            for (const slot of timeslots) {
                const startTime =
                    slot.start_time
                        .split("T")[1]
                        .substring(0, 5);

                const endTime =
                    slot.end_time
                        .split("T")[1]
                        .substring(0, 5);

                rows +=
                    "<tr>"
                    + "<td>" + slot.id + "</td>"
                    + "<td>" + startTime + "</td>"
                    + "<td>" + endTime + "</td>"
                    + "</tr>";
            }

            table.innerHTML = rows;

        } else if (request.status !== 401) {
            summary.textContent =
                "Freie Kapazität: 0";

            table.innerHTML =
                "<tr>"
                + '<td colspan="3">'
                + "Zeitslots konnten nicht geladen werden"
                + "</td>"
                + "</tr>";
        }
    };

    request.open(
        "GET",
        "Zeitslot?date="
        + encodeURIComponent(date)
        + "&Center_id="
        + encodeURIComponent(centerId),
        true
    );

    request.send();
}
