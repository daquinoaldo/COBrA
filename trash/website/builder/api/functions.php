<?php

define("DEBUG", true);
$sites_folder = "/sites";

/* LOGS and MESSAGES */
function newMessage($code, $text) {
    $message = (object) [
        'code' => $code,
        'text' => $text,
    ];
    return json_encode($message);
}

/* FILE MANAGER */
function recursive_copy($src, $dst) {
    $dir = opendir($src);
    if (!$dir) {
        error_log("Can't open $src.");
        return false;
    }
    $old_umask = umask(0);	// Maybe dangerous?
    if (umask() != 0) {
        error_log("Can't set the umask to 0. The umask value is ".umask().".");
        return false;
    }
    mkdir($dst, 0777);
    while($file = readdir($dir) !== false) {
        if($file != '.' && $file != '..') {
            if(is_dir($src.'/'.$file)) recursive_copy ($src.'/'.$file, $dst.'/'.$file);
            else if(copy($src.'/'.$file, $dst.'/'.$file) == false) {
                error_log("Error in copying the default folder.");
                return false;
            }
        }
    }
    umask($old_umask);
    if ($old_umask != umask()) {
        error_log("Error while changing back the umask. The umask value is ".umask().".");
        return false;
    }
    closedir($dir);
    return true;
}

/* BUILDER DATABASE */
function builderDbConnect() {
    $servername = "pier.aldodaquino.com";
    $port = 8000;
    $username = "root";
    $password = "r00t";
    $dbname = "builder";
    $conn = new mysqli($servername, $username, $password, $dbname, $port);
    if ($conn->connect_error) {
        $conn->close();
        error_log("Error in ".__FILE__." at line ".__LINE__.": connect(). servername = \"$servername\", ".
        "port = \"$port\", username = \"$username\", password = \"$password\", dbname = \"$dbname\".");
        die(newMessage(-1, "Connection failed: ".$conn->connect_error));
    }
    return $conn;
}

function exec_bool($sql) {
    $conn = builderDbConnect();
    $result = $conn->query($sql);
    $conn->close();
    return $result;
}

function exec_id($sql) {
    $conn = builderDbConnect();
    if ($conn->query($sql)) $result = $conn->insert_id;
    else $result = -1;
    $conn->close();
    return $result;
}

/* FETCH QUERY RESULT:
$row = mysqli_fetch_array($result)                  // $row is an array with all the column content of a row
$rows = mysqli_fetch_all($result, [MYSQLI_BOTH])    // $rows is an array with all the rows
$rows = mysqli_fetch_all($result, [MYSQLI_ASSOC])
*/


/* USERS, LOGIN and SESSION */

function startSession($username) {
    session_start();
    $_SESSION['username'] = $username;
    session_commit();
}

function signUp($username, $password, $email) {
    // put data in database
    $password = md5($password);
    $sql = "INSERT INTO users (username, password, email) VALUES ('$username', '$password', '$email')";
    if(exec_bool($sql) !== true) return false;
    // create ftp account
    ftpAddUser($username, $password);
    // create mysql account
    if (!mySqlAddUser($username, $password)) return false;
    // logged in
    startSession($username);
    return true;
}

function login($username, $password) {
    // get user data from database
    $result = exec_bool("SELECT password FROM users WHERE username='$username'");
    if(!$result) return false;
    // check password
    $result = mysqli_fetch_array($result)['password'];
    $password = md5($password);
    if($result !== $password) return false;
    // logged in
    startSession($username);
    return true;
}

function getUsername() {
    session_start();
    return $_SESSION['username'];
}

/* BUILDER DATABASE */
function addWebsiteToDatabase($username, $domain, $port, $webserver, $php = 0, $mysql = 0) {
    $sql = "INSERT INTO websites (username, domain, port, webserver, php, mysql)".
        "VALUES ('$username', '$domain', '$port', '$webserver', '$php', '$mysql')";
    return exec_id($sql);
}

function updateWebsiteInDatabase($id, $domain, $webserver, $php = 0, $mysql = 0) {
    $sql = "UPDATE websites SET domain = '$domain', webserver = '$webserver', php = '$php', mysql = '$mysql')".
        "WHERE id = '$id'";
    return exec_id($sql);
}

function getWebsitesList($username) {
    $sql = "SELECT * FROM websites WHERE username='$username'";
    return exec_bool($sql);
}

function getWebsite($id) {
    if ($id < 0) return false;
    return mysqli_fetch_array(exec_bool("SELECT * FROM websites WHERE id='$id'"));
}

