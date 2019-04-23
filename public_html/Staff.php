<?php

/**
 * Description of Staff
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class Staff extends DatabaseConnect {

    private $connection;
    private $response = array();
    private $errorStatus = TRUE;

    public function __construct() {
        $this->connection = $this->connect();
    }

    public function __destruct() {
        $this->connection->close();
    }

    public function getResponse() {
        return $this->response;
    }

    public function getStaff($comanyID) {

        $resultSet = $this->connection->query("SELECT userID, profilePicture, firstName, lastName, userType, email, phone, companyID FROM user WHERE companyID = '$comanyID'");

        if ($resultSet->num_rows > 0) {
            while ($row2 = $resultSet->fetch_assoc()) {
//                    public StaffData(String id, String fName, String lName, String job, String email, String phone, String picture) {

                array_push($this->response, array("userID" => $row2["userID"], "profilePicture" => $row2["profilePicture"], "firstName" => $row2["firstName"], "lastName" => $row2["lastName"],
                    "userType" => $row2["userType"], "email" => $row2["email"], "phone" => $row2["phone"], "companyID" => $row2["companyID"]));
            }
            $this->errorStatus = FALSE;
//             $this->response['error'] = TRUE;
//            $this->response['message'] = "Failed to getStaff " . mysqli_error($this->connection);
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
    }

    public function addStaff($job, $fname, $lname, $email, $phone, $password, $companyID, $image) {

        $url = DatabaseConnect::getServerIP() . "/uploads/profilePicture.png";

        if ($image !== '') {
            $uid = uniqid($phone, true);
            $upladPath = './uploads/' . $uid . '.' . 'jpg';
			$upladPath2 = '/uploads/' . $uid . '.' . 'jpg';
            $url = DatabaseConnect::getServerIP() . "$upladPath2";
        }

        $stmt = $this->connection->prepare("INSERT INTO `user` "
                . "(`userID`, `userType`, `firstName`, `lastName`, `email`, `phone`, `password`, `companyID`, `profilePicture`) "
                . "VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param('ssssssis', $job, $fname, $lname, $email, $phone, $password, $companyID, $url);

        if ($stmt->execute()) {
            if ($image !== '') {
                file_put_contents($upladPath, base64_decode($image));
            }
            $this->response['error'] = FALSE;
            $this->response['message'] = "Staff Registration successfull" . mysqli_error($this->connection);
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Staff Registration failed, please try again!" . mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

    public function editStaff($userID, $job, $fname, $lname, $email, $phone, $password, $image) {

        $url = DatabaseConnect::getServerIP() . "/uploads/profilePicture.png";

        if ($image !== '') {
            $uid = uniqid($phone, true);
            $upladPath = './uploads/' . $uid . '.' . 'jpg';
			$upladPath2 = '/uploads/' . $uid . '.' . 'jpg';
            $url = DatabaseConnect::getServerIP() . "$upladPath2";
        }

        if ($this->connection->query("UPDATE user SET userType = '$job', firstName = '$fname', lastName = '$lname', email = '$email', "
                        . "phone = '$phone', password = '$password', profilePicture = '$url' WHERE userID = '$userID';")) {

            if ($image !== '') {
                file_put_contents($upladPath, base64_decode($image));
            }

            $this->response['error'] = FALSE;
            $this->response['message'] = "Staff Profile has successfully been updated";
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to edit staff profile, please try again";
        }
        echo json_encode($this->response);
    }

    public function deleteStaff($userID) {
        if ($this->connection->query("DELETE FROM user WHERE userID = '$userID'")) {

            $this->response['error'] = FALSE;
            $this->response['message'] = "Staff has successfully been deleted";
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to delete staff, please try again. ". mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

}

$newStaff = new Staff;
$response = $newStaff->getResponse();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (isset($_POST['job_add']) and isset($_POST['fname_add']) and isset($_POST['lname_add']) and
            isset($_POST['email_add']) and isset($_POST['phone_add']) and isset($_POST['password_add']) and
            isset($_POST['companyID_add']) and isset($_POST['image_add'])) {

        $newStaff->addStaff($_POST['job_add'], $_POST['fname_add'], $_POST['lname_add'], $_POST['email_add'], $_POST['phone_add'], $_POST['password_add'], $_POST['companyID_add'], $_POST['image_add']);
    }

    if (isset($_POST['userID_edit']) and isset($_POST['job_edit']) and isset($_POST['fname_edit']) and isset($_POST['lname_edit']) and
            isset($_POST['email_edit']) and isset($_POST['phone_edit']) and isset($_POST['password_edit']) and isset($_POST['image_edit'])) {

        $newStaff->editStaff($_POST['userID_edit'], $_POST['job_edit'], $_POST['fname_edit'], $_POST['lname_edit'], $_POST['email_edit'], $_POST['phone_edit'], $_POST['password_edit'], $_POST['image_edit']);
    }

    if (isset($_POST['userID_delete'])) {
        $newStaff->deleteStaff($_POST['userID_delete']);
    }

    if (isset($_POST['companyID_get'])) {
        $newStaff->getStaff($_POST['companyID_get']);
    }
    
} else {
    $response['error'] = TRUE;
    $response['message'] = "Invalid request!";
    echo json_encode($response);
}
unset($newStaff);


