
const queryString = window.location.search;

// Parse the query string
const urlParams = new URLSearchParams(queryString);

const error= urlParams.get("error");



if (error === "1") {
    const errors = document.getElementById("error-container");
    errors.textContent = "Registration nicht Erfolgreich";

}
else if(error==="2"){
    const errors = document.getElementById("error-container");
    errors.textContent = "Email schon registriert!";
}
else if(error === "4"){
    const errors = document.getElementById("error-container");
    errors.textContent = "Passwörter stimmen nicht überein";

}

else if(error==="3"){


    const errors = document.querySelector(".container_registrieren");

    if (errors) {

        errors.innerHTML = "<b>Registrierung Erfolgreich!</b><br><br><i style='color:orchid'>Sie werden die Bestätigung per Mail bekommen.</i><br>";
        errors.style.color = "green";
        errors.style.fontSize="30px";

       // const link = document.createElement("a");
       // link.href = "user_login.html";


        //    const button = document.createElement("button");
        //     button.textContent = "Einloggen!";
        //   button.className = "button";
        //   link.appendChild(button);
        // errors.appendChild(link);
    }
}

document.querySelector("form").addEventListener("submit", function(event) {



    // Email Validation
    const email = document.getElementById("email").value;

  //  const emailRegex = /^[a-zA-Z0-9]+@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)\.)+[a-z]{2,}$/;
    const emailRegex = /^[a-zA-Z0-9]+@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-z]{2,}$/;



    //vor @ ist lokal und nach ist domain

    const password = document.getElementById("password").value;


    const passwordRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)[A-Za-z\d]{8,15}$/;




    const errorContainer = document.getElementById("error-container");
    errorContainer.textContent = "";

    // Email validation
    if(!emailRegex.test(email)) {
        errorContainer.textContent = "Ungültige Email-Adresse!";
        event.preventDefault();
        return;
    }

    // Password validation
    if(!passwordRegex.test(password)) {
        errorContainer.textContent = "Passwort muss erhalten:- 8-15 Zeichen: mind. ein Zahl , ein Groß Buchstabe und ein Klein Buchstabe";
        event.preventDefault();
        return;
    }


});


