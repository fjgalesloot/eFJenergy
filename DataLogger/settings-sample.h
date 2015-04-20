
//#define NOMYSQL 		// define to output MySQL statements to debug output
//#define NOEMONCMS 	// define to output Emoncms query to debug output
//#define DEBUG 		// define to output debug messages to stdout
//#define ERROR 		// define to output extra error messages to stderr

// COM port settings
// Look in rs232.c for device in comport array: 16 = /dev/ttyUSB0
const int comport_device_p1 = 16; 
const int comport_baud_p1 = 115200;
// Max delay for receiving p1 telegram before resetting the COM port
const int maxdelay_p1 = 20;

// MySQL server details
const char *mysql_server = "server.full.name";
const char *mysql_database = "database";
const char *mysql_username = "user";
const char *mysql_password = "password";

// Emoncms details
const char *emoncms_server = "server.full.name";
const int emoncms_port = 80;
// Fill in correct URL, Node number and API key (write)
const char *emoncms_urlbuilder = "/api/post.json?node=1&apikey=<apikey>&";
