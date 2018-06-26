function checkLogin() {
    /// Loading
    get("api/get-address", null, function (json) {
        if (json.code < 0) {
            console.error("ERROR: "+json.text);
            window.location.replace("login.html");
        } else {
            document.getElementById("welcome_message").innerHTML =
                document.getElementById("welcome_message").innerHTML.replace("!", " "+json.text+"!");
            loadContentsList();
        }
    });
}
checkLogin();

function loadContentsList() {
  var data = {
    author: "c83407c0b9738578d23b6a5c44e7120628e2c2c6"
  };
  post("api/getAuthorContents", data, function (list) {
    var table = document.getElementById("contents_table");
    for (var i = 0; i < list.length; i++) {
      var row = table.insertRow(table.rows.length);
      row.insertCell(0).innerHTML = list[i].address;
      row.insertCell(1).innerHTML = list[i].genre;
      row.insertCell(2).innerHTML = list[i].price;
      row.insertCell(5).innerHTML = "<a href=\"withdraw.html?id="+list[i].id+"\">withdraw</a>";
    }
    /// stop loading
  });
}

function logMeOut() {
    get("api/logout", null, function (json) {
        if(json.code < 0) {
            console.error("ERROR: "+json.text);
            alert("Can't log out. Sorry for the inconvenience. Try again later.")
        } else {
            window.location.href = "login.html";
        }
    });
    return false;
}