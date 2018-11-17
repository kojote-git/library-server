const authorsModule = angular.module("authorsModule", []);

authorsModule.controller("AuthorsController", ["$http", "$scope", function ($http, $scope) {
    const searchBar = document.getElementById("search-by-name");
    $http
        .get(LISE_REST_URL + "authors")
        .then(function (response) {
            $scope.authors = response.data.authors;
        });
    searchBar.addEventListener("keydown", function (e) {
        if (e.keyCode === 13) {
            let url = LISE_REST_URL + "authors?";
            if (searchBar.value !== "") {
                let name = searchBar.value.split(" ");
                if (name.length === 1)
                    url += "fn=" + name[0];
                else if (name.length === 2)
                    url += "fn=" + name[0] + "&ln=" + name[1];
                else if (name.length >= 3)
                    url += "fn=" + name[0] + "&md=" + name[1] + "&ln=" + name[2];
            }
            $http
                .get(url)
                .then( (r) => {
                    $scope.authors = r.data.authors;
                });
        }
    })
}]);