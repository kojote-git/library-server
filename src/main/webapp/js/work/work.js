const workModule = angular.module("workModule", []);

workModule.controller("WorkController", ["$http", "$scope", function ($http, $scope) {
    const subjectInput = document.getElementById("subject");
    $http
        .get(LISE_REST_URL + "works/" + getWorkId() + "/subjects")
        .then(function (response) {
            $scope.subjects = response.data.subjects;
        });
    $scope.authorsAreLoaded = false;
    $scope.authorsAreHidden = true;
    $scope.authorsChanges = [];
    $scope.subjectsChanges = [];
    $scope.getSrcForRemoveIcon = function () {
        if (window.location.href.startsWith("file"))
            return "../../res/remove.png";
        else
            return "/lise/res/remove.png";
    };
    $scope.loadAuthors = function (synchronous) {
        if ($scope.authorsAreLoaded) {
            toggleAuthors();
            return;
        }
        if (!synchronous) {
            $http
                .get(LISE_REST_URL + "works/" + getWorkId() + "/authors")
                .then(r => {
                    $scope.authors = r.data.authors;
                    $scope.authorsAreLoaded = true;
                });
        } else {
            let xhr = new XMLHttpRequest();
            xhr.open("GET", LISE_REST_URL + "works/" + getWorkId() + "/authors", false);
            xhr.addEventListener("load", e => {
                $scope.authors = JSON.parse(xhr.response).authors;
                $scope.authorsAreLoaded = true;
            });
            xhr.send();
        }
        toggleAuthors();
    };
    $scope.addAuthor = function() {
        let id = parseInt(prompt("enter id"));
        $scope.loadAuthors(true);
        if ($scope.authorsAreHidden)
            toggleAuthors();
        if (contains($scope.authors, a => a.id === id))
            return false;
        if (contains($scope.authorsChanges, a => a.id === id))
            return false;
        $http
            .get(LISE_REST_URL + "authors/" + id)
            .then(r => {
                $scope.authors.push(r.data);
                $scope.authorsChanges.push({
                    id: r.data.id,
                    action: "add"
                });
            }).catch(e => {
                alert(e.data.error);
            });
    };
    $scope.removeAuthor = function($event) {
        let target = $event.currentTarget,
            authorId = parseInt(target.getAttribute("data-author-id")),
            i = firstIndexOf($scope.authorsChanges, a => a.id === authorId);
        if (i === -1) {
            $scope.authorsChanges.push({
                id: authorId,
                action: "remove"
            });
        } else {
            if ($scope.authorsChanges[i].action === "remove") {
                $scope.authorsChanges.splice(i, 1);
            } else {
                let j = firstIndexOf($scope.authors, a => a.id === authorId);
                $scope.authors.splice(j, 1);
                $scope.authorsChanges.splice(i, 1);
            }
        }
    };
    $scope.removeSubject = function($event) {
        let target = $event.currentTarget,
            subject = target.parentNode.parentNode.children[0].innerText;
        let i = firstIndexOf($scope.subjectsChanges, s => s.subject === subject && s.action === "add");
        let j = firstIndexOf($scope.subjects, s => s === subject);
        if (i !== -1) {
            $scope.subjectsChanges.splice(i, 1);
            $scope.subjects.splice(j, 1);
        } else {
            $scope.subjects.splice(j, 1);
            $scope.subjectsChanges.push({
                subject: subject,
                action: "remove"
            });
        }
        console.log($scope.subjectsChanges);
    };
    subjectInput.addEventListener("keydown", function(e) {
        if (e.keyCode !== 13)
            return;
        let subject = subjectInput.value;
        subjectInput.value = "";
        let i = firstIndexOf($scope.subjectsChanges, s => s.subject === subject && s.action === "remove");
        if (i !== -1) {
            $scope.subjectsChanges.splice(i, 1);
            $scope.subjects.push(subject);
        } else {
            if (contains($scope.subjects, s => s === subject)) {
                console.log($scope.subjectsChanges);
                return;
            }
            $scope.subjects.push(subject);
            $scope.subjectsChanges.push({
                subject: subject,
                action: "add"
            });
        }
        console.log($scope.subjectsChanges);
        $scope.$apply();
    });
    $scope.getClassForAssociation = function(id) {
        let idx = firstIndexOf($scope.authorsChanges, a => a.id === id);
        if (idx === -1)
            return "";
        if ($scope.authorsChanges[idx].action === "remove")
            return "for-removal";
        else
            return "for-adding";
    };
    $scope.getHidden = function() {
        if ($scope.authorsAreHidden)
            return "hidden";
        return "";
    };
    $scope.submitEditing = function () {
        let requestBody = {
            title: document.getElementById("title").value,
            description: document.getElementById("description").value,
            lang: document.getElementById("lang").value
        };
        subjectInput.disabled = true;
        if ($scope.authorsChanges.length !== 0)
            requestBody.authors = $scope.authorsChanges;
        if ($scope.subjectsChanges.length !== 0)
            requestBody.subjects = $scope.subjectsChanges;
        $http
            .put(LISE_REST_URL + "works/" + getWorkId() + "/editing", requestBody, {
                headers: {
                    "Login": getLogin(),
                    "Access-token": getAccessToken()
                }
            }).then(r => {
                alert(r.data.responseMessage);
                window.location.reload();
            }).catch(e => {
                alert(e.data.error);
            })
    };
    $scope.remove = function () {
        $http
            .delete(LISE_REST_URL + "works/" + getWorkId() + "/deleting", {
                headers: {
                    "Access-token": getAccessToken(),
                    "Login": getLogin()
                }
            }).then(r => {
                alert(r.data.responseMessage);
                window.open(LISE_ADM_URL + "works", "_self", false);
        });
    };
    function toggleAuthors() {
        if ($scope.authorsAreHidden) {
            $scope.authorsAreHidden = false;
            document.getElementById("author-associations").classList.remove("hidden");
            let caret = document.getElementById("caret");
            if (window.location.href.startsWith("file"))
                caret.src = "../../res/down-caret.png";
            else
                caret.src = "/lise/res/down-caret.png";
        } else {
            $scope.authorsAreHidden = true;
            document.getElementById("author-associations").classList.add("hidden");
            let caret = document.getElementById("caret");
            if (window.location.href.startsWith("file"))
                caret.src = "../../res/up-caret.png";
            else
                caret.src = "/lise/res/up-caret.png";
        }
    }
    function getWorkId() {
        return parseInt(document.getElementById("work-id").innerText);
    }
}]);

