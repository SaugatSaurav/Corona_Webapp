(function () {
    const script = document.currentScript;
    const loginPage =
        script.dataset.login || "user_login.html";

    const originalOpen = XMLHttpRequest.prototype.open;
    let redirecting = false;

    XMLHttpRequest.prototype.open = function () {
        this.addEventListener("loadend", function () {
            if (this.status === 401 && !redirecting) {
                redirecting = true;

                alert("Sie sind nicht eingeloggt.");
                window.location.replace(loginPage);
            }
        });

        return originalOpen.apply(this, arguments);
    };
})();
