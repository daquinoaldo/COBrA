var DEBUG = true;

function xhr(method, url, data, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, url, true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            if (DEBUG) console.log("Got: "+xhr.responseText);
            var jsonResponse = JSON.parse(xhr.responseText);
            callback(jsonResponse);
        }
    };
    xhr.send(data);
}

function post(url, data, callback) {
    xhr("POST", url, data, callback);
}

function get(url, data, callback) {
    xhr("GET", url, data, callback);
}