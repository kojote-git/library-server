const authorsModule = angular.module("authorsModule", []);

authorsModule.controller("AuthorsController", ["$http", "$scope", function ($http, $scope) {
    const searchBar = document.getElementById("search-by-name");
    $http
        .get(LISE_REST_URL + "authors")
        .then(function (response) {
            $scope.authors = response.data.authors;
        })
}]);