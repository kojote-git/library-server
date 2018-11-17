const
    submit = document.getElementById("submit"),
    login = document.getElementById("login"),
    password = document.getElementById("password"),
    form = document.querySelector(".entity-form")

submit.addEventListener("click", authorize);

form.addEventListener("keydown", function(e) {
   if (e.keyCode === 13)
       authorize(e);
});

function authorize(e) {
    let tLogin = login.value,
        tPassword = password.value,
        xhr = new XMLHttpRequest();
    xhr.open("POST", LISE_ADM_URL+"authorization");
    xhr.setRequestHeader("Login", tLogin);
    xhr.setRequestHeader("Password", tPassword);
    xhr.addEventListener("load", function (e) {
        if (xhr.status !== 200) {
            alert(JSON.parse(xhr.response).error);
        } else {
            window.open(LISE_ADM_URL+"admin-page", "_self", false);
        }
    });
    xhr.send();
}