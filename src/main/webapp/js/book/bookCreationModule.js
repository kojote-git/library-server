const bookCreationModule = angular.module("bookCreationModule", []);

bookCreationModule.controller("BookCreationController", ["$http", "$scope", function ($http, $scope) {
    const
        publisherId = document.getElementById("publisher-id"),
        workId = document.getElementById("work-id"),
        edition = document.getElementById("edition");
    $scope.submitCreation = function() {

    };
    publisherId.addEventListener("keydown", function (e) {
        return isNaN(e.key);
    });
    workId.addEventListener("keydown", function (e) {
        return isNaN(e.key);
    });
    publisherId.addEventListener("keyup", function (e) {
        getPublisher(publisherId.value);
    });
    workId.addEventListener("keyup", function (e) {
        getWork(workId.value);
    });
    $scope.submitCreation = function () {
        let requestBody = {
            publisherId: publisherId.value,
            workId: workId.value,
            edition: edition.value
        };
        $http
            .post(LISE_REST_URL + "books/creation", requestBody, {
                headers: getRequestHeaders()
            }).then(r => {
                window.open(LISE_ADM_URL + "books/" + r.data.id, "_self", false);
            });
    };
    function getPublisher(id) {
        if (id.length === 0)
            return;
        $http
            .get(LISE_REST_URL + "publishers/" + id)
            .then(r => {
                $scope.publisher = r.data
            }).catch(e => {
                $scope.publisher = {
                    name: "NOT FOUND!"
                }
            });
    }
    function getWork(id) {
        if (id.length === 0)
            return;
        $http
            .get(LISE_REST_URL + "works/" + id)
            .then(r => {
                $scope.work = r.data
            }).catch(e => {
                $scope.work = {
                    title: "NOT FOUND!"
                }
            });
    }
}]);