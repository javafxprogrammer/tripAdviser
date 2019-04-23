<?php


class DatabaseConnect {
    
    private $serverName = "ENTER YOUR SERVER NAME(IP ADDRESS) EG localhost";
    private $userName = "ENTER YOUR USERNAME";
    private $password = "ENTER YOUR PASSWORD";
    private $databaseName = 'id7750263_tripadviserdb';
    private $sql = 'CREATE DATABASE IF NOT EXISTS id7750263_tripadviserdb';

/*	private $serverName = "localhost";
    private $userName = "root";
    private $password = "";
    private $databaseName = 'id7750263_tripadviserdb';
    private $sql = 'CREATE DATABASE IF NOT EXISTS id7750263_tripadviserdb'; */
    private $connection2;
    private $response2 = array();

    protected function connect() {
         
        $conn = new mysqli($this->serverName, $this->userName, $this->password);
		$this->connection2 = $conn;

        if ($conn->connect_error) {
            die('Connection failed: ' . $conn->connect_error);
        }
        
        if($conn->query($this->sql) === TRUE){
             $conn->select_db($this->databaseName);
//             $conn->query("SET NAMES  'utf8'");
			 $this->createTables();
             return $conn;
        } else{
            die('Failed to create the database: ' . $conn->connect_error);
        } 
    }

    public static function getServerIP() {
        return 'ENTER YOUR SERVER NAME(IP ADDRESS)';
    }

	public function createTables(){

$filename = 'id7750263_tripadviserdb.sql';
		// Temporary variable, used to store current query
$templine = '';
// Read in entire file
$lines = file($filename);
// Loop through each line
foreach ($lines as $line)
{
// Skip it if it's a comment
if (substr($line, 0, 2) == '--' || $line == '')
    continue;

// Add this line to the current segment
$templine .= $line;
// If it has a semicolon at the end, it's the end of the query
if (substr(trim($line), -1, 1) == ';')
{
    // Perform the query
	if(!$this->connection2->query("$templine")){
/*	$this->response2['error'] = TRUE;
	$this->response2['message'] = mysqli_error($this->connection2);
	echo json_encode($this->response2); */
}
    // Reset temp variable to empty
    $templine = '';
}
}

   /* $this->response2['error'] = FALSE;
	$this->response2['message'] = 'TABLES SUCCESSFULLY CREATED';
	echo json_encode($this->response2); */
}
}

