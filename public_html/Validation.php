<?php

class Validation {

    //Booking
    public function isValidDate($dayOfWeek, $date) {
//        echo 'Day of week = '.date("l", strtotime($date)).'<br>';
        if ($dayOfWeek === date("l", strtotime($date))) {
            return true;
        } else {
            return false;
        }
    }

    public function __destruct() {
    }

}
