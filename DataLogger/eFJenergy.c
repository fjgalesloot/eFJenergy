/****************
* eFJenergy.c
* Used to get data from Arduino UNO micro controller with eFJenergy sketch 
* connected to a serial port and put that data in a mysql DB on the network.
*****************/

#include "eFJenergy.h"

//#define NOMYSQL // define to output MySQL statements to stdout
#define DEBUG
//#define ERROR

#ifdef DEBUG
#define printf_debug(fmt, args...)    printf(fmt, ## args);fflush(stdout)
#else
#define printf_debug(fmt, args...)    /* Don't do anything in release builds */
#endif


#ifdef ERROR
#define printf_error(fmt, args...)    fprintf(stderr,fmt, ## args);fflush(stderr)
#else
#define printf_error(fmt, args...)    /* Don't do anything in release builds */
#endif

#define printf_outputerror(fmt, args...)    fprintf(stderr,fmt, ## args);fflush(stderr)


/* This flag controls termination of the main loop. */
volatile sig_atomic_t keep_going = 1;

const int comport_device_p1 = 16;
const int comport_baud_p1 = 115200;
const int maxdelay_p1 = 20;

const char *mysql_server = "daffy.internal.triplew.nl";
const char *mysql_database = "eFJenergy";
const char *mysql_username = "eFJenergy";
const char *mysql_password = "D27edY3ZChcR6CmP";

const char *emoncms_server = "localhost";
const int emoncms_port = 80;
const char *emoncms_urlbuilder = "/input/post.json?node=1&apikey=a525c39d6c3cc524127076c5373f2669&";


pthread_mutex_t mysql_lock;

time_t  lastdata_p1;

#define MAX_NUM_P1_TELEGRAMS 5
#define MAX_SIZE_P1_TELEGRAMS 1536
p1_telegram_io_comm_struct p1_telegrams_io_comm[MAX_NUM_P1_TELEGRAMS];

#ifndef NOMYSQL
static MYSQL *mysql_conn; 
#endif

void get_data_electricity_p1(int status)
{
	printf_debug ("\nget_data_electricity_p1: started  ");
	int oldflag = fcntl(GetComportHandle(comport_device_p1), F_GETFL);
	fcntl(GetComportHandle(comport_device_p1), F_SETFL, oldflag-FASYNC);

	int n;
	char buf[4096]; // a 4k buffer *should* be large enough
	char *line;
	short allslotsfilled=1;
	struct timeval tv;

	p1_telegram_io_comm_struct *current_p1_telegram;
	for ( n=0; n<MAX_NUM_P1_TELEGRAMS; n++ )
	{
		if (p1_telegrams_io_comm[n].active == 0 )
		{
			current_p1_telegram = &p1_telegrams_io_comm[n];
			printf_debug ("using telegram io comm slot %d\n",n);
			allslotsfilled = 0;
			break;
		}
	}

	char *p1_telegram_running;
	if ( allslotsfilled == 1 )
	{
		// No empty slot found
		printf_outputerror("All slots for Telegrams filled, reading result but not saving to DB!");
		printf_debug (" all telegram io comm slots in use\n");
		// Read COM port and discard results afterwards
		p1_telegram_running = malloc(MAX_SIZE_P1_TELEGRAMS*sizeof(char));
	}
	else
	{
		current_p1_telegram->active=1;
		p1_telegram_running = current_p1_telegram->telegram;
	}

	char *p1_telegram_runningpos = p1_telegram_running;

	// Set time for last interrupt catch
	time(&lastdata_p1);
	
	// Allow for total delay of max 5s for getting all lines from Comport
	tv.tv_sec = 5;
	tv.tv_usec = 0;
	int p1_telegram_currentsize = 0;
	do
	{
		n = PollComportSelectLine(comport_device_p1, &tv, buf, 4095); 
		char *line = buf;
		int line_len = strlen(line);
		while ( line_len >0 && line[0] < 32 ) // Skip all non-printable ASCII characters on start of line
		{
			line+=sizeof(char);
			line_len--;
		}
		if ( strncmp(line,"/KFM5KAIFA-METER",16)==0 ) continue; //Skip this line
		if ( line[0] == '!' ) break; //End of P1 telegram
		if ( p1_telegram_currentsize += line_len+3 < MAX_SIZE_P1_TELEGRAMS )
		{
			//printf_debug(" line                   =%s line_len=%d\n",line,line_len);
			strcpy(p1_telegram_runningpos, line);
			//printf_debug(" p1_telegram_runningpos =%s\n",p1_telegram_runningpos);
			// Add newline
			strcpy(p1_telegram_runningpos+=line_len*sizeof(char), "\n\r");
			p1_telegram_runningpos+=2*sizeof(char);
			//printf_debug(" p1_telegram_running:\n%s\n\n",p1_telegram_running);
		}
		else
		{
			printf_outputerror("P1 Telegram size greater than MAX_SIZE_P1_TELEGRAMS. Currentsize=%d. Skipping telegram", p1_telegram_currentsize);
		}
	} while ( n > 0 );
		
	if ( allslotsfilled == 1 )
	{
		free(p1_telegram_running);
	}
	else
	{
		// push correct semaphore to start processing thread that is waiting
		sem_post(&(current_p1_telegram->semaphore));
	}


	// Reset the SIGIO interrupt
	fcntl(GetComportHandle(comport_device_p1), F_SETFL, oldflag);
}

