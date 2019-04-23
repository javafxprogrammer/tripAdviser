<?php

/**
 * Description of SignUp
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class SignUp extends DatabaseConnect {

    private $connection;
    private $response = array();

    function __construct() {
        $this->connection = $this->connect();
    }

    public function editUser($userID, $firstName, $lastName, $email, $phone, $password, $picture) {
        $image = '';
        $url = DatabaseConnect::getServerIP() . "/uploads/profilePicture.png";

        if (!empty($picture)) {
            $image = $picture;
            $uid = uniqid($phone, true);
            $upladPath = './uploads/' . $uid . '.' . 'jpg';
			$upladPath2 = '/uploads/' . $uid . '.' . 'jpg';
            $url = DatabaseConnect::getServerIP() . "$upladPath2";
        }

        if ($this->connection->query("UPDATE user SET firstName = '$firstName', lastName = '$lastName', email = '$email', "
                        . "phone = '$phone', password = '$password', profilePicture = '$url' WHERE userID = '$userID';")) {

            if ($image === '') {
                
            } else {
                file_put_contents($upladPath, base64_decode($image));
            }

            $this->response['error'] = FALSE;
            $this->response['message'] = "Profile has successfully been updated";
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to edit profile, please try again";
        }
        echo json_encode($this->response);
    }

    public function addUser($firstName, $lastName, $email, $phone, $password, $profilePicture) {
        $stmt = $this->connection->prepare("INSERT INTO `user` (`userID`, `userType`, `firstName`, `lastName`, `email`, `phone`, `password`, `companyID`, `profilePicture`) "
                . "VALUES (NULL, 'customer', ?, ?, ?, ?, ?, NULL, ?)");
        $stmt->bind_param('ssssss', $firstName, $lastName, $email, $phone, $password, $profilePicture);
        if ($stmt->execute()) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public function addUserProcessing() {
        $picture = $_POST['profilePicture'];
        $image = '';
        $url = DatabaseConnect::getServerIP() . "/uploads/profilePicture.png";
		$upladPath = '';

        if ($picture !== '') {
            $image = $picture;
            $phone = $_POST['phone'];
            $uid = uniqid($phone, true);
            $upladPath = './uploads/' . $uid . '.' . 'jpg';
			$upladPath2 = '/uploads/' . $uid . '.' . 'jpg';
            $url = DatabaseConnect::getServerIP() . "$upladPath2";
        }

        $registerStatus = $this->addUser($_POST['firstName'], $_POST['lastName'], $_POST['email'], $_POST['phone'], $_POST['password'], $url);
        if ($registerStatus === TRUE) {

            if ($image !== '') {
                file_put_contents($upladPath, base64_decode($image));
            }

            $this->response['error'] = FALSE;
            $this->response['message'] = "Registration successfull" . mysqli_error($this->connection);
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Registration failed, please try again!" . mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

    public function getResponse() {
        return $this->response;
    }

    public function __destruct() {
        $this->connection->close();
    }

}

$newSignUp = new SignUp;
$response = $newSignUp->getResponse();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    if (isset($_POST['userID_edit']) and isset($_POST['firstName_edit']) and isset($_POST['lastName_edit']) and
            isset($_POST['email_edit']) and isset($_POST['phone_edit']) and isset($_POST['password_edit'])) {
        $userID = $_POST['userID_edit'];
        $firstName = $_POST['firstName_edit'];
        $lastName = $_POST['lastName_edit'];
        $email = $_POST['email_edit'];
        $phone = $_POST['phone_edit'];
        $password = $_POST['password_edit'];
        $picture = $_POST['profilePicture_edit'];

        $newSignUp->editUser($userID, $firstName, $lastName, $email, $phone, $password, $picture);
    }

    if (isset($_POST['firstName']) and isset($_POST['lastName']) and isset($_POST['email']) and
            isset($_POST['phone']) and isset($_POST['password']) and isset($_POST['profilePicture'])) {
        $newSignUp->addUserProcessing();
    }
} else {
    $response['error'] = TRUE;
    $response['message'] = "Invalid request!";
    echo json_encode($response);
}
unset($newSignUp);
