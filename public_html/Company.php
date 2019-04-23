<?php

/**
 * Description of Company
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class Company extends DatabaseConnect {

    private $connection;
    private $responce = array();
    private $errorStatus = TRUE;

    public function __construct() {
        $this->connection = $this->connect();
    }

    public function addCompany() {
        $userID = $_POST['userID'];
        $name = mysqli_real_escape_string($this->connection, $_POST['name']);
        $description = mysqli_real_escape_string($this->connection, $_POST['description']);
        $phone = $_POST['phone'];
        $email = $_POST['email'];
        $website = $_POST['website'];

        $image = ''; //insert default image if provided, put image in dir
        $url = DatabaseConnect::getServerIP() . "/uploads/bus.jpg";

        if (!empty($_POST['image'])) {
            $image = $_POST['image'];
			$uid = uniqid($phone, true);
            $upladPath = './uploads/' . $uid . '.' . 'jpg';
			$upladPath2 = '/uploads/' . $uid . '.' . 'jpg';
            $url = DatabaseConnect::getServerIP() . "$upladPath2";
        }
        $stmt = $this->connection->query("INSERT INTO `company` (`companyID`, `image`, `name`, `details`, `phone`, `email`, `website`) VALUES (NULL, '$url', '$name', '$description', $phone, '$email', '$website')");

        if ($stmt) {

            $companiID;
            $result = $this->connection->query("SELECT companyID FROM company WHERE email = '$email'");
            if ($result->num_rows === 1) {
                $row = $result->fetch_assoc();
                $companiID = $row['companyID'];
            }

            $this->connection->query("UPDATE user SET userType = 'management', companyID = '$companiID' WHERE userID = '$userID'");

            $this->response['error'] = FALSE;
            $this->response['message'] = "Company registration successfull";

            if ($image === '') {
                
            } else {
                file_put_contents($upladPath, base64_decode($image));
            }
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Company registration failed, please try again! ". mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

    public function getAllCompanies() {
        $result = $this->connection->query("SELECT * FROM company");
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {

                // Get bus count for the current company being iterated over
                $busCout;
                $companyID = $row["companyID"];
                $result2 = $this->connection->query("SELECT COUNT(bus.busID) AS busCount FROM company INNER JOIN bus ON company.companyID = bus.companyID WHERE company.companyID = '$companyID';");
                if ($result2->num_rows > 0) {
                    while ($row2 = $result2->fetch_assoc()) {
//                        if ($row2["busCount"] === 1) {
                            $busCout = $row2["busCount"];
//                        } else {
//                            $busCout = $row2["busCount"] . ' Available Buses';
//                        }
                    }
                }

                $starRating;
                $result3 = $this->connection->query("SELECT AVG(stars) AS starRating FROM company, review WHERE company.companyID = review.companyID AND company.companyID = '$companyID';");
                if ($result3->num_rows > 0) {
                    while ($row3 = $result3->fetch_assoc()) {
                        $starRating = number_format((float) $row3["starRating"], 1, '.', '');
                    }
                }
                $ratingCount;
                $result4 = $this->connection->query("SELECT COUNT(review.companyID) AS ratingCount FROM company, review WHERE company.companyID = review.companyID AND company.companyID = '$companyID';");
                if ($result4->num_rows > 0) {
                    while ($row4 = $result4->fetch_assoc()) {
                        $ratingCount = $row4["ratingCount"];
                    }
                }

                $ratingStats = $starRating . ' (' . $ratingCount . ')';

                // Add/push associative array into an array
                array_push($this->responce, array("companyID" => $row["companyID"], "image" => $row["image"], "name" => $row["name"], "details" => $row["details"], "phone" => $row["phone"], "email" => $row["email"], "website" => $row["website"], "rating" => $starRating, "busCount" => $busCout, "ratingStats" => $ratingStats));
            }
            $errorStatus = FALSE;
            echo json_encode(array("errorStatus" => $errorStatus, "records" => $this->responce)); // put this outside if(){}
        }
//        else if ($result->num_rows === 0) {
//            $this->responce["error"] = TRUE;
//            $this->responce["message"] = 'No companies have been added';
//        }
        //turn array into json format
//        echo json_encode(array("errorStatus" => $errorStatus, "records" => $this->responce));***********
    }

    public function editCompany() {

        $companyID = $_POST['companyID'];
        $name = mysqli_real_escape_string($this->connection, $_POST['name']);
        $description = mysqli_real_escape_string($this->connection, $_POST['desc']);
        $phone = $_POST['phone'];
        $email = $_POST['email'];
        $website = $_POST['website'];

        $image = ''; //insert default image if provided, put image in dir
        $url = DatabaseConnect::getServerIP() . "/uploads/bus.jpg";

        if (isset($_POST['image'])) {
            $image = $_POST['image'];
            $uid = uniqid($phone, true);
            $upladPath = './uploads/' . $uid . '.' . 'jpg';
			$upladPath2 = '/uploads/' . $uid . '.' . 'jpg';
            $url = DatabaseConnect::getServerIP() . "$upladPath2";
        }

        $result = $this->connection->query("UPDATE company SET image = '$url', name = '$name', details = '$description', phone = '$phone', email = '$email', website = '$website' WHERE companyID = '$companyID'");

        if ($result) {

            if ($image === '') {
                
            } else {
                file_put_contents($upladPath, base64_decode($image));
            }

            $this->response['error'] = FALSE;
            $this->response['message'] = "Company editing successfull";
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Editing company failed, please try again!";
        }
        echo json_encode($this->response);
    }

    public function deleteCompany() {
        $companyID = $_POST['compID'];
//        "DELETE FROM MyGuests WHERE id=3
        $result = $this->connection->query("DELETE FROM company where companyID = '$companyID'");
        if ($result) {
            $this->response['error'] = FALSE;
            $this->response['message'] = "Company deleted successfull";
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Deleting company failed, please try again! <br>".mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

    public function getResponse() {
        return $this->responce;
    }

    public function __destruct() {
        $this->connection->close();
    }

}

$newCompany = new Company;
$response = $newCompany->getResponse();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    //add company
    if (isset($_POST['userID']) and isset($_POST['name']) and isset($_POST['description']) and isset($_POST['phone']) and isset($_POST['email']) and isset($_POST['website'])) {
        $newCompany->addCompany();
    }
    //edit company
    if (isset($_POST['companyID']) and isset($_POST['name']) and isset($_POST['desc']) and isset($_POST['phone']) and isset($_POST['email']) and isset($_POST['website'])) {
        $newCompany->editCompany();
    }
    //delete compam=ny
    if (isset($_POST['compID'])) {
        $newCompany->deleteCompany();
    }

    $newCompany->getAllCompanies();
} else {
    $response['error'] = TRUE;
    $response['message'] = 'Invalid request!';
    echo json_encode($response);
}
unset($newCompany);
