const authorModule = angular.module("authorModule", []);

authorModule.controller("AuthorController", function AuthorController($http, $scope) {
    $scope.loadWorks = function () {
        $http.get(LISE_REST_URL+"authors/"+getAuthorId()+"/works")
            .then(function (response) {
                $scope.works = response.data.works;
            });
        document.getElementById("work-associations").classList.remove("hidden");
    }
});

function getAuthorId() {
    return document.getElementById("author-id").innerText;
}