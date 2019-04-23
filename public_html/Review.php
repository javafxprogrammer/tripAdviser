<?php

include_once dirname(__FILE__) . '/DatabaseConnect.php';

/**
 * Description of Review
 *
 * @author lengwe
 */
class Review extends DatabaseConnect {

    private $connection;
    private $response = array();
    private $errorStatus = TRUE;

    function __construct() {
        $this->connection = $this->connect();
    }

    public function getResponse() {
        return $this->response;
    }

    public function getReviews($compayID) {

        // get review id
        $result = $this->connection->query("SELECT "
                . "user.userID, user.firstName, user.lastName, user.profilePicture, review.details, review.stars, review.date_, company.companyID, review.reviewID "
                . "FROM "
                . "user, review, company WHERE company.companyID = review.companyID AND review.userID = user.userID "
                . "AND company.companyID = '$compayID';");

        if ($result->num_rows > 0) {
            $this->errorStatus = FALSE;
            while ($row = $result->fetch_assoc()) {
                array_push($this->response, array("userID" => $row["userID"], "firstName" => $row["firstName"], "lastName" => $row["lastName"], "profilePicture" => $row["profilePicture"], "details" => $row["details"], "stars" => $row["stars"], "date" => $row["date_"], "companyID" => $row["companyID"], "reviewID" => $row["reviewID"]));
            }
        } else {
//            $this->errorStatus['message'] = 'Failed to retieve user reviews! ' . mysqli_error($this->connection);
        }
        echo json_encode(array("errorStatus" => $this->errorStatus, "records" => $this->response));
    }

    public function addReview($companyID, $rating, $details, $userID) {

     $details2 = mysqli_real_escape_string($this->connection,$details);

        if ($this->connection->query("INSERT INTO `review` 
            (`reviewID`, `companyID`, `userID`, `details`, `stars`, `date_`) 
            VALUES (NULL, '$companyID', '$userID', '$details2', '$rating', "
                        . "CURDATE());")) {

            $this->response['error'] = FALSE;
            $this->response['message'] = 'Review successfully submited';
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = 'Failed to add review, Please try again! '
                    . mysqli_error($this->connection);
        }

        echo json_encode($this->response);
    }

    public function editReview($reviewID, $rating, $details) {

		$details2 = mysqli_real_escape_string($this->connection, $details);

        if ($this->connection->query("UPDATE review SET details = '$details2', stars = '$rating', date_ = CURDATE() WHERE "
                        . "reviewID = '$reviewID';")) {
            $this->response['error'] = FALSE;
            $this->response['message'] = 'Review has been updated successfully';
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = 'Failed to update review, please try again' . mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

    public function deleteReview($reviewID) {

        if ($this->connection->query("DELETE FROM review WHERE reviewID = '$reviewID';")) {
            $this->response['error'] = FALSE;
            $this->response['message'] = 'Review deleted successfully';
        } else {
            $this->response['error'] = TRUE;
            $this->response['message'] = 'Failed to delete review, please try again' . mysqli_error($this->connection);
        }
        echo json_encode($this->response);
    }

    public function __destruct() {
        $this->connection->close();
    }

}

$newReview = new Review;
$response = $newReview->getResponse();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['reviewID_editReview']) and isset($_POST['rating_editReview']) and isset($_POST['details_editReview'])) {
        $newReview->editReview($_POST['reviewID_editReview'], $_POST['rating_editReview'], $_POST['details_editReview']);
    }

    if (isset($_POST['reviewID_deleteReview'])) {
        $newReview->deleteReview($_POST['reviewID_deleteReview']);
    }

    if (isset($_POST['companyID_getReview'])) {
        $compayID = $_POST['companyID_getReview'];
        $newReview->getReviews($compayID);
    }

    if (isset($_POST['companyID_addRating']) && isset($_POST['rating_addRating']) && isset($_POST['details_addRating']) && isset($_POST['userID_addRating'])) {
        $newReview->addReview($_POST['companyID_addRating'], $_POST['rating_addRating'], $_POST['details_addRating'], $_POST['userID_addRating']);
    }
} else {
    $response['error'] = TRUE;
    $response['message'] = "Invalid request!";
    echo json_encode($response);
}
unset($newReview);
