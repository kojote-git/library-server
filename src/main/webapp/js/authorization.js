const
    submit = document.getElementById("submit"),
    login = document.getElementById("login"),
    password = document.getElementById("password");

submit.addEventListener("click", function (e) {
    let tLogin = login.value,
        tPassword = password.value,
        xhr = new XMLHttpRequest();
    xhr.open("POST", LISE_ADM_URL+"/authorization");
    xhr.setRequestHeader("Credentials", tLogin+":"+tPassword);
    xhr.addEventListener("load", function (e) {
       console.log(e);
    });
    xhr.send();
});