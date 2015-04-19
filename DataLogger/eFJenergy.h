#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>
#include <time.h>
#include <semaphore.h>

#include <stdarg.h>

#include "rs232.h"
#include "procstat.h"
#include "readp1.h"
#include <mysql/mysql.h>


void get_data_electricity_p1(int);
void process_p1_telegram_thread(void *arg);

void do_stuff (void );
void catch_term (int );
void eventlog(char *e);
unsigned long mysql_write( char *);


typedef struct p1_telegram_io_comm_struct
{
	short active;
	char *telegram;
	sem_t semaphore;
	pthread_t thread;
	int instance;
}p1_telegram_io_comm_struct;