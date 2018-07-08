<?php
require_once "functions.php";

if (empty($_POST['username']) || empty($_POST['email']) || empty($_POST['password']))
    die(newMessage(-1, "Username, email or password empty."));

$username = htmlentities($_POST['username'], ENT_QUOTES);
$email = htmlentities($_POST['email'], ENT_QUOTES);
$password = htmlentities($_POST['password'], ENT_QUOTES);

if (signUp($username, $password, $email) != true) die(newMessage(-2, "Username or email already exists."));
else echo newMessage(0, "Successfully signed up as $username.");