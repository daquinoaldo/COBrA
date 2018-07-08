function logMeIn() {
    var username = document.getElementById("LUsername").value;
    var password = document.getElementById("LPassword").value;
    post("api/login.php", "username="+username+"&password="+password, onLoggedIn);
    return false;
}

function onLoggedIn(json){
    if(json.code < 0) {
        console.error("ERROR: "+json.text);
        alert("Can't login: "+json.text);   //TODO: Shake form
    } else {
        window.location.href = "index.html";
    }
}

function signMeUp() {
    var username = document.getElementById("RUsername").value;
    var email = document.getElementById("REmail").value;
    var password = document.getElementById("RPassword").value;
    var password2 = document.getElementById("RPassword2").value;
    if (password !== password2) {
        console.error("ERROR: Password mismatching.");
        alert("Can't sign up: password mismatching.");  //TODO: Shake form
    }
    else post("api/sign-up.php", "username="+username+"&email="+email+"&password="+password, onSignedUp);
    return false;
}

function onSignedUp(json) {
    if (json.code < 0) {
        console.error("ERROR: "+json.text);
        alert("Can't sign up: "+json.text); //TODO: Shake form
    } else {
        alert("Signed up successfully!");
        window.location.href = "index.html";
    }
}