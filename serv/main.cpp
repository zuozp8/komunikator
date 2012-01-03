#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <signal.h>
#include <iostream>
#include <cstdlib>
#include <cstring>
#include <cstdio>
#include <vector>
#include <queue>
#include <map>

#define SERVER_PORT 4790
#define QUEUE_SIZE 10
typedef int socketlen_t;
using namespace std;

//User Id => his password
vector<string> pass;

//User Id => messages (whole packets) to be sent
map<int, queue<string> > bufferedMessages;

//User Id => file descriptor of socket to client
map<int, int> connections;

/**
 * Function that is executed when client dies
 * Removes zombies form system
 */
void childend(int signo) {
	pid_t pid;
	pid = wait(NULL);
	cerr<<"\t[end of child process number "<<pid<<"]\n";
}

/**
 * Terminates everything
 */
void die(int signo) {
	cerr<<"Dying...\n";
	exit(0);
}

/**
 * Create socket, handle some error
 * @return File descriptor referring to created socket
 */
int makeSocket() {
	int nSocket;
	int nBind, nListen;
	int nFoo = 1;
	sockaddr_in stAddr;
	/* address structure */
	memset(&stAddr, 0, sizeof(sockaddr));
	stAddr.sin_family = AF_INET;
	stAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	stAddr.sin_port = htons(SERVER_PORT);
	
	/* create a socket */
	nSocket = socket(AF_INET, SOCK_STREAM, 0);
	if (nSocket < 0)
	{
		cerr<<": Can't create a socket.\n";
		exit(1);
	}
	setsockopt(nSocket, SOL_SOCKET, SO_REUSEADDR, (char*)&nFoo, sizeof(nFoo));

	/* bind a name to a socket */
	nBind = bind(nSocket, (struct sockaddr*)&stAddr, sizeof(struct sockaddr));
	if (nBind < 0)
	{
		cerr<<": Can't bind a name to a socket.\n";
		exit(1);
	}

	/* specify queue size */
	nListen = listen(nSocket, QUEUE_SIZE);
	if (nListen < 0)
	{
		cerr<<": Can't set queue size.\n";
	}
	return nSocket;
}

/**
 * Login or register
 * @return If user logged in
 */
bool login(int fd) {
	char buf[1600];
	read(fd,*buf, 2);//Read length
	int length = *(short*(buf)); //First 2 bytes of buf contain the length
	int readed = 0;
	while(readed < length) {
		readed += read(fd,*buf, length-readed);
	}
	//FOO
}
/**
 * Communication between server and client
 */
void talkWithClient(int fd) {
	
}

int main(int argc, char* argv[]) {

	signal(SIGCHLD, childend); //manage zombies
	signal(SIGUSR1, die); //manage zombies
	
	int nSocket = makeSocket();

	while(1) {
		int nClientSocket;
		socklen_t nTmp;
		sockaddr_in stClientAddr;

		/* block for connection request */
		nTmp = sizeof(sockaddr);
		nClientSocket = accept(nSocket, (sockaddr*)&stClientAddr, &nTmp);
		if (nClientSocket < 0) {
			cerr<<argv[0]<<": Can't create a connection's socket.\n";
			exit(1);
		}

		/* connection */
		if (! fork()) {
			//We're child
			cerr<<argv[0]<<": [connection from "<<inet_ntoa((in_addr)stClientAddr.sin_addr)<<"]\n";
			if (login(nClientSocket)) {
				talkWithClient(nClientSocket);
			}
			exit(0);
		}
	}

	close(nSocket);
	return(0);
}