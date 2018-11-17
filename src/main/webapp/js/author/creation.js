const submit = document.getElementById("submit"),
    firstName = document.getElementById("first-name"),
    middleName = document.getElementById("middle-name"),
    lastName = document.getElementById("last-name");
submit.addEventListener("click", function (e) {
    let xhr = new XMLHttpRequest(),
        requestBody = {
            firstName: firstName.value,
            middleName: middleName.value,
            lastName: lastName.value
        };
    xhr.open("POST", LISE_REST_URL + "authors/creation", false);
    xhr.setRequestHeader("Access-token", getAccessToken());
    xhr.setRequestHeader("Login", getLogin());
    xhr.addEventListener("load", function (e) {
        let resp = JSON.parse(xhr.response);
        window.open(LISE_ADM_URL + "authors/" + resp.id, "_self", false);
    });
    xhr.send(JSON.stringify(requestBody));
});