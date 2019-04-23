<?php

include_once dirname(__FILE__) . '/DatabaseConnect.php';

class Booking extends DatabaseConnect {

    private $connection;
    private $response = array();
    private $errorStatus = TRUE;

    function __construct() {
        $this->connection = $this->connect();
    }

    public function deleteBooking() {

        $seatNumber = $_POST['seatNumber_delete'];
        $date = $_POST['date_delete'];
        $weekDay = $_POST['weekDay_delete'];
        $time = $_POST['time_delete'];
        $from = $_POST['from_delete'];
        $to = $_POST['to_delete'];
        $busID = $_POST['busID_delete'];

        if ($this->connection->query("DELETE FROM booking WHERE seatNumber = '$seatNumber' AND date_ = '$date' AND dayOfWeek = '$weekDay' AND time_ = '$time' AND from_ = '$from' "
                        . "AND to_ = '$to' AND busID = '$busID';")) {
            $this->response['error'] = FALSE;
            $this->response['message'] = 'Booking successfully canceled';
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = 'Failed to cancel booking, please try again'. mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

    public function editBooking() {
        $bookingRecords = json_decode($_POST['bookingRecords_edit'], TRUE);

        $seatNumber_x = $_POST['seatNumber_edit_x'];
        $date_x = $_POST['date_edit_x'];
        //PRIMARY KEY(seatNumber, date_, dayOfWeek, time_, from_, to_, busID)
        foreach ($bookingRecords as $val) {
            //primary key
            //new filds
            $seatNumber = $val['seatNumber'];
            $date = $val['date'];
            // old fields
            $dayOfWeek = $val['dayOfWeek'];
            $time = $val['time'];
            $from = $val['from'];
            $to = $val['to'];
            $busID = $val['busID'];

            if ($this->connection->query("UPDATE booking SET seatNumber = '$seatNumber', date_ = '$date' "
                            . "WHERE seatNumber = '$seatNumber_x' AND date_ = '$date_x' AND dayOfWeek = '$dayOfWeek' "
                            . "AND time_ = '$time' AND from_ = '$from' AND to_ = '$to' AND busID = '$busID';")) {
                $this->response['error'] = FALSE;
                $this->response['message'] = 'Booking successfully updated';
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = 'Failed to update booking, please try again' . mysqli_error($this->connection);
            }
        }
        echo json_encode($this->response);
    }

    public function addBooking($userID, $seatNumber, $date, $dayOfWeek, $time, $from, $to, $busID, $notf) {
        $stmt = $this->connection->prepare("INSERT INTO booking(userID, seatNumber, date_, dayOfWeek, time_, from_, to_, busID, notificationStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        $stmt->bind_param('iisssssii', $userID, $seatNumber, $date, $dayOfWeek, $time, $from, $to, $busID, $notf);
        if ($stmt->execute() and mysqli_affected_rows($this->connection) > 0) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public function bookingProcessing() {
        $status = array();
//        $status['error'] = true;
        $bookingRecords = json_decode($_POST['bookingRecords'], TRUE);

        foreach ($bookingRecords as $val) {
            $bookingStatus = $this->addBooking($val['userID'], $val['seatNumber'], $val['date'], $val['dayOfWeek'], $val['time'], $val['from'], $val['to'], $val['busID'], 0);

            if ($bookingStatus === FALSE) {
                array_push($status, array("error" => TRUE, "seatNumber" => $val['seatNumber'], "errorMessage"=> mysqli_error($this->connection)));
            }
        }
        echo json_encode(array("result" => $status));
    }

    public function getBookedSeats() {
        $date = $_POST['date'];
        $dayOfWeek = $_POST['dayOfWeek'];
        $time = $_POST['time'];
        $from = $_POST['from'];
        $to = $_POST['to'];
        $busID = $_POST['busID'];
        $stmt = $this->connection->query("SELECT booking.seatNumber FROM booking WHERE date_ = '$date' AND dayOfweek = '$dayOfWeek' AND time_ = '$time' AND from_ = '$from' AND to_ = '$to' AND busID = '$busID';");

        if ($stmt->num_rows > 0) {
            while ($row = $stmt->fetch_assoc()) {
                array_push($this->response, array("seatNumber" => $row["seatNumber"]));
            }
        }
        echo json_encode(array("records" => $this->response));
    }

    public function getUserBookings($userID) {
        $stmt = $this->connection->query("SELECT "
                . "booking.seatNumber, booking.date_, booking.dayOfWeek, booking.time_, "
                . "booking.from_, booking.to_, booking.busID, company.name, bus.seats "
                . "from user, booking, timeTable, trip, bus, company "
                . "where user.userID = booking.userID and booking.dayOfWeek = timeTable.dayOfWeek and "
                . "booking.time_ = timeTable.time_ and booking.from_ = timeTable.from_ and "
                . "booking.to_ = timeTable.to_ and booking.busID = timeTable.busID and "
                . "timeTable.from_ = trip.from_ and timeTable.to_ = trip.to_ and "
                . "timeTable.busID = trip.busID and trip.busID = bus.busID and "
                . "bus.companyID = company.companyID AND booking.userID = '$userID' ORDER BY booking.date_ DESC;");
        if ($stmt->num_rows > 0) {
            while ($row2 = $stmt->fetch_assoc()) {
                array_push($this->response, array("seatNumber" => $row2["seatNumber"], "date" => $row2["date_"], "dayOfWeek" => $row2["dayOfWeek"], "time" => $row2["time_"], "from" => $row2["from_"], "to" => $row2["to_"], "busID" => $row2["busID"], "companyName" => $row2["name"], "seatCount" => $row2["seats"]));
            }
            $this->errorStatus = FALSE;
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
    }
    
    public function getBookingCount($userID){
        $result = $this->connection->query("SELECT COUNT(booking.userID) AS `bookingCount` FROM booking WHERE userID = '$userID';");
        
        $this->response['error'] = TRUE;

        if($result->num_rows > 0){
            $this->response['error'] = FALSE;
            $row = $result->fetch_assoc();
            $this->response['message'] = $row["bookingCount"];
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

$newBooking = new Booking;
$response = $newBooking->getResponse();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    
    if(isset($_POST['userID_getBookingCount'])){
        $newBooking->getBookingCount($_POST['userID_getBookingCount']);
    }

    if (isset($_POST['seatNumber_delete']) and isset($_POST['date_delete']) and isset($_POST['weekDay_delete']) and
            isset($_POST['time_delete']) and isset($_POST['from_delete']) and isset($_POST['to_delete']) and isset($_POST['busID_delete'])) {
        $newBooking->deleteBooking();
    }

    if (isset($_POST['bookingRecords_edit']) and isset($_POST['seatNumber_edit_x']) and isset($_POST['date_edit_x'])) {
        $newBooking->editBooking();
    }

    if (isset($_POST['bookingRecords'])) {
        $newBooking->bookingProcessing();
    }

    if (isset($_POST['userID_getUserBookings'])) {
        $userID = $_POST['userID_getUserBookings'];
        $newBooking->getUserBookings($userID);
    }

    if (isset($_POST['date']) and isset($_POST['dayOfWeek']) and isset($_POST['time']) and isset($_POST['from']) and isset($_POST['to']) and isset($_POST['busID'])) {
        $newBooking->getBookedSeats();
    }
} else {
    $response['error'] = TRUE;
    $response['message'] = "Invalid request!";
    echo json_encode($response);
}
unset($newBooking);
