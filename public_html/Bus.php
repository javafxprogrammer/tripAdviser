<?php

/**
 * Description of Bus
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';
include_once dirname(__FILE__) . '/TripTimeTable.php';
include dirname(__FILE__) . '/Promotion.php';

class Bus extends DatabaseConnect {

    private $connection;
    private $response = array();
    private $response2 = array();
    private $errorStatus = TRUE;

//        private $response2 = array("key"=>"value");


    function __construct() {
        $this->connection = $this->connect();
//        $this->connection->query("SET NAMES  'utf8'");
    }

    public function getTripAndTimeTable() {
        $busID = $_POST['busID'];
        $result = $this->connection->query("SELECT trip.from_ as `from`, trip.to_ as `to`, trip.amount, timeTable.dayOfWeek as `day`, timeTable.time_ as `time` FROM bus, trip, timeTable WHERE bus.busID = trip.busID AND trip.from_ = timeTable.from_ AND trip.to_ = timeTable.to_ AND trip.busID = timeTable.busID AND bus.busID = '$busID';");
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                array_push($this->response, array_map('utf8_encode', array("from" => $row["from"], "to" => $row["to"], "amount" => $row["amount"], "day" => $row["day"], "time" => $row["time"])));
            }
            $this->errorStatus = FALSE;
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
    }

    public function getBuses($companyID, $userID) {


        $this->connection->query("SET NAMES  'utf8'");
        $result2 = $this->connection->query("SELECT company.name AS `companyName`, company.companyID, bus.busID, bus.name, seats, bus.description, "
                . "trip.from_ as `from`, "
                . "trip.to_ as `to`, trip.amount, timeTable.dayOfWeek as `day`, timeTable.time_ as `time` "
                . "FROM company, bus, trip, timeTable WHERE company.companyID = bus.companyID AND "
                . "bus.busID = trip.busID AND trip.from_ = timeTable.from_ AND trip.to_ = timeTable.to_ AND "
                . "trip.busID = timeTable.busID AND company.companyID = '$companyID' ORDER BY bus.busID;");

        $array = $this->getPromo($userID);

        if ($result2->num_rows > 0) {

            while ($row2 = $result2->fetch_assoc()) {
                foreach ($array as $element) {
                    if ($element["companyID"] === $row2["companyID"]) {
                        $row2["amount"] = $row2["amount"] - (($element["discount"] / 100) * $row2["amount"]);
                    }
                }
                array_push($this->response, array("companyName" => $row2["companyName"], "busID" => $row2["busID"], "name" => $row2["name"], 
                    "seats" => $row2["seats"], "description" => $row2["description"], "from" => $row2["from"], "to" => $row2["to"], 
                    "amount" => $row2["amount"], "day" => $row2["day"], "time" => $row2["time"]));
            }
            $this->errorStatus = FALSE;
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
    }

    public function addBus() {

        $busName = mysqli_real_escape_string($this->connection, $_POST['busName_addBus']);
        $seatCount = $_POST['busSeatCount_addBus'];
        $description = mysqli_real_escape_string($this->connection, $_POST['busDescription_addBus']);
        $companyID = $_POST['companyID_addBus'];
        $from = $_POST['from_addBus'];
        $to = $_POST['to_addBus'];
        $amount = $_POST['amount_addBus'];
        $weekDay = $_POST['weekDay_addBus'];
        $time = $_POST['time_addBus'];

        if ($this->connection->query("INSERT INTO `bus` (`busID`, `name`, `description`, `seats`, `companyID`) VALUES "
                        . "(NULL, '$busName', '$description', '$seatCount', '$companyID');")) {

            $result = $this->connection->query("select LAST_INSERT_ID() AS `busID`");

            if ($result->num_rows === 1) {
                $row = $result->fetch_assoc();
                $lastInsertBusID = $row['busID'];
                $newTimeTable = new TripTimeTable;
                $newTimeTable->addSchedule($lastInsertBusID, $from, $to, $amount, $weekDay, $time);
                unset($newTimeTable);

                $this->response['error'] = FALSE;
                $this->response['message'] = "Adding bus details successfull";
            }
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to add bus, please try again";
        }
        echo json_encode($this->response);
    }

    public function editBus() {
        $busName = mysqli_real_escape_string($this->connection, $_POST['busName_editBus']);
        $seatCount = $_POST['busSeatCount_editBus'];
        $description = mysqli_real_escape_string($this->connection,  $_POST['busDescription_editBus']);
        $busID = $_POST['busID_editBus'];

        if ($this->connection->query("UPDATE bus SET name = '$busName', description = '$description', seats = '$seatCount' WHERE busID = '$busID';")) {
            $this->response['error'] = FALSE;
            $this->response['message'] = "Editing bus details successfull";
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to edit bus details, please try again";
        }
        echo json_encode($this->response);
    }

    public function deleteBus() {
        $busID = $_POST['busID_deleteBus'];

        if ($this->connection->query("DELETE FROM bus WHERE busID = '$busID'")) {
            $this->response['error'] = FALSE;
            $this->response['message'] = "Deleting bus details successfull";
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to delete bus, please try again";
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
                    array_push($this->response2, array(""
                        . "userID" => $row["userID"], "numberOfBookings" => $row["numberOfBookings"], "companyID" => $row["companyID"],
                        "CompanyName" => $row["name"], "discount" => $row["discount"]));
                }
            }
        } else {
//            $this->response['message'] = "failed to get promotion data" . mysqli_error($this->connection);
//            array_push($this->response2, array("userID" => $row["userID"]));
        }
        return $this->response2;
    }

    // add $userID to paramiter
    public function getAllBuses($userID) {

        $this->connection->query("SET NAMES  'utf8'");
        // get companyID
        $result2 = $this->connection->query("SELECT "
                . "company.name AS `companyName`, company.companyID, bus.busID, bus.name, seats, bus.description, "
                . "trip.from_ as `from`, trip.to_ as `to`, "
                . "trip.amount, timeTable.dayOfWeek as `day`, timeTable.time_ as `time` FROM company, bus, "
                . "trip, timeTable WHERE company.companyID = bus.companyID AND bus.busID = trip.busID AND "
                . "trip.from_ = timeTable.from_ AND trip.to_ = timeTable.to_ AND trip.busID = timeTable.busID "
                . "ORDER BY bus.busID;");

        $array = $this->getPromo($userID);

        if ($result2->num_rows > 0) {

            while ($row2 = $result2->fetch_assoc()) {

                foreach ($array as $element) {
                    if ($element["companyID"] === $row2["companyID"]) {
                        $row2["amount"] = $row2["amount"] - (($element["discount"] / 100) * $row2["amount"]);
                    }
                }
                array_push($this->response, array("companyName" => $row2["companyName"], "busID" => $row2["busID"], "name" => $row2["name"], "seats" => $row2["seats"], "description" => $row2["description"], "from" => $row2["from"], "to" => $row2["to"], "amount" => $row2["amount"], "day" => $row2["day"], "time" => $row2["time"]));
            }
            $this->errorStatus = FALSE;
        } else {
            $this->response['message'] = "failed to get allBus data" . mysqli_error($this->connection);
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
        unset($newPromo);
    }

    public function getResponse() {
        return $this->response;
    }

    public function __destruct() {
        $this->connection->close();
    }

}

$newBus = new Bus;
$response = $newBus->getResponse();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (isset($_POST['userID_getAllbuses'])) {
        $newBus->getAllBuses($_POST['userID_getAllbuses']);
    }
    if (isset($_POST['companyID']) and isset($_POST['getCompanyBusedUserID'])) {
        $newBus->getBuses($_POST['companyID'], $_POST['getCompanyBusedUserID']);
    }
    if (isset($_POST['busName_addBus']) and isset($_POST['busSeatCount_addBus']) and isset($_POST['busDescription_addBus']) and isset($_POST['companyID_addBus'])) {
        $newBus->addBus();
    }
    if (isset($_POST['busName_editBus']) and isset($_POST['busSeatCount_editBus']) and isset($_POST['busDescription_editBus']) and isset($_POST['busID_editBus'])) {
        $newBus->editBus();
    }
    if (isset($_POST['busID_deleteBus'])) {
        $newBus->deleteBus();
    }
} else {
    $response['error'] = TRUE;
    $response['message'] = 'Invalid request!';
    echo json_encode($response);
}
unset($newBus);
