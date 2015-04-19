#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <netdb.h>
#include <string.h>

#define USERAGENT "eFJenergy-DataLogger 2.5"

int create_tcp_socket();
char *get_ip(char *host);
char *build_get_query(char *host, char *page);

 