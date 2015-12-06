#include "emoncms.h"

//#define DEBUG
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


int http_input_emoncms (  char *json_string, char* host, unsigned int port )
{
	struct sockaddr_in *remote;
	int sock;
	int tmpres;
	char *ip;
	char *get;
	char buf[BUFSIZ+1];
	int retval=0;
	
	sock = create_tcp_socket();
	ip = get_ip(host);

	printf_debug("IP is %s\n", ip);

	remote = (struct sockaddr_in *)malloc(sizeof(struct sockaddr_in *));
	remote->sin_family = AF_INET;
	tmpres = inet_pton(AF_INET, ip, (void *)(&(remote->sin_addr.s_addr)));

	if( tmpres < 0)  
	{
		printf_error("Can't set remote->sin_addr.s_addr");
		retval = -1;
	}
	else 
	{
		if(tmpres == 0)
		{
			printf_error("%s is not a valid IP address\n", ip);
			retval = -2;
		}
	}
	remote->sin_port = htons(port);

	if(connect(sock, (struct sockaddr *)remote, sizeof(struct sockaddr)) < 0)
	{
		printf_error("Could not connect");
		retval = -3;
	}
	get = build_get_query(host, json_string);
	printf_debug("Query is:\n<<START>>\n%s<<END>>\n", get);

	//Send the query to the server
	int sent = 0;
	while(sent < strlen(get))
	{
		tmpres = send(sock, get+sent, strlen(get)-sent, 0);
		if(tmpres == -1)
		{
			printf_error("Can't send query");
			retval = -4;
		}
		sent += tmpres;
	}
	//now it is time to receive the page
	memset(buf, 0, sizeof(buf));
	char *json_response;
	if ( retval == 0 )
	{
		if ( (tmpres = recv(sock, buf, BUFSIZ, 0)) > 0 )
		{
			json_response = buf;
			if(tmpres >= 2 && strncmp(json_response +  sizeof(char)*(tmpres-2), "ok",2)==0)
			//if(tmpres >= 2 && strncmp("ok", json_response,2) != 0)
			{
				retval = 0;
			}
			else
			{
				//other return than "ok"
				printf_error("JSON return NON-OK: %s\n",json_response);
				retval = -5;
			}
		}
		else
		{
			printf_error("Error receiving data");
		}
	}	
	free(get);
	free(remote);
	free(ip);
	close(sock);
	return retval;
}

char *build_get_query(char *host, char *page)
{
  char *query;
  char *getpage = page;
  char *tpl = "GET /%s HTTP/1.0\r\nHost: %s\r\nUser-Agent: %s\r\n\r\n";
  if(getpage[0] == '/'){
    getpage = getpage + 1;
    printf_error("Removing leading \"/\", converting %s to %s\n", page, getpage);
  }
  // -5 is to consider the %s %s %s in tpl and the ending \0
  query = (char *)malloc(strlen(host)+strlen(getpage)+strlen(USERAGENT)+strlen(tpl)-5);
  sprintf(query, tpl, getpage, host, USERAGENT);
  return query;
}

int create_tcp_socket()
{
  int sock;
  if((sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0){
    printf_error("Can't create TCP socket");
    exit(1);
  }
  return sock;
}
 
 
char *get_ip(char *host)
{
  struct hostent *hent;
  int iplen = 15; //XXX.XXX.XXX.XXX
  char *ip = (char *)malloc(iplen+1);
  memset(ip, 0, iplen+1);
  if((hent = gethostbyname(host)) == NULL)
  {
    printf_error("Can't get IP");
    exit(1);
  }
  if(inet_ntop(AF_INET, (void *)hent->h_addr_list[0], ip, iplen) == NULL)
  {
    printf_error("Can't resolve host");
    exit(1);
  }
  return ip;
}
