const booksModule = angular.module("booksModule", []);
booksModule.controller("BooksController", ["$http", "$scope", function ($http, $scope) {
    $http
        .get(LISE_REST_URL + "publishers")
        .then(function (resp) {
            $scope.publishers = resp.data.publishers;
            $scope.publishers.sort((a, b) => a.id - b.id);
        });
    $http
        .get(LISE_REST_URL + "works")
        .then(function (resp) {
            $scope.works = resp.data.works;
            $scope.works.sort((a, b) => a.id - b.id);
        });
    $http
        .get(LISE_REST_URL + "books")
        .then(function (resp) {
            $scope.books = resp.data;
        });
    $scope.getWork = function(id) {
        let idx = binarySearch($scope.works, id, (v, it) => v.id - it);
        return $scope.works[idx];
    };
    $scope.getPublisher = function (id) {
        let idx = binarySearch($scope.publishers, id, (v, it) => v.id - it);
        return $scope.publishers[idx];
    };
}]);