const
    submit = document.getElementById("submit"),
    login = document.getElementById("login"),
    password = document.getElementById("password");

submit.addEventListener("click", function (e) {
    let tLogin = login.value,
        tPassword = password.value,
        xhr = new XMLHttpRequest();
    xhr.open("POST", LISE_ADM_URL+"/authorization");
    xhr.setRequestHeader("Login", tLogin);
    xhr.setRequestHeader("Password", tPassword);
    xhr.addEventListener("load", function (e) {
        if (xhr.status !== 200) {
            alert(JSON.parse(xhr.response).error);
        } else {
            window.open(LISE_ADM_URL + "admin-page", "_self", false);
        }
    });
    xhr.send();
});