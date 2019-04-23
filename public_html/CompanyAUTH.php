<?php

/**
 * Description of CompanyAUTH
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class CompanyAUTH extends DatabaseConnect {

    //put your code here
    private $connection;
    private $responce = array();

    public function __construct() {
        $this->connection = $this->connect();
    }

    public function generateUniqueIDs() {
        for ($i = 0; $i < 110; $i++) {
            $uniqid = uniqid('', true);
            $this->connection->query("INSERT INTO uniqueIdentifier(id, uniqueID) VALUES(NULL, '$uniqid');");
        }
    }

    public function getUniqueID($password) {
        $result = $this->connection->query("SELECT uniqueID FROM uniqueIdentifier WHERE uniqueID = '$password'");

        if ($result->num_rows == 1) {

            $row = $result->fetch_assoc();
            $uid = $row["uniqueID"];

            $this->response['error'] = FALSE;
            $result2 = $this->connection->query("DELETE FROM uniqueIdentifier WHERE uniqueID = '$uid'");
        } else {

            $this->response['error'] = TRUE;
            $this->response['message'] = 'Password is incorrect!';
        }
    }

    public function uniqueIDProcessing() {
        if ($_SERVER['REQUEST_METHOD'] == 'POST') {
            if (isset($_POST['password'])) {
                $this->getUniqueID($_POST['password']);
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = 'Please enter password';
            }
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = 'Invalid request!';
        }
        echo json_encode($this->response);
    }

    public function __destruct() {
        
    }

}

$newCompanyAUTH = new CompanyAUTH;
$newCompanyAUTH->uniqueIDProcessing();
//$newCompanyAUTH->generateUniqueIDs();
unset($newCompanyAUTH);
