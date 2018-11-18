const
    submit = document.getElementById("submit"),
    name = document.getElementById("name");
submit.addEventListener("click", function(e) {
    let requestBody = {
        name: name.value
    },
        xhr = new XMLHttpRequest();
    xhr.open("POST", LISE_REST_URL + "publishers/creation", true);
    xhr.addEventListener("load", function(e) {
        let response = JSON.parse(xhr.response);
        window.open(LISE_ADM_URL + "publishers/" + response.id, "_self", false);
    });
    xhr.setRequestHeader("Access-token", getAccessToken());
    xhr.setRequestHeader("Login", getLogin());
    xhr.send(JSON.stringify(requestBody));
});