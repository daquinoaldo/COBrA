// DEBUG LEVEL: 0 = off, 1 = error, 2 = warning, 3 = info, 4 = debug, 5 = verbose
var DEBUG = 1;

function xhr(method, url, data, onResult, onError) {
  var xhr = new XMLHttpRequest();
  xhr.open(method, url, true);
  xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xhr.onreadystatechange = function () {
    if (xhr.readyState === 4 && xhr.status === 200) {
      var result = JSON.parse(xhr.responseText);
      if (DEBUG >= 5) console.log("Got: " + result);
      onResult(result);
    } else {
      var error = JSON.parse(xhr.responseText);
      if (DEBUG) console.log("ERROR "+xhr.status+": "+error);
      onError(xhr.status, error);
    }
  };
  xhr.send(JSON.stringify(data));
}

function post(url, data, onResult, onError) {
  xhr("POST", url, data, onResult, onError);
}

function get(url, data, onResult, onError) {
  xhr("GET", url, data, onResult, onError);
}