const worksModule = angular.module("worksModule", []);

worksModule.controller("WorksController", ["$http", "$scope", function ($http, $scope) {
    const searchTitle = document.getElementById("search-title");
    $scope.works = [];
    $http
        .get(LISE_REST_URL + "works")
        .then(r => {
            $scope.works = r.data.works;
        });
    searchTitle.addEventListener("keydown", function (e) {
        if (e.keyCode !== 13)
            return;
        let url = LISE_REST_URL + "works?",
            title = searchTitle.value;
        if (title.length !== 0) {
            url += "title=" + title;
        }
        $http.get(url)
            .then(r => {
                $scope.works = r.data.works;
            })
    })
}]);