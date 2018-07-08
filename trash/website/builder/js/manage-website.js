function checkLogin() {
    /// Loading
    get("api/get-username.php", null, function (json) {
        if (json.code < 0) {
            console.error("ERROR: "+json.text);
            window.location.replace("login.html");
        } else retrieveWebsiteInfo();
    });
    /// Stop loading
}
checkLogin();

/* RADIO HANDLERS */
var domainField = document.getElementById("domain");
var apache = document.getElementById("apache");
var nginx = document.getElementById("nginx");
var php = document.getElementById("php");
var mysql = document.getElementById("mysql");

function radioHandler() {
    if(!apache.checked) {		// you cannot have PHP and/or mySQL without Apache
        php.checked = false;
        mysql.checked = false;
    }
}

function mysqlHandler() {
    if (mysql.checked) {		// Apache with PHP required for mySQL
        apache.checked = true;
        php.checked = true;
    }
}

function phpHandler() {
    if (php.checked) apache.checked = true;		// PHP requires Apache
    else mysql.checked = false;		// you cannot have mySQL without PHP
}

var id = null;

/* MANAGE WEBSITE FUNCTIONS */
function retrieveWebsiteInfo() {
    var $_GET = {};
    document.location.search.replace(/\??(?:([^=]+)=([^&]*)&?)/g, function () {
        function decode(s) {
            return decodeURIComponent(s.split("+").join(" "));
        }
        $_GET[decode(arguments[1])] = decode(arguments[2]);
    });

    if ($_GET['id'] != null && $_GET['id'] > 0) {
        id = $_GET['id'];
        get("api/get-website.php?id="+id, null, function (json) {
            if (json.code < 0) {
                console.error("ERROR: " + json.text);
                alert("ERROR: Can't retrieve the website info. ID is not correct or the website is not your.")
            } else {
                document.title = "Edit website | Pier";
                // add id to form
                /*var input = document.createElement("input");
                input.setAttribute("type", "hidden");
                input.setAttribute("name", "site-id");
                input.setAttribute("value", id);
                document.getElementById("form").appendChild(input);*/
                // complete the form
                domainField.value = json.text.domain;
                if (json.text.webserver === "nginx") nginx.checked = true;
                else if (json.text.webserver === "apache") {
                    apache.checked = true;
                    if (json.text.php) {
                        php.checked = true;
                        if (json.text.mysql) mysql.checked = true;
                    }
                }
            }
        });
    }
}

function pushWebsite() {
    // Domain
    var domain = encodeURIComponent(domainField.value);
    if(domain === "") {
        console.error("Domain not defined.");
        alert("Please fill the domain field.");
        return false;
    }
    // Webserver type
    var webserver;
    if (apache.checked) webserver = "apache";
    else if (nginx.checked) webserver = "nginx";
    else {
        console.error("Webserver not defined.");
        alert("Please choose a web server.");
        return false;
    }
    // Send request
    var data = "domain="+domain+"&webserver="+webserver+"&php="+php.checked+"&mysql="+mysql.checked;
    if (id != null) data = data+"&id="+id;
    post("api/manage-website.php", data, onWebsiteCreated);
    //TODO: show spinner
    return false;
}

function onWebsiteCreated(json) {
    if (json.code < 0) {
        console.error("ERROR: "+json.text);
        alert("Cannot create website now. Sorry for the inconvenience.");
        return false;
    } else {
        window.location.href = "index.html";
    }
}