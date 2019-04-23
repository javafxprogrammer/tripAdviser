<?php

/**
 * Description of LogIn
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class LogIn extends DatabaseConnect {

    private $connection;
    private $response = array();

    function __construct() {
        $this->connection = $this->connect();
    }

    private function logIn($email, $password) {
        $stmt = $this->connection->prepare('SELECT userID FROM user where email = ? AND password = ?');
        $stmt->bind_param('ss', $email, $password);
        $stmt->execute();
        $stmt->store_result();
        return $stmt->num_rows > 0;
    }

    private function getUserDetails($email, $password) {    
        $stmt = $this->connection->prepare('SELECT * FROM user where email = ? AND password = ?');
        $stmt->bind_param('ss', $email, $password);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $this->response['error'] = FALSE;
        $this->response['userID'] = $result['userID'];
        $this->response['userType'] = $result['userType'];
        $this->response['userName'] = $result['firstName'].' '.$result['lastName'];
        $this->response['email'] = $result['email'];
        $this->response['phone'] = $result['phone'];
        $this->response['companyID'] = $result['companyID'];
        $this->response['profilePicture'] = $result['profilePicture'];
//        echo json_encode($this->response); //remove this duplicate
    }

    public function logInProcessing() {
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            if (isset($_POST['email']) and isset($_POST['password'])) {
                $logInStatus = $this->logIn($_POST['email'], $_POST['password']);
                if ($logInStatus === TRUE) {
                    $this->getUserDetails($_POST['email'], $_POST['password']);
//                    $this->response['error'] = FALSE;
//                    $this->response['message'] = 'LogIn successfull';
                } else {
                    $this->response['error'] = TRUE;
                    $this->response['message'] = 'Email or password is incorrect!';
                }
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = 'Please enter both fields!';
            }
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = 'Invalid request!';
        }
        echo json_encode($this->response);
    }

    public function __destruct() {
        $this->connection->close();
    }

}

$newLogIn = new LogIn;
$newLogIn->logInProcessing();
