<?php
require_once "api.class.php";

function auto_cast($row) {
	foreach ($row as $key => $value) {
		if (ctype_digit($value)) {
			$row[$key] = (int) $value;
		}
		elseif (is_numeric($value)) {
			$row[$key] = (float) $value;
		}
		else {
			$row[$key] = (string) $value;
		}
	}
	return $row;
}


class eFJenergyAPI extends API
{
	protected $User;
	protected $mysqli;

	public function __construct($request, $origin) {
		parent::__construct($request);

		/* Connect to the MySQL database */
		$mysqli = new mysqli('localhost','eFJenergy', 'D27edY3ZChcR6CmP','eFJenergy');
		
		$APIKey = "01e3567f7b2a485aa38292dbe620f366";
		
		if (!array_key_exists('apiKey', $this->request)) {
			throw new Exception('No API Key provided');
		} else if (!$APIKey==$this->request['apiKey']) {
			throw new Exception('Invalid API Key');
		} 
	}

	protected function getmeterreadingscurrent() {
		header("Expires: 0");
		if ($this->method == 'GET')
		{
			$mysqli = new mysqli('localhost','eFJenergy', 'D27edY3ZChcR6CmP','eFJenergy');
			$result = $mysqli->query("SELECT TotalkWhTarif1,TotalkWhTarif2,DATE_FORMAT(MeasurementTimestamp, '%Y-%m-%dT%TZ') as MeasurementTimestamp FROM `ElectricityP1` ORDER BY MeasurementTimestamp DESC LIMIT 1"); 
			if ( $result )
			{
				$row = $result->fetch_assoc();
				$row = auto_cast($row);
				return json_encode($row);
			}
			else
			{
				throw new Exception("Error reading from database");
			}
		}
		else
		{
			return "Only accepts GET requests";
		}
	}	

	protected function getmeterreadingslastweek() {
		header("Expires: 0");
		if ($this->method == 'GET')
		{
			$mysqli = new mysqli('localhost','eFJenergy', 'D27edY3ZChcR6CmP','eFJenergy');
			$result = $mysqli->query("(SELECT TotalkWhTarif1,TotalkWhTarif2, DATE_FORMAT(MeasurementTimestamp, '%Y-%m-%dT%TZ') as MeasurementTimestamp FROM `ElectricityP1` ORDER BY MeasurementTimeStamp DESC LIMIT 1) ".
				"UNION ALL (SELECT Min(TotalkWhTarif1) as TotalkWhTarif1 , Min(TotalkWhTarif2) as TotalkWhTarif2,DATE_FORMAT(MIN(MeasurementTimestamp), '%Y-%m-%dT%TZ') as MeasurementTimestamp FROM  `ElectricityP1` ".
				"WHERE MeasurementTimestamp >= DATE_SUB(NOW(),INTERVAL 7 DAY) ".
				"GROUP BY ROUND( UNIX_TIMESTAMP( MeasurementTimestamp ) / ( 7200 ) )) ORDER BY MeasurementTimestamp ASC");
			if ( $result )
			{
				$rows=array();
				while ( $row = $result->fetch_assoc() )
				{
					$row = auto_cast($row);
					$rows[] = $row;
				}
				return $rows;
			}
			else
			{
				throw new Exception("Error reading from database " . mysqli_error($mysqli));
			}
		}
		else
		{
			throw new Exception("Only accepts GET requests");
		}
	}	

	protected function getpowerusage( $args )
	{
		header("Expires: 0");
		if ($this->method == 'GET')
		{
			if ( is_array($args) && count($args)==3) 
			{
				$startdt = null;
				$enddt = null;
				try
				{
					$startdt = DateTime::createFromFormat(DateTime::ISO8601,$args[0]);
					$enddt   = DateTime::createFromFormat(DateTime::ISO8601,$args[1]);
					if ( $startdt == null || $enddt == null )
					{
						throw new Exception("No valid time format.");
					}
				}
				catch (Exception $e)
				{
					throw new Exception("Arguments for time interval not valid.\n" . $e->getMessage());
				}
				
				$precision = null;
				try
				{
					$precision = intval($args[2]);
					if ( $precision <= 0 ) throw new Exception("Not a valid precision.");
				}
				catch (Exception $e)
				{
					throw new Exception("Argument for precision not valid.\n" . $e->getMessage());
				}
				
				
				$sampleinterval_seconds = ($enddt->getTimestamp() - $startdt->getTimestamp())/$precision;

				$mysqli = new mysqli('localhost','eFJenergy', 'D27edY3ZChcR6CmP','eFJenergy');
				
				$result = $mysqli->query("SELECT MAX( TotalkWhTarif1 ) - MIN( TotalkWhTarif1 ) + MAX( TotalkWhTarif2 ) - MIN( TotalkWhTarif2 ) AS kWattHourTotal FROM `ElectricityP1` " . 
					"WHERE MeasurementTimestamp>='" . date_format($startdt, 'Y-m-d H:i:s') . "' AND MeasurementTimestamp<='" . date_format($enddt, 'Y-m-d H:i:s') . "' ");				

				$TotalkWh = 0;
				if ( $result )
				{
					$obj = $result->fetch_object();
					$TotalkWh = $obj->kWattHourTotal;
				}
				else
				{
					throw new Exception("Error reading from database " . mysqli_error($mysqli));
				}
				
				$result = $mysqli->query("SELECT DATE_FORMAT(from_unixtime(AVG(UNIX_TIMESTAMP(MeasurementTimestamp))), '%Y-%m-%dT%TZ')  As MeasurementTimestamp ,AVG(CurrentUsagekWatt)*1000 as WattAverage,MAX(CurrentUsagekWatt)*1000 as WattHigh,MIN(CurrentUsagekWatt)*1000 as WattLow FROM `ElectricityP1` " . 
					"WHERE MeasurementTimestamp>='" . date_format($startdt, 'Y-m-d H:i:s') . "' AND MeasurementTimestamp<='" . date_format($enddt, 'Y-m-d H:i:s') . "' " .
					"GROUP BY ROUND(UNIX_TIMESTAMP(MeasurementTimestamp)/(" . $sampleinterval_seconds .")) ORDER BY MeasurementTimestamp;");				

				$rows=array();
				if ( $result )
				{
					while ( $row = $result->fetch_assoc() )
					{
						$row = auto_cast($row);
						$rows[] = $row;
					}
				}
				else
				{
					throw new Exception("Error reading from database " . mysqli_error($mysqli));
				}
				return array( "TotalkWh" => $TotalkWh, 
					"MeasurementStart" => $startdt->format(DateTime::ISO8601),
					"MeasurementEnd" => $enddt->format(DateTime::ISO8601),
					"PowerUsageReadings" => $rows );
			}
			else
			{
				throw new Exception("Arguments for time interval not set");
			}
		}
		else
		{
			throw new Exception("Only accepts GET requests");
		}
	}
}
?>