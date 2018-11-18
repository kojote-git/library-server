const bookInstanceModule = angular.module("bookInstanceModule", []);

bookInstanceModule.controller("BookInstanceController", ["$http", "$scope", function ($http, $scope) {
    const
        coverInputUpload = document.getElementById("cover-input-upload"),
        coverInput = document.getElementById("cover-input"),
        fileInputUpload = document.getElementById("file-input-upload"),
        fileInput = document.getElementById("file-input"),
        format = document.getElementById("format"),
        image = document.getElementById("cover");
    $http
        .get(LISE_REST_URL + "instances/" + getBookInstanceId())
        .then(function (resp) {
            $scope.bookInstance = resp.data;
        });
    $http
        .get(LISE_REST_URL + "books/" + getBookId())
        .then(function (resp) {
            $scope.book = resp.data;
            $http
                .get(LISE_REST_URL + "works/" + $scope.book.workId)
                .then(function (resp) {
                    $scope.book.work = resp.data;
                })
        });
    fileInputUpload.addEventListener("click", (e) => fileInput.click());
    coverInputUpload.addEventListener("click", (e) => coverInput.click());
    coverInput.addEventListener("change", function (e) {
        $scope.coverChanged = true;
        let file = coverInput.files[0];
        let reader = new FileReader();
        reader.onload = function (e) {
            image.setAttribute("src", e.target.result);
        };
        reader.readAsDataURL(file);
    });
    fileInput.addEventListener("change", function (e) {
        $scope.fileChanged = true;
    });
    $scope.submitEditing = function () {
        let requestBody = {
            format: format.value
        };
        $http
            .put(LISE_REST_URL + "instances/" + getBookInstanceId() + "/editing", requestBody, {
                headers: getRequestHeaders()
            });
        tryUploadBookFile();
        tryUploadCover();
    };
    $scope.remove = function () {
        let id = getBookInstanceId();
        $http
            .delete(LISE_REST_URL + "rest/instances/" + id + "/deleting", {
                headers : getRequestHeaders()
            })
            .then(function (resp) {
                alert(resp.data.responseMessage);
                window.open($scope.book._links.adm, "_self", false);
            });
    };
    function tryUploadBookFile() {
        if (!$scope.fileChanged)
            return;
        fileInput.disabled = true;
        let
            file = fileInput.files[0],
            name = "file",
            url = LISE_REST_URL + "instances/" + getBookInstanceId() + "/file",
            callback = function (e) {
                fileInput.disabled = false;
                $scope.fileChanged = false;
                alert("File's been uploaded!");
            };
        tryUploadFile(file, name, url, true, callback);
    }
    function tryUploadCover() {
        if (!$scope.coverChanged)
            return;
        coverInput.disabled = true;
        let file = coverInput.files[0],
            name = "cover",
            url = LISE_REST_URL + "instances/" + getBookInstanceId() + "/cover",
            callback = function (e) {
                coverInput.disabled = false;
                $scope.coverChanged = false;
                alert("Image's been uploaded!");
            };
        tryUploadFile(file, name, url, true, callback);
    }
    function tryUploadFile(file, name, url, async, callback) {
        let xhr = new XMLHttpRequest();
        xhr.open("PUT", url, async);
        let formData = new FormData();
        formData.append(name, file);
        xhr.setRequestHeader("Access-token", getAccessToken());
        xhr.setRequestHeader("Login", getLogin());
        if (callback)
            xhr.addEventListener("load", callback);
        xhr.send(formData);
    }
    function getBookInstanceId() {
        return parseInt(document.getElementById("book-instance-id").innerText)
    }
    function getBookId() {
        return parseInt(document.getElementById("book-id").innerText);
    }
}]);