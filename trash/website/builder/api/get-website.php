<?php
require_once "functions.php";

$username = getUsername();
if(empty($username)) die(newMessage(-1, "Not logged in."));

$id = htmlentities($_GET['id']);
if (empty($id) || $id == null || $id === "") {
    $id = htmlentities($_POST['id']);
    if (empty($id) || $id == null || $id === "") die(newMessage(-2, "Missing id."));
}

$result = getWebsite($id);

echo newMessage(0, $result);