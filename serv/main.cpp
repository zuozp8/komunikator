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
#include <set>

#define SERVER_PORT 4790
#define QUEUE_SIZE 10
typedef int socketlen_t;
using namespace std;
//Identyficator of program used in diagnostic output
string progname;

//User Id => his password
vector<string> pass;

//User Id => messages (whole packets) to be sent
map<int, queue<string> > bufferedMessages;

//User Id => file descriptor of socket to client
map<int, int> connections;

//file descriptors of users that are not logged in yet
set<int> newConnections;

//file descriptor => data about to write
map<int,string> writeBuffor;
//file descriptor => readed data
map<int,string> readBuffor;

//Socket we are listening on
int nSocket;

/**
 * Terminates everything
 */
void die(int signo) {
	cerr<<progname<<": Dying...\n";
	for (auto i=connections.begin(); i!=connections.end(); i++) {
		close(i->second);
	}
	for (auto i=newConnections.begin(); i!=newConnections.end(); i++) {
		close(*i);
	}
	close(nSocket);
	exit(0);
}

/**
 * Create socket, handle some errors
 */
void makeSocket() {
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
		cerr<<progname<<": Can't create a socket.\n";
		exit(1);
	}
	setsockopt(nSocket, SOL_SOCKET, SO_REUSEADDR, (char*)&nFoo, sizeof(nFoo));

	/* bind a name to a socket */
	nBind = bind(nSocket, (struct sockaddr*)&stAddr, sizeof(struct sockaddr));
	if (nBind < 0)
	{
		cerr<<progname<<": Can't bind a name to a socket.\n";
		exit(1);
	}

	/* specify queue size */
	nListen = listen(nSocket, QUEUE_SIZE);
	if (nListen < 0)
	{
		cerr<<progname<<": Can't set queue size.\n";
		exit(1);
	}
}

/**
 * Login or register
 * @return If user logged in
 */
bool login(int fd) {
	char buf[1600];
	read(fd,buf, 2);//Read length
	int length = *((short*)buf); //First 2 bytes of buf contain the length
	int readed = 0;
	while(readed < length) {
		readed += read(fd,buf, length-readed);
	}
	//FOO
}

/**
 * Communication between server and client
 */
void talkWithClient(int fd) {
	
}

/**
 * @return maximum Fd found in project
 */
int getMaxFd() {
	int maxFd = nSocket;
	for (auto i=connections.begin(); i!=connections.end(); i++) {
		if (maxFd < i->second)
			maxFd = i->second;
	}
	for (auto i=newConnections.begin(); i!=newConnections.end(); i++) {
		if (maxFd < *i)
			maxFd = *i;
	}
	return maxFd;
}

fd_set getRmask() {
	fd_set mask;
	FD_ZERO(&mask);
	FD_SET(nSocket, &mask);
	for (auto i=connections.begin(); i!=connections.end(); i++) {
		FD_SET(i->second,&mask);
	}
	for (auto i=newConnections.begin(); i!=newConnections.end(); i++) {
		FD_SET(*i,&mask);
	}
	return mask;
}

fd_set getWmask() {
	fd_set mask;
	FD_ZERO(&mask);
	for (auto i=writeBuffor.begin(); i!=writeBuffor.end(); i++) {
		FD_SET(i->first,&mask);
	}
	return mask;
}

int main(int argc, char* argv[]) {
	progname = argv[0];
	signal(SIGUSR1, die); //let user kill the server
	makeSocket();

	while(1) {
		fd_set fsRmask = getRmask(), fsWmask=getWmask();
		timeval tTimeout;
		tTimeout.tv_sec = 5;
		tTimeout.tv_usec = 0;
		
		cerr<<"select... "<<flush;
		int nFound = select(getMaxFd()+1, &fsRmask, &fsWmask, NULL, &tTimeout);
		cerr<<nFound<<endl;
		
		if (nFound < 0) {
			cerr<<progname<<": select error.\n";
			exit(1);
		}
		
		if (nFound == 0) {
			continue;
		}
		
		/* New connection */
		if (FD_ISSET(nSocket, &fsRmask)) {
			int nClientSocket;
			sockaddr_in stClientAddr;
			socklen_t nTmp;
			nClientSocket = accept(nSocket, (sockaddr*)&stClientAddr, &nTmp);
			if (nClientSocket < 0) {
				cerr<<progname<<": Can't create a connection's socket.\n";
				exit(1);
			}
			newConnections.insert(nClientSocket);
			cerr<<progname<<": [connection from "<<inet_ntoa((in_addr)stClientAddr.sin_addr)<<"] fd: "<<nClientSocket<<"\n";
		}

		/* writing */
		for (auto i=writeBuffor.begin(); i!=writeBuffor.end();) {
			if (FD_ISSET(i->first,&fsWmask)) {
				int writtenBytes = write(i->first,i->second.c_str(), i->second.length());
				i->second.erase(0,writtenBytes);
				cerr<<progname<<": written "<<writtenBytes<<" to fd "<<i->first
					<<" "<<i->second.length()<<" bytes left\n";
			}
			auto i2 = i;
			i++;
			//Delete from writeBuffer, when there is no bytes left to write
			if (!i->second.length())
				writeBuffor.erase(i2);
		}
		
		/* logins or registrations */
		for (auto i=newConnections.begin(); i!=newConnections.end(); i++) {
			if (FD_ISSET(*i,&fsRmask)) {
				char buf[1600];
				int readedBytes = read(*i,buf, 1600);
				readBuffor[*i].append(buf,readedBytes);
			}
		}
		
		/* logged in users */
		/*if (login(nClientSocket)) {
			talkWithClient(nClientSocket);
		}*/
		
	}

	return(0);
}