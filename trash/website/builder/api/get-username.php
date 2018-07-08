<?php
require_once "functions.php";

$username = getUsername();
if(empty($username)) die(newMessage(-1, "Not logged in."));

echo newMessage(0, $username);