void process_p1_telegram_thread(void *arg)
{
	p1_telegram_io_comm_struct *current_p1_telegram  = ((p1_telegram_io_comm_struct*) arg);
	char *debugmessage;
	printf_debug("process_p1_telegram_thread %d started\n",current_p1_telegram->instance);
	while (1)
	{
		sem_wait(&(current_p1_telegram->semaphore));
		debugmessage = malloc(1024*sizeof(char));
		debugmessage[0] = 0;
		sprintf(debugmessage,"%s process_p1_telegram_thread %d waking",debugmessage, current_p1_telegram->instance);
		char **decodedp1data = readp1_decode(current_p1_telegram->telegram);
		char *mysql_statement = decodedp1data[0];
		char *json_query = decodedp1data[1];
		if ( mysql_statement == NULL || json_query == NULL )
		{
			sprintf(debugmessage,"%s  !! error decoding p1. result=NULL\n", debugmessage);
			sprintf(debugmessage,"%s  !! telegram:\n%s\n", debugmessage, current_p1_telegram->telegram );

			printf_error("Error decoding p1. result=NULL\n");
			eventlog("Error decoding p1. result=NULL\n");
		}
		else 
		{
			unsigned long mysql_retval = 0;
			if ( (mysql_retval = mysql_write( mysql_statement )) < 0 )
			{
				sprintf(debugmessage," !! error saving data to MySQL!");
				printf_error("Error saving data to MySQL: Statement = %s\n", mysql_statement);
				char *eventtext = malloc((strlen(mysql_statement)+40)*sizeof(char));
				sprintf(eventtext, "Error saving data to MySQL: Statement:\n%s\n", mysql_statement);
				eventlog(eventtext);
				free(eventtext);
			}
			else
			{
				sprintf(debugmessage,"; %lu rows saved to MySQL",mysql_retval);
			}
			int emoncms_retval = 0;
			char *json_query_complete = malloc( sizeof(char*)* (strlen(json_query)+strlen(emoncms_urlbuilder)));
			sprintf(json_query_complete,"%s%s", emoncms_urlbuilder, json_query);
			if ( (emoncms_retval = http_input_emoncms( json_query_complete, emoncms_server, emoncms_port ) ) < 0 )
			{
				sprintf(debugmessage," !! error saving data to Emoncms!");
				printf_error("Error saving data to Emoncms: Statement = %s\n", json_query);
				char *eventtext = malloc((strlen(json_query)+40)*sizeof(char));
				sprintf(eventtext, "Error saving data to Emoncms: JSON:\n%s\n", json_query);
				eventlog(eventtext);
				free(eventtext);
			}
			else
			{
				sprintf(debugmessage,"; data saved to Emoncms");
			}
			sprintf(debugmessage,"\n");
		}
		if ( mysql_statement != NULL ) free(mysql_statement);
		if ( json_query != NULL ) free(json_query);
		if ( decodedp1data != NULL ) free(decodedp1data);
		
		current_p1_telegram->active = 0;
		printf_debug("%s",debugmessage);
		free(debugmessage);
	}
}

void catch_term (int sig)
{
	printf_debug("Term caught!\n");
	eventlog("Shutting down.");
	CloseComport(comport_device_p1);
	exit(0);
}

void do_stuff (void)
{
	int lastresult_p1 = 0;

	printf_debug(".");

	time_t currenttime;
	time(&currenttime);
	
	lastresult_p1 = difftime(currenttime, lastdata_p1);
	if ( lastresult_p1 > maxdelay_p1 )
	{
		if ( 1== OpenComport_P1() )
		{
			catch_term(0);
		}
		// Reset time for last interrupt catch
		time(&lastdata_p1);

	}
	
	sleep(1);
}



