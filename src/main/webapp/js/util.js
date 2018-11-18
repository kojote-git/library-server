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

function getRequestHeaders() {
    return {
        "Access-token": getAccessToken(),
        "Login": getLogin()
    };
}

function binarySearch(array, value, comparator) {
    var guess,
        min = 0,
        max = array.length - 1;

    while(min <= max) {
        guess = Math.floor((min + max) /2);
        if(comparator(array[guess], value) === 0)
            return guess;
        else if(comparator(array[guess], value) < 0)
            min = guess + 1;
        else
            max = guess - 1;
    }

    return -1;
};