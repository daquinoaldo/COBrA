<?php
require_once "functions.php";

if (empty($_POST['username']) || empty($_POST['password']))
    die(newMessage(-1, "Username or password empty."));

$username = htmlentities($_POST['username'], ENT_QUOTES);
$password = htmlentities($_POST['password'], ENT_QUOTES);

if (login($username, $password) != true) die(newMessage(-2, "Username or password wrong."));
else echo newMessage(0, "Logged in as $username.");