int main(int argc, char *argv[])
{
	time_t timer = time(NULL);
	char *stringdatetime = malloc(128*sizeof(char));
	strftime(stringdatetime, 128,  "%F %T", localtime(&timer));
	printf ( "%s: %s (eFJenergy) starting.\n",stringdatetime,argv[0]);
	free(stringdatetime);
	
	int n;

	if (pthread_mutex_init(&mysql_lock, NULL) != 0)
    {
        printf("\nMutex init for MySQL Lock failed\n");
        exit(1);
    }
	
	// Reserve enough room to allow for MAX_NUM_P1_TELEGRAMS of size MAX_SIZE_P1_TELEGRAMS
	// Also start threads for processing
	for ( n=0; n<MAX_NUM_P1_TELEGRAMS; n++ )
	{
		//p1_telegram_io_comm_struct p1_telegrams_io_comm_single;
		//p1_telegrams_io_comm[n] = p1_telegrams_io_comm_single;
		p1_telegrams_io_comm[n].active = 0;
		p1_telegrams_io_comm[n].telegram = malloc(MAX_SIZE_P1_TELEGRAMS*sizeof(char));
		sem_init(&(p1_telegrams_io_comm[n].semaphore), 0, 0);
		p1_telegrams_io_comm[n].instance = n;
		pthread_create(&(p1_telegrams_io_comm[n].thread), NULL, (void *) &process_p1_telegram_thread, (void *) &p1_telegrams_io_comm[n]);
	}
	
	// set lasdata_p1 to current time to start check correclty
	time(&lastdata_p1);
	
	
	#ifndef	NOMYSQL
	mysql_conn = mysql_init ( NULL );
	if ( mysql_conn == NULL)
	{
		printf("Error during init MySQL!\n");
		exit(2);
	}
	
	if (mysql_real_connect (mysql_conn, mysql_server, mysql_username, mysql_password, mysql_database, 0, NULL, 0) == NULL )
	{
		printf("Error during connection to MySQL server!\n");
		mysql_close( mysql_conn );
		exit(3);	
	}
	mysql_close( mysql_conn );
	#endif

	/* Set P1 serial port for interrupt on receiving data */
	sigset_t mask;
	struct sigaction saio;
	saio.sa_handler = get_data_electricity_p1;
	sigemptyset(&saio.sa_mask);
	saio.sa_flags = 0;
	saio.sa_restorer = NULL;
	sigaction(SIGIO, &saio, NULL);
	
	if ( 1 == OpenComport_P1())
	{
		printf("Error during OpenComport P1!\n");
		exit(1);
	}
	

	
	eventlog("Successfully started Serial & MySQL communication.");
			
	/* Establish a handler for signals. */
	signal (SIGTERM, catch_term);
	signal (SIGINT, catch_term);
	
	//fcntl(GetComportHandle(comport_device_p1), F_SETOWN, getpid());
	//fcntl(GetComportHandle(comport_device_p1), F_SETFL, FASYNC);

	/* Check the flag once in a while to see when to quit. */
	while (keep_going)
		/* Wait for input of just wait */
		do_stuff ();

	eventlog("Something went terrible wrong.");
		
	return 0;
}	

void eventlog(char *eventtext)
{
	char *mysql_statement = malloc((strlen(eventtext)+63)*sizeof(char));
	sprintf( mysql_statement, "INSERT INTO EventLog (Program,Event) VALUES ('eFJenergy','%s');", eventtext);
	if (  mysql_write ( mysql_statement) != 0 )
	{
		printf_debug("Error saving EventLog to MySQL!: %s\n",mysql_statement);
	}
	free(mysql_statement);
}

unsigned long mysql_write( char *mysql_statement )
{
	#ifndef	NOMYSQL
	printf_debug("mysql_write: wait for lock\n");
	pthread_mutex_lock(&mysql_lock);
	printf_debug("mysql_write: lock\n");
	mysql_conn = mysql_init ( NULL );
	
	if ( mysql_conn == NULL || mysql_real_connect (mysql_conn, mysql_server, mysql_username, mysql_password, mysql_database, 0, NULL, 0) == NULL )
	{
		printf_outputerror("MySQL query not saved: %s\n",mysql_statement);
		mysql_close( mysql_conn );
		return -3;
	}
	
	unsigned long retval = mysql_query( mysql_conn, mysql_statement);
	if ( retval == 0 )
		retval = mysql_affected_rows (mysql_conn);
	else
		retval = -1;
	mysql_close( mysql_conn );
	pthread_mutex_unlock(&mysql_lock);
	printf_debug("mysql_write: unlocked\n");
	return retval;
	#else
	printf_outputerror("MySQL: %s\n",mysql_statement);
	fflush(stdout);
	return 0;
	#endif
}

int emoncms_json_input( char *json_string )
{
	#ifndef	NOEMONCMS
	
	#else
	printf_outputerror("JSON: %s\n",json_string);
	fflush(stdout);
	return 0;
	#endif
	
}

int OpenComport_P1(void)
{
	int retval=1, n;
	CloseComport(comport_device_p1);
	
	retval = OpenComport(comport_device_p1, comport_baud_p1);
	
	printf_debug("Sending a few newlines to P1 port to wake it up...  ");
	n = SendByte(comport_device_p1, '\n') + SendByte(comport_device_p1, '\r') +
		SendByte(comport_device_p1, '\n') + SendByte(comport_device_p1, '\r') +
		SendByte(comport_device_p1, '\n') + SendByte(comport_device_p1, '\r'); 
	printf_debug ("response: %d ", n);		
	
	fcntl(GetComportHandle(comport_device_p1), F_SETOWN, getpid());
	fcntl(GetComportHandle(comport_device_p1), F_SETFL, FASYNC);
	printf_debug ("- interrupt set.\n");		

}