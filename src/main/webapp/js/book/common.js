function getPublisher($http, $scope, id) {
    if (id.length === 0)
        return;
    $http
        .get(LISE_REST_URL + "publishers/" + id)
        .then(r => {
            $scope.publisher = r.data
        }).catch(e => {
            $scope.publisher = {
                name: "NOT FOUND!"
            }
        });
}
function getWork($http, $scope, id) {
    if (id.length === 0)
        return;
    $http
        .get(LISE_REST_URL + "works/" + id)
        .then(r => {
            $scope.work = r.data
        }).catch(e => {
            $scope.work = {
                title: "NOT FOUND!"
            }
        });
}