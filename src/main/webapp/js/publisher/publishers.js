const publishersModule = angular.module("publishersModule", []);

publishersModule.controller("PublishersController", ["$http", "$scope", function ($http, $scope) {
    $http
        .get(LISE_REST_URL + "publishers")
        .then(r => {
            $scope.publishers = r.data.publishers
        });
}])