#include "readp1.h"

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


double readp1_double_withunit( char *line )
{
	printf_debug(" readp1_double_withunit ");
	line = strchr( line, '(');
	line+=sizeof(char);
	char *endvaluepos = strchr( line, '*');
	*endvaluepos = 0;
	//printf_debug("readp1: readp1_double_withunit line=%s\r\n",line);
	return atof(line);
}

int readp1_int_withunit( char *line )
{
	printf_debug(" readp1_int_withunit ");
	line = strchr( line, '(');
	line+=sizeof(char);
	char *endvaluepos = strchr( line, '*');
	*endvaluepos = 0;
	//printf_debug("readp1: readp1_double_withunit line=%s\r\n",line);
	return atoi(line);
}

int readp1_int( char *line )
{
	printf_debug(" readp1_int ");
	line = strchr( line, '(');
	line+=sizeof(char);
	char *endvaluepos = strchr( line, ')');
	*endvaluepos = 0;
	//printf_debug("readp1: readp1_int line=%s\r\n",line);
	return atoi(line);
}

struct tm readp1_date( char *line )
{
	printf_debug(" readp1_date ");
	printf_debug(" line=%s ",line);
	struct tm time;

	line = strchr( line, '(');
	if ( line != NULL )
	{
		printf_debug(" line!=NULL ");
		line+=sizeof(char);
		char *endvaluepos = strchr( line, ')');
		time.tm_isdst = 0;
		if (*(endvaluepos-sizeof(char))=='S')
			time.tm_isdst=1;
		*(endvaluepos-sizeof(char)) = 0;
		printf_debug(" line=%s ",line);
		char temp[3];
		temp[2] = 0;
		strncpy( temp, line, 2 );
		time.tm_year = 100 + atoi(temp); //Number of years from 1900
		strncpy( temp, line+2*sizeof(char), 2 );
		time.tm_mon = atoi(temp)-1; // 0-11
		strncpy( temp, line+4*sizeof(char), 2 );
		time.tm_mday = atoi(temp);
		strncpy( temp, line+6*sizeof(char), 2 );
		time.tm_hour = atoi(temp);
		strncpy( temp, line+8*sizeof(char), 2 );
		time.tm_min = atoi(temp);
		strncpy( temp, line+10*sizeof(char), 2 );
		time.tm_sec = atoi(temp);	
		mktime( &time );
	}
	return time;
}

char *readp1_findlineforcode( char *p1code, char *p1_telegram, int *curposition, int maxsize )
{
	printf_debug(" readp1_findlineforcode ");
	int len = strlen(p1code);
	char *line;
	do
	{
		line=readline(p1_telegram, curposition, maxsize);
	} while( line != NULL && 0 != strncmp(p1code, line, len));
	printf_debug(" line=%s ",line);
	return line;
}

char **readp1_decode( char *p1_telegram )
{
	printf_debug(" readp1_decode ");
	const char *mysql_statement_base = "INSERT INTO ElectricityP1 (MeasurementTimeStamp,TotalkWhTarif1,TotalkWhTarif2,CurrentTarif,CurrentUsagekWatt,CurrentUsageL1A,CurrentUsageL2A,CurrentUsageL3A,CurrentUsageL1kWatt,CurrentUsageL2kWatt,CurrentUsageL3kWatt) VALUES ('%s',%f,%f,%d,%f,%d,%d,%d,%f,%f,%f);";
	const char *json_base = "time=%s&json={TotalkWhTarif1:%f,TotalkWhTarif2:%f,CurrentTarif:%d,CurrentUsagekWatt:%f,CurrentUsageL1kWatt:%f,CurrentUsageL2kWatt:%f,CurrentUsageL3kWatt:%f}";
	char *mysql_statement = malloc ( 1024*sizeof(char) );
	char *json_string = malloc ( 1024*sizeof(char) );
	int curposition = 0;
	int maxsize = strlen(p1_telegram);
	char *line;
	
	// MeasurementTimeStamp = 0-0:1.0.0
	line = readp1_findlineforcode("0-0:1.0.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	struct tm time = readp1_date(line);
	printf_debug(" finished:readp1_date ");
	char MeasurementTimeStamp[26] = "";
	strftime(MeasurementTimeStamp, 25, "%F %T", &time);
	// TotalkWhTarif1 = 1-0:1.8.1
	line = readp1_findlineforcode("1-0:1.8.1",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	double TotalkWhTarif1 = readp1_double_withunit(line);
	// TotalkWhTarif2 = 1-0:1.8.2
	line = readp1_findlineforcode("1-0:1.8.2",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	double TotalkWhTarif2 = readp1_double_withunit(line);
	// CurrentTarif = 0-0:96.14.0	
	line = readp1_findlineforcode("0-0:96.14.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	int CurrentTarif = readp1_int(line);
	// CurrentUsageWatt = 1-0:1.7.0
	line = readp1_findlineforcode("1-0:1.7.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	double CurrentUsageWatt = readp1_double_withunit(line);
	// CurrentUsageL1A = 1-0:31.7.0
	line = readp1_findlineforcode("1-0:31.7.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	int CurrentUsageL1A = readp1_int_withunit(line);
	// CurrentUsageL2A = 1-0:51.7.0
	line = readp1_findlineforcode("1-0:51.7.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	int CurrentUsageL2A = readp1_int_withunit(line);
	// CurrentUsageL3A = 1-0:71.7.0
	line = readp1_findlineforcode("1-0:71.7.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	int CurrentUsageL3A = readp1_int_withunit(line);
	// CurrentUsageL1Watt = 1-0:21.7.0
	line = readp1_findlineforcode("1-0:21.7.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	double CurrentUsageL1Watt = readp1_double_withunit(line);
	// CurrentUsageL2Watt = 1-0:41.7.0
	line = readp1_findlineforcode("1-0:41.7.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	double CurrentUsageL2Watt = readp1_double_withunit(line);
	// CurrentUsageL3Watt = 1-0:61.7.0
	line = readp1_findlineforcode("1-0:61.7.0",p1_telegram,&curposition,maxsize );
	if ( line == NULL ) return NULL;
	double CurrentUsageL3Watt = readp1_double_withunit(line);
	
	sprintf( mysql_statement, mysql_statement_base, MeasurementTimeStamp,TotalkWhTarif1,TotalkWhTarif2,CurrentTarif,CurrentUsageWatt,CurrentUsageL1A,CurrentUsageL2A,CurrentUsageL3A,CurrentUsageL1Watt,CurrentUsageL2Watt,CurrentUsageL3Watt);
	sprintf( json_string, json_base, MeasurementTimeStamp,TotalkWhTarif1,TotalkWhTarif2,CurrentTarif,CurrentUsageWatt,CurrentUsageL1Watt,CurrentUsageL2Watt,CurrentUsageL3Watt);
	
	printf_debug("readp1:mysql_statement=%s\n\r",mysql_statement);
	printf_debug("readp1:json_string=%s\n\r",json_string);
	
	char **retval;
	retval = malloc( sizeof(char *)*2 );
	retval[0]=mysql_statement;
	retval[1]=json_string;
	
	return retval;
}

char *readline( char* buffer, int *position, int maxsize )
{
	char *retval = buffer+sizeof(char)*(*position);
	int n = *position;
	
	while ( n+2 < maxsize )
	{
		if ((buffer[n] == '\n' && buffer[n+1]=='\r') || (buffer[n] == '\r' && buffer[n+1]=='\n'))
		{
			buffer[n]=0;
			*position=n+2;
			return retval;
		}
		n++;
	}
	return NULL;
}
