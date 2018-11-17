const
    submit = document.getElementById("submit"),
    title = document.getElementById("title");
submit.addEventListener("click", function (e) {
    let xhr = new XMLHttpRequest(),
        requestBody = {
            title: title.value,
            description: "",
            authors: [],
            subjects: []
        };
    xhr.open("POST", LISE_REST_URL + "works/creation", false);
    xhr.setRequestHeader("Access-token", getAccessToken());
    xhr.setRequestHeader("Login", getLogin());
    xhr.addEventListener("load", function (e) {
        let response = JSON.parse(xhr.response);
        window.open(LISE_ADM_URL + "works/" + response.id, "_self", false);
    });
    xhr.send(JSON.stringify(requestBody));
});