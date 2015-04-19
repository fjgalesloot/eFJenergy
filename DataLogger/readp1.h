#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

double 		readp1_double_withunit( char *line );
int 		readp1_int_withunit( char *line );
int 		readp1_int( char *line );
struct tm 	readp1_date( char *line );

char *readp1_findlineforcode( char *p1code, char *p1_telegram, int *curposition, int maxsize );
char **readp1_decode( char *p1_telegram );
char *readline( char* buffer, int *position, int maxsize );