<?php

/**
 * Description of TripTimeTable
 *
 * @author lengwe
 */
include_once dirname(__FILE__) . '/DatabaseConnect.php';

class TripTimeTable extends DatabaseConnect {

    private $connection;
    private $response = array();

    public function __construct() {
        $this->connection = $this->connect();
    }

    public function addSchedule($busID, $from, $to, $amount, $weekDay, $time) {

        if ($this->connection->query("INSERT INTO `trip` (`from_`, `to_`, `busID`, `amount`) VALUES ('$from', '$to', '$busID', '$amount')")) {
            if ($this->connection->query("INSERT INTO `timeTable` (`dayOfWeek`, `time_`, `from_`, `to_`, `busID`) VALUES "
                            . "('$weekDay', '$time', '$from', '$to', '$busID')")) {

                $this->response['error'] = FALSE;
                $this->response['message'] = "Schedule successfull added";
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = "Failed to add time table, please try again";
            }
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to add trip, please try again";
        }

        echo json_encode($this->response);
    }

    public function editSchedule() {
        $busID_x = $_POST['busID_updateSchedule_x'];
        $from_x = $_POST['from_updateSchedule_x'];
        $to_x = $_POST['to_updateSchedule_x'];
        $from = $_POST['from_updateSchedule'];
        $to = $_POST['to_updateSchedule'];
        $amount = $_POST['amount_updateSchedule'];
        $weekDay = $_POST['weekDay_updateSchedule'];
        $time = $_POST['time_updateSchedule'];

        if ($this->connection->query("UPDATE trip SET from_ = '$from', to_ = '$to', amount = '$amount' "
                        . "WHERE busID = '$busID_x' AND from_ = '$from_x' AND to_ = '$to_x'")) {
            if ($this->connection->query("UPDATE timeTable SET dayOfWeek = '$weekDay', time_ = '$time' "
                            . "WHERE busID = '$busID_x' AND from_ = '$from' AND to_ = '$to'")) {

                $this->response['error'] = FALSE;
                $this->response['message'] = "Schedule successfull updated";
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = "Failed to update time table, please try again";
            }
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = "Failed to update trip, please try again" . mysqli_error($this->connection);
        }

        echo json_encode($this->response);
    }

  public function deleteSchedule() {
        $from = $_POST['from_deleteSchedule'];
        $to = $_POST['to_deleteSchedule'];
        $busID = $_POST['busID_deleteSchedule'];

        //added
        $res = $this->connection->query("select busID from trip where busID = '$busID'");
        if ($res->num_rows === 1) {
            $this->connection->query("delete from bus where busID = '$busID'");
            $this->response['error'] = FALSE;
            $this->response['message'] = "Bus & last schedule successfull deleted";
        } else {

            if ($this->connection->query("DELETE FROM trip WHERE from_ = '$from' AND to_ = '$to' AND busID = '$busID'")) {
                $this->response['error'] = FALSE;
                $this->response['message'] = "Schedule successfull deleted";
            } else {
                $this->response['error'] = TRUE;
                $this->response['message'] = "Failed to delete schedule, please try again";
            }
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

$newTripTimeTable = new TripTimeTable;
$response = $newTripTimeTable->getResponse();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    if (isset($_POST['busID_addSchedule']) and isset($_POST['from_addSchedule']) and isset($_POST['to_addSchedule'])
            and isset($_POST['amount_addSchedule']) and isset($_POST['weekDay_addSchedule']) and isset($_POST['time_addSchedule'])) {

        $busID = $_POST['busID_addSchedule'];
        $from = $_POST['from_addSchedule'];
        $to = $_POST['to_addSchedule'];
        $amount = $_POST['amount_addSchedule'];
        $weekDay = $_POST['weekDay_addSchedule'];
        $time = $_POST['time_addSchedule'];

        $newTripTimeTable->addSchedule($busID, $from, $to, $amount, $weekDay, $time);
    }
    if (isset($_POST['busID_updateSchedule_x']) and isset($_POST['from_updateSchedule_x']) and isset($_POST['to_updateSchedule_x']) and
            isset($_POST['from_updateSchedule']) and isset($_POST['to_updateSchedule']) and isset($_POST['amount_updateSchedule']) and
            isset($_POST['weekDay_updateSchedule']) and isset($_POST['time_updateSchedule'])) {
        $newTripTimeTable->editSchedule();
    }
    if (isset($_POST['from_deleteSchedule']) and isset($_POST['to_deleteSchedule']) and isset($_POST['busID_deleteSchedule'])) {
        $newTripTimeTable->deleteSchedule();
    }
} else {
    $response['error'] = TRUE;
    $response['message'] = 'Invalid request!';
    echo json_encode($response);
}

unset($newTripTimeTable);
