function contains(array, predicate) {
    for (let i = 0; i < array.length; i++) {
        if (predicate(array[i]))
            return true;
    }
    return false;
}

function firstIndexOf(array, predicate) {
    for (let i = 0; i < array.length; i++) {
        if (predicate(array[i]))
            return i;
    }
    return -1;
}

function getCookie(name) {
    var matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

function getLogin() {
    return getCookie("login");
}
function getAccessToken() {
    return getCookie("accessToken");
}