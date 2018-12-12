const subjectStatsModule = angular.module("subjectStats", []);
subjectStatsModule.controller("SubjectStatsController", ["$http", "$scope", function ($http, $scope) {
    $scope.subjectInput = document.getElementById("subject");
    $scope.dateBegin = document.getElementById("date-begin");
    $scope.dateEnd = document.getElementById("date-end");
    $scope.results = document.getElementById("results");
    $scope.results.downloadedTotal = document.getElementById("downloaded-total");
    $scope.results.idValue = document.getElementById("id");
    $scope.results.dateBegin = document.getElementById("from");
    $scope.results.dateEnd = document.getElementById("to");
    $scope.getStats = function () {
        if ($scope.results.classList.contains("hidden"))
            $scope.results.classList.remove("hidden");
        let requestUrl = LISE_REST_URL + "subj/stats/" + $scope.subjectInput.value + "?";
        if ($scope.dateBegin.value.length !== 0) {
            requestUrl += "dateBegin=" + $scope.dateBegin.value;
            if ($scope.dateEnd.value !== 0)
                requestUrl += "&dateEnd=" + $scope.dateEnd.value
        } else if ($scope.dateEnd.value.length !== 0) {
            requestUrl += "dateEnd=" + $scope.dateEnd.value;
        }
        $http
            .get(requestUrl)
            .then(function (resp) {
                $scope.results.downloadedTotal.innerText =
                    resp.data.totalDownloads;
                $scope.results.idValue.innerText = resp.data.id;
                $scope.results.dateBegin.innerText = resp.data.dateBegin;
                $scope.results.dateEnd.innerText = resp.data.dateEnd;
            })
            .catch(function (error) {
                alert(error.data.error);
            })
    }
}]);