/* PORTS */
function getPort() {
    $start_port = 8000;
    $finish_port = 8999;
    $port_to_exclude = array(8000, 8080, 8888);	// builder-mysql, builder and phpmyadmin

    $port = mysqli_fetch_array(exec_bool("SELECT MAX(port) AS port FROM websites"))['port']; // last port used
    if ($port == null) $port = $start_port; // there is no active website
    else $port++;    // last port used + 1 = next port number
    while (in_array($port, $port_to_exclude)) $port++;    // if the port is reserved increment again
    if ($port > $finish_port) {
        for ($i = $start_port; $i <= $finish_port; $i++)    // check if there is a port that is not in use
            if(exec_bool("SELECT COUNT(1) FROM websites WHERE port = '$i'") > 0) {
                $port = $i;
                break;
            }
        if ($port > $finish_port) { // if there is no ports free in absolute
            error_log("All the ports are in use, cannot allocate another port.");
            return null;
        }
    }
    return $port;
}

/* FTP */
function ftpAddUser ($username, $password, $home = null) {
    global $sites_folder;
    if ($home == null) $home = "$sites_folder/$username";
    shell_exec("sudo docker exec ftp bash /add-user.sh $username $password $home");
}

/* MYSQL */
function check($result, $link, $message) {
    if (!$result) {
        error_log("create_sql(): $message");
        mysqli_close($link);
        return false;
    }
    else return true;
}

function mySqlConnect() {
    $root_pw = "r00t";

    $link = mysqli_connect("172.17.0.1:3306", "root", "$root_pw");
    if (!check($link, $link,
        "Error in connecting to MySQL container at 172.17.0.1:3306 with user root and password $root_pw.")) {
        mysqli_close($link);
        return null;
    }
    return $link;
}

function mySqlAddUser ($username, $password) {
    $link = mySqlConnect();
    // Create user
    $sql = "CREATE USER '$username'@'%' IDENTIFIED BY '$password'";
    $result = mysqli_query($link, $sql);
    if (!check($result, $link, "Error in creating user $username.")) return false;
    // Flush privileges
    $sql = "FLUSH PRIVILEGES";
    $result = mysqli_query($link, $sql);
    if (!check($result, $link, "Error in flushing privileges to username $username.")) return false;
    mysqli_close($link);
    return true;
}

function mySqlAddDb ($database, $username) {
    $link = mySqlConnect();
    // Create database
    $sql = "CREATE DATABASE `$database`";
    $result = mysqli_query($link, $sql);
    if (!check($result, $link, "Error in creating database $database.")) return false;
    // Grant privileges
    $sql = "GRANT ALL PRIVILEGES ON `$database`.* TO '$username'@'%'";
    $result = mysqli_query($link, $sql);
    if (!check($result, $link, "Error in granting privileges to username $username.")) return false;
    mysqli_close($link);
    return true;
}

/* DOCKER */
function dockerRun ($name, $domain, $port, $volume, $image, $options = "") {
    if (!empty($volume)) $volume = "-v ".$volume;
    shell_exec("sudo docker run -d --name $name -e VIRTUAL_HOST=$domain -p $port:80 $volume $options $image");
}

function isRunning ($container_name) {
    if ($container_name == null || $container_name == "") return false;
    $result = shell_exec("sudo docker ps | grep $container_name");
    if ($result === null || $result ===  "") return false;
    return true;
}

function stopContainer ($container_name) {
    if ($container_name == null || $container_name == "") return false;
    $result = shell_exec("sudo docker stop $container_name");
    if ($result === null || $result ===  "" || $result !== $container_name) return false;
    return true;
}

function rmContainer ($container_name) {
    if ($container_name == null || $container_name == "") return false;
    $result = shell_exec("sudo docker rm $container_name");
    if ($result === null || $result ===  "" || $result !== $container_name) return false;
    return true;
}

function builderRun($id) {
    global $sites_folder;

    $website = getWebsite($id);
    if ($website == null) return false;

    $website_name = "site".$website['id'];
    $username = $website['username'];
    $webserver = $website['webserver'];
    $domain = $website['domain'];
    $port = $website['port'];
    if ($website_name == null | $username == null | $webserver == null | $domain == null | $port == null) return false;

    // is an update?
    $isRunning = isRunning($website_name);

    $volume_path = "$sites_folder/$username/$website_name";
    if(!$isRunning) //recursive_copy("$sites_folder/test_html/", "$volume_path/");    // can return false
        shell_exec("sudo cp $sites_folder/test_html/index.html $volume_path/index.html");

    $php = $website['id'] > 0;

    if (!$isRunning) {
        $mysql = $website['mysql'] > 0;
        if ($mysql)
            if (!mySqlAddDb($website_name, $username)) return false;
    }

    switch ($webserver) {
        case "apache":
            if($php) $image = "php:apache";
            else $image = "httpd";
            $volume = "/var/www/html/";
            break;
        case "nginx":
            $image = "nginx";
            $volume = "/usr/share/nginx/html/";
            break;
        default:
            error_log("Unknown web server $webserver");
            return false;
    }

    $volume = "$volume_path:$volume";

    if ($isRunning) {
        stopContainer($website_name);
        rmContainer($website_name);
    }

    dockerRun($website_name, $domain, $port, $volume, $image);

    return true;
}