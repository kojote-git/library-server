const publisherModule = angular.module("publisherModule", []);

publisherModule.controller("PublisherController", ["$http", "$scope", function ($http, $scope) {

    $scope.submitEditing = function() {
        let name = document.getElementById("name").value,
            id = getPublisherId(),
            requestBody = {
                name: name
            };
        $http
            .put(LISE_REST_URL + "publishers/" + id + "/editing", requestBody, {
                headers: getHeaders()
            }).then(r => {
                alert(r.data.responseMessage);
            })
    };
    $scope.remove = function() {
        let id = getPublisherId();
        $http
            .delete(LISE_REST_URL + "publishers/" + id + "/deleting", {
                headers: getHeaders()
            }).then(r => {
                alert(r.data.responseMessage);
                window.open(LISE_ADM_URL + "publishers", "_self", false);
            })
    };

    function getPublisherId() {
        return parseInt(document.getElementById("publisher-id").innerText);
    }
    function getHeaders() {
        return {
            "Access-token": getAccessToken(),
            "Login": getLogin()
        }
    }
}]);