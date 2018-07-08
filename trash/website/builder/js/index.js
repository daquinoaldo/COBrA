function checkLogin() {
    /// Loading
    get("api/get-username.php", null, function (json) {
        if (json.code < 0) {
            console.error("ERROR: "+json.text);
            window.location.replace("login.html");
        } else {
            document.getElementById("welcome_message").innerHTML =
                document.getElementById("welcome_message").innerHTML.replace("!", " "+json.text+"!");
            loadWebsitesList();
        }
    });
}
checkLogin();

function loadWebsitesList() {
    get("api/get-user-websites.php", null, function (json) {
        if (json.code < 0) {    // Not logged in: really unlikely, I just checked
            console.error("ERROR: "+json.text);
            window.location.replace("login.html");
        } else {
            var websites = json.text;
            var table = document.getElementById("websites_table");
            for (var i = 0; i < websites.length; i++) {
                var row = table.insertRow(table.rows.length);
                row.insertCell(0).innerHTML = websites[i].id;
                row.insertCell(1).innerHTML = "<a href=\"http://"+websites[i].domain+"\">"+websites[i].domain+"</a>";
                row.insertCell(2).innerHTML = websites[i].webserver;
                row.insertCell(3).innerHTML = websites[i].php == 1 ? "yes" : "no";
                row.insertCell(4).innerHTML = websites[i].php == 1 ? "yes" : "no";
                row.insertCell(5).innerHTML = "<a href=\"manage-website.html?id="+websites[i].id+"\">manage</a>";
            }
            /// stop loading
        }
    });
}

function logMeOut() {
    get("api/logout.php", null, function (json) {
        if(json.code < 0) {
            console.error("ERROR: "+json.text);
            alert("Can't log out. Sorry for the inconvenience. Try again later.")
        } else {
            window.location.href = "login.html";
        }
    });
    return false;
}