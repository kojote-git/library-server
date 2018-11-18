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
    $scope.associationsAreHidden = true;
    $scope.submitEditing = function (e) {
        let id = getBookId(),
            publisher = publisherId.value,
            editionValue = edition.value;
        let requestBody = {
            publisherId: publisher,
            edition: editionValue
        };
        $http
            .put(LISE_REST_URL + "books/" + id + "/editing", requestBody, {
                headers: getRequestHeaders()
            }).then(r => {
               location.reload();
            });
    };
    $scope.loadInstances = function (e) {
        if (!$scope.worksAreLoaded) {
            toggleInstances();
        }
        $http
            .get(LISE_REST_URL + "books/" + getBookId() + "/instances")
            .then(function (resp) {
                $scope.instances = resp.data.instances;
                $scope.worksAreLoaded = true;
            });
        toggleInstances();
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
    $scope.getHidden = function () {
        if ($scope.associationsAreHidden)
            return "hidden";
        else
            return "";
    };
    function toggleInstances() {
        if ($scope.associationsAreHidden) {
            $scope.associationsAreHidden = false;
            if (window.location.href.startsWith("file"))
                document.getElementById("caret").src = "../../res/down-caret.png";
            else
                document.getElementById("caret").src = "/lise/res/down-caret.png";
        } else {
            $scope.associationsAreHidden = true;
            if (window.location.href.startsWith("file"))
                document.getElementById("caret").src = "../../res/up-caret.png";
            else
                document.getElementById("caret").src = "/lise/res/up-caret.png";
        }
    }
}]);