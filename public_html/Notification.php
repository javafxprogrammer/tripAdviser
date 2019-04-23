<?php

/**
 * Description of Notification class
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class Notification extends DatabaseConnect {

    private $connection;
    private $response = array();
    private $errorStatus = TRUE;

    private $updateNotificationStatus = "update booking set notificationStatus = 1 where "
            . "booking.seatNumber = ? and "
            . "booking.date_ = ? and booking.dayOfWeek = ? and "
            . "booking.time_ = ? and booking.from_ = ? and "
            . "booking.to_ = ? and booking.busID = ?";

    public function getNotificationData($userID) {

        $stmt = $this->connection->query("SELECT booking.userID, booking.seatNumber, booking.date_, booking.dayOfWeek, booking.time_, booking.from_, booking.to_, booking.busID
        from user, booking, timeTable, trip, bus, company
        where user.userID = booking.userID and 
        booking.dayOfWeek = timeTable.dayOfWeek and 
        booking.time_ = timeTable.time_ and 
        booking.from_ = timeTable.from_ and 
        booking.to_ = timeTable.to_ and 
        booking.busID = timeTable.busID and 
        timeTable.from_ = trip.from_ and 
        timeTable.to_ = trip.to_ and 
        timeTable.busID = trip.busID and 
        trip.busID = bus.busID and 
        bus.companyID = company.companyID and 
        DATEDIFF(booking.date_, CURDATE()) <= 1 AND 
        user.userID = '$userID' and 
        booking.notificationStatus = 0");

        if ($stmt->num_rows > 0) {
            $this->errorStatus = FALSE;
            while ($row = $stmt->fetch_assoc()) {
                array_push($this->response, array("seatNumber" => $row["seatNumber"], "date" => $row["date_"], "dayOfWeek" => $row["dayOfWeek"],
                    "time" => $row["time_"], "from" => $row["from_"], "to" => $row["to_"], "busID" => $row["busID"]));
            }
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
    }

    public function updateNotification($seatNum, $date, $dayOfWeek, $time, $from, $to, $busID) {

        $stmt2 = $this->connection->prepare($this->updateNotificationStatus);
        $stmt2->bind_param('isssssi', $seatNum, $date, $dayOfWeek, $time, $from, $to, $busID);
        if ($stmt2->execute()) {
            $this->response['error'] = FALSE;
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = 'Failed to update notification status ' . mysqli_error($this->connection);
        }
        
        echo json_encode($this->response);
    }

    public function getResponse() {
        return $this->response;
    }

    public function __construct() {
        $this->connection = $this->connect();
    }

    public function __destruct() {
        $this->connection->close();
    }
}

$newNotification = new Notification;
$response = $newNotification->getResponse();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['userID_getNotificationData'])) {
        $newNotification->getNotificationData($_POST['userID_getNotificationData']);
    }
    if (isset($_POST['seatNumber_u']) and isset($_POST['date_u']) and
            isset($_POST['dayOfWeek_u']) and isset($_POST['time_u']) and isset($_POST['from_u']) and
            isset($_POST['to_u']) and isset($_POST['busID_u'])) {
        $newNotification->updateNotification($_POST['seatNumber_u'], $_POST['date_u'], $_POST['dayOfWeek_u'], $_POST['time_u'], $_POST['from_u'], $_POST['to_u'], $_POST['busID_u']);
    }
} else {
    $response['error'] = TRUE;
    $response['message'] = "Invalid request!";
    echo json_encode($response);
}
unset($newNotification);
