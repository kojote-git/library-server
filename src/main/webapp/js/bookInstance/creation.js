const
    bookId = document.getElementById("book-id").value,
    isbn13 = document.getElementById("isbn13"),
    format = document.getElementById("format"),
    submit = document.getElementById("submit");
submit.addEventListener("click", function (e) {
    let requestBody = {
        bookId: bookId,
        isbn13: isbn13.value,
        format: format.value
    }, xhr = new XMLHttpRequest();
    xhr.open("POST", LISE_REST_URL + "instances/creation", true);
    xhr.setRequestHeader("Access-token", getAccessToken());
    xhr.setRequestHeader("Login", getLogin());
    xhr.addEventListener("load", function (e) {
        if (xhr.status !== 201) {
            alert("bad request");
        } else {
            let resp = JSON.parse(xhr.response);
            window.open(LISE_ADM_URL + "instances/" + resp.id, "_self", false);
        }
    });
    xhr.send(JSON.stringify(requestBody));
});