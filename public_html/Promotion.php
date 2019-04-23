<?php

/**
 * Description of home
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class Promotion extends DatabaseConnect {

    private $connection;
    private $response = array();
    private $errorStatus = TRUE;

    function __construct() {
        $this->connection = $this->connect();
    }

    public function addPromo($company, $details, $tickeCount, $discount) {

            $result = $this->connection->query("SELECT * FROM promotion WHERE companyID = '$company'");

        if ($result->num_rows > 0) {

            if ($this->connection->query("UPDATE promotion SET details = '$details', ticketCountForDiscout = '$tickeCount', "
                            . "discount = '$discount' WHERE companyID = '$company'")) {
                $this->response['error'] = FALSE;
                $this->response['message'] = 'Update of promotion was successfull';
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = 'failed to update promotion details ' . mysqli_error($this->connection);
            }
        } else {

            if ($this->connection->query("INSERT INTO `promotion` (`companyID`, `details`, `ticketCountForDiscout`, `discount`) 
            VALUES ('$company', '$details', '$tickeCount', '$discount')")) {
                $this->response['error'] = FALSE;
                $this->response['message'] = 'Adding of promotion was successfull';
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = 'failed to insert promotion details ' . mysqli_error($this->connection);
            }
        }
        
        echo json_encode($this->response);
    }

    public function getPromo($userID) {
        $result = $this->connection->query("
        SELECT company.companyID, user.userID, count(booking.userID) as numberOfBookings, company.name, promotion.ticketCountForDiscout, 
        promotion.discount 
        from user, booking, timeTable, trip, bus, company, promotion 
        where user.userID = booking.userID and booking.dayOfWeek = timeTable.dayOfWeek and booking.time_ = timeTable.time_ and 
        booking.from_ = timeTable.from_ and booking.to_ = timeTable.to_ and booking.busID = timeTable.busID and 
        timeTable.from_ = trip.from_ and timeTable.to_ = trip.to_ and timeTable.busID = trip.busID and trip.busID = bus.busID and 
        bus.companyID = company.companyID and company.companyID = promotion.companyID and user.userID = '$userID' GROUP BY company.companyID;");
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                if ($row['ticketCountForDiscout'] !== 0 and $row['ticketCountForDiscout'] !== null and $row['numberOfBookings'] % $row['ticketCountForDiscout'] === 0) {
                    $this->errorStatus = FALSE;
                    array_push($this->response, array(""
                        . "userID" => $row["userID"], "numberOfBookings" => $row["numberOfBookings"], "companyID" => $row["companyID"],
                        "CompanyName" => $row["name"], "discount" => $row["discount"]));
                }
            }
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
    }

    public function getResponse() {
        return $this->response;
    }

    public function __destruct() {
        $this->connection->close();
    }

}

$newPromotion = new Promotion;
$response = $newPromotion->getResponse();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['userID_getPromo'])) {
        $newPromotion->getPromo($_POST['userID_getPromo']);
    }
    
    if(isset($_POST['companyID']) and isset($_POST['details']) and isset($_POST['tickeCount']) and isset($_POST['discount'])){
        $newPromotion->addPromo($_POST['companyID'], $_POST['details'], $_POST['tickeCount'], $_POST['discount']);
    }
} else {
    $response['error'] = TRUE;
    $response['message'] = "Invalid request!";
    echo json_encode($response);
}
unset($newPromotion);
