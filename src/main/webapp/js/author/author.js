const authorModule = angular.module("authorModule", []),
    firstName = document.getElementById("first-name"),
    middleName = document.getElementById("middle-name"),
    lastName = document.getElementById("last-name");

authorModule.controller("AuthorController", ["$http", "$scope",
    function ($http, $scope) {
    $scope.worksAreLoaded = false;
    $scope.worksChanges =[];
    $scope.works = [];
    $scope.associationsAreHidden = true;
    $scope.loadWorks = function (synchronous, refresh) {
        if ($scope.worksAreLoaded) {
            $scope.toggleWorks();
            return;
        }
        if (!synchronous) {
            $http
                .get(LISE_REST_URL + "authors/" + getAuthorId() + "/works")
                .then(function (response) {
                    $scope.works = response.data.works;
                    $scope.worksAreLoaded = true;
                });
        } else {
            let xhr = new XMLHttpRequest();
            xhr.open("GET", LISE_REST_URL + "authors/" + getAuthorId() + "/works");
            xhr.addEventListener("load", e => {
                $scope.works = JSON.parse(xhr.response).works;
                $scope.worksAreLoaded = true;
                if (refresh)
                    $scope.$apply();
            })
            xhr.send();
        }
        $scope.toggleWorks();
    };
    $scope.addWork = function () {
        let id = parseInt(prompt("enter id"));
        $scope.loadWorks(true, false);
        if (contains($scope.works, w => w.id === id ))
            return false;
        if (contains($scope.worksChanges, w => w.id === id && w.action === "add"))
            return false;
        $http
            .get(LISE_REST_URL + "works/" + id)
            .then(function (response) {
                $scope.works.push(response.data);
                $scope.worksChanges.push({
                    id: response.data.id,
                    action: "add"
                });
                if ($scope.associationsAreHidden) {
                    $scope.toggleWorks();
                }
            }).catch(function (response) {
                alert(response.data.error);
            });
    };
    $scope.submitEditing = function () {
        let requestBody = {
            firstName: firstName.value,
            middleName: middleName.value,
            lastName: lastName.value
        };
        if ($scope.worksChanges.length !== 0)
            requestBody.works = $scope.worksChanges;
        $http
            .put(LISE_REST_URL+"authors/"+getAuthorId()+"/editing", requestBody, {
                headers: {
                    "Access-token": getAccessToken(),
                    "Login": getLogin()
                }
            })
            .then(function (response) {
                alert(response.data.responseMessage);
                location.reload();
            }).catch(function (response) {
                alert(response.data.error);
            });
    };
    $scope.removeWork = function ($event) {
        let target = $event.currentTarget,
            workId = parseInt(target.getAttribute("data-work-id")),
            i = firstIndexOf($scope.worksChanges, w => w.id === workId);
        if (i === -1) {
            $scope.worksChanges.push({
                id: workId,
                action: "remove"
            });
        } else {
            let ch = $scope.worksChanges[i];
            if (ch.action === "remove") {
                $scope.worksChanges.splice(i, 1);
            } else if (ch.action === "add") {
                let j = firstIndexOf($scope.works, w => w.id === workId);
                $scope.works.splice(j, 1);
                $scope.worksChanges.splice(i, 1);
            }
        }
    };
    $scope.getHidden = function () {
        if (!$scope.worksAreLoaded)
            return "hidden";
        if (!$scope.associationsAreHidden)
            return "";
        return "hidden";
    };
    $scope.getClassForAssociation = function (id) {
        let i = firstIndexOf($scope.worksChanges, function (w) { return w.id === id });
        if (i === -1)
            return "";
        if ($scope.worksChanges[i].action === "add")
            return "for-adding";
        if ($scope.worksChanges[i].action === "remove")
            return "for-removal";
    };
    $scope.toggleWorks = function () {
        let caret = document.getElementById("caret");
        if ($scope.associationsAreHidden) {
            if (window.location.href.startsWith("file"))
                caret.src = "../../res/down-caret.png";
            else
                caret.src = "/lise/res/down-caret.png";
            $scope.associationsAreHidden = false;
            $scope.setHidden = false;
        } else {
            if (window.location.href.startsWith("file"))
                caret.src = "../../res/up-caret.png";
            else
                caret.src = "/lise/res/up-caret.png";
            $scope.associationsAreHidden = true;
            $scope.setHidden = true;
        }
    };
    $scope.remove = function () {
        let id = getAuthorId();
        $http
            .delete(LISE_REST_URL+ "authors/" + id + "/deleting", {
                headers: {
                    "Login": getLogin(),
                    "Access-token": getAccessToken()
                }
            })
            .then(function (response) {
                alert(response.data.responseMessage);
                window.open(LISE_ADM_URL + "authors", "_self", false);
            }).catch(function (response) {
                alert(response.data.error);
            })
    }
}]);
function getAuthorId() {
    return document.getElementById("author-id").innerText;
}


