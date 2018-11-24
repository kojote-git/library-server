const queryModule = angular.module("query", []);
queryModule.controller("QueryController", ["$http", "$scope", function ($http, $scope) {
    const
        run = document.getElementById("run"),
        sqlInput = document.getElementById("sql-input"),
        clear = document.getElementById("clear"),
        errors = document.getElementById("errors");
    clear.addEventListener("click", function (e) {
        sqlInput.value = "";
        errors.innerText = "";
        $scope.result = [];
        $scope.$apply();
    });
    run.addEventListener("click", function (e) {
        let request = {
            query: sqlInput.value
        };
        $http
            .post(LISE_ADM_URL + "query", request, {
                headers: {
                    "Login": getLogin(),
                    "Access-token": getAccessToken()
                }
            })
            .then(function (resp) {
                $scope.result = resp.data;
                errors.innerText = "";
            }).catch(function (r) {
                errors.innerText = r.data.error;
            });
    });
    sqlInput.addEventListener("keypress", function (e) {
        if (e.ctrlKey && e.key === "Enter")
            run.click();
    });
}]);