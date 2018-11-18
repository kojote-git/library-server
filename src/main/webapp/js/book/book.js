const
    bookModule = angular.module("bookModule",[]);
bookModule.controller("BookController", ["$http", "$scope", function ($http, $scope) {
    const
        publisherId = document.getElementById("publisher-id"),
        workId = document.getElementById("work-id"),
        edition = document.getElementById("edition");
    getPublisher($http, $scope, publisherId.value);
    getWork($http, $scope, workId.value);
    function getBookId() {
        return parseInt(document.getElementById("book-id").innerText)
    }
    publisherId.addEventListener("keydown", function (e) {
        return isNaN(e.key);
    });
    workId.addEventListener("keydown", function (e) {
        return isNaN(e.key);
    });
    publisherId.addEventListener("keyup", function (e) {
        getPublisher($http, $scope, publisherId.value);
    });
    workId.addEventListener("keyup", function (e) {
        getWork($http, $scope, workId.value);
    });
    $scope.submitEditing = function (e) {
        let id = getBookId(),
            publisher = publisherId.value,
            edition = edition.value;
        let requestBody = {
            publisherId: publisher,
            edition: edition
        };
        $http
            .put(LISE_REST_URL + "books/" + id + "/editing", requestBody, {
                headers: getRequestHeaders()
            }).then(r => {
               location.reload();
            });
    };
    $scope.remove = function () {
        let id = getBookId();
        $http
            .delete(LISE_REST_URL + "books/" + id + "/deleting", {
                headers: getRequestHeaders()
            }).then(r => {
                alert(r.data.responseMessage);
                window.open(LISE_ADM_URL + "books", "_self", false);
            });
    };
}]);