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
#include <algorithm>

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
	for (map<int,int>::iterator i=connections.begin(); i!=connections.end(); i++) {
		close(i->second);
	}
	for (set<int>::iterator i=newConnections.begin(); i!=newConnections.end(); i++) {
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
 */
void login(int fd, string data) {
	cerr<<progname<<": login(fd:"<<fd<<")\n";
	if (data.length() < 1) {
		cerr<<progname<<":\t no code\n";
		close(fd);
		readBuffor[fd].clear();
		return;
	}
	char code = data[0];
	data.erase(0,1);
	switch (code) {
	case 1: { //register
		cerr<<progname<<":\t register \n";
		if (data.length() < 3) {//Zbyt krótkie hasło
			cerr<<progname<<":\t\t password to short \n";
			close(fd);
			readBuffor[fd].clear();
		} else if (pass.size() >= 1<<15) {
			cerr<<progname<<":\t\t too many users \n";
			close(fd);
			readBuffor[fd].clear();
		} else{
			//Create user
			pass.push_back(data);
			int newId= pass.size()-1;
			
			cerr<<progname<<":\t\t success, new ID: "<<newId<<"\n";
			
			writeBuffor[fd].push_back(char(3));//Size of message
			writeBuffor[fd].push_back(char(0));//Size of message
			writeBuffor[fd].push_back(char(1));//CODE
			writeBuffor[fd].push_back(char(newId));
			writeBuffor[fd].push_back(char(newId/256));
		}
	} break;
	case 2: { //login
		cerr<<progname<<":\t login \n";
		if(data.length() < 3) {
		}
		uint16_t id = unsigned(data[0])+unsigned(data[1])*256;
		if (pass.size()>id && string(data,2)==pass.at(id)) {
			writeBuffor[fd].push_back(char(2));//Size of message
			writeBuffor[fd].push_back(char(0));
			writeBuffor[fd].push_back(char(2));//CODE
			writeBuffor[fd].push_back(char(1));
			connections[id] = fd;
		} else {
			close(fd);
			readBuffor[fd].clear();
		}
	} break;
	default:
		cerr<<progname<<":\t wrong code \n";
		close(fd);
		readBuffor[fd].clear();
	}
}

/**
 * Communication between server and logged in user
 */
void talkWithClient(int id, string data) {
	int fd = connections[id];
	cerr<<progname<<": talkWithClient(id:"<<id<<")\n";
	try {
		if (data.length() < 1) {
			throw "no code";
		}
		char code = data[0];
		data.erase(0,1);
		switch (code) {
		case 3: { //check friend's status
			cerr<<progname<<":\t check friend's status\n";
			if (data.length()%2 ==1 || data.length()>2000) {
				throw "wrong data size";
			}
			string response;
			int responseSize = 1+3*data.length()/2;
			response.push_back(responseSize);
			response.push_back(responseSize/256);
			response.push_back(3);
			for (uint16_t i=0; i<data.length(); i+=2) {
				response.push_back(data[i]);
				response.push_back(data[i+1]);
				int checkedId=unsigned(data[i+1])*256+unsigned(data[i]);
				char status = connections.count(checkedId);
				response.push_back(status);
			}
			writeBuffor[fd].append(response);
		} break;
		case 5: { //write message
			cerr<<progname<<":\t write message\n";
			if (data.length()<2) {
				throw "wrong data";
			}
			unsigned recieverId = unsigned(data[0])+unsigned(data[1])*256;
			data.erase(0,2);
			cerr<<progname<<" to "<<recieverId;
			if (recieverId >= pass.size()) {
				throw "wrong reciever";
			}
			string response;
			response.push_back((data.size()+3));
			response.push_back((data.size()+3)/256);
			response.push_back(5);//CODE
			response.push_back(id);
			response.push_back(id/256);
			response.append(data);
			bufferedMessages[recieverId].push(response);
		} break;
		default:
			throw "wrong code";
		}
	} catch (char* error){
		cerr<<progname<<":\t\t "<<error<<"\n";
		close(fd);
		readBuffor[fd].clear();
		writeBuffor.erase(fd);
		connections.erase(id);
	}
}

/**
 * @return maximum Fd found in project
 */
int getMaxFd() {
	int maxFd = nSocket;
	for (map<int,int>::iterator i=connections.begin(); i!=connections.end(); i++) {
		if (maxFd < i->second)
			maxFd = i->second;
	}
	for (set<int>::iterator i=newConnections.begin(); i!=newConnections.end(); i++) {
		if (maxFd < *i)
			maxFd = *i;
	}
	for (map<int,string>::iterator i=writeBuffor.begin(); i!=writeBuffor.end(); i++) {
		if (maxFd < i->first)
			maxFd = i->first;
	}
	return maxFd;
}

fd_set getRmask() {
	fd_set mask;
	FD_ZERO(&mask);
	FD_SET(nSocket, &mask);
	for (map<int,int>::iterator i=connections.begin(); i!=connections.end(); i++) {
		FD_SET(i->second,&mask);
	}
	for (set<int>::iterator i=newConnections.begin(); i!=newConnections.end(); i++) {
		FD_SET(*i,&mask);
	}
	return mask;
}

fd_set getWmask() {
	fd_set mask;
	FD_ZERO(&mask);
	for (map<int,string>::iterator i=writeBuffor.begin(); i!=writeBuffor.end(); i++) {
		FD_SET(i->first,&mask);
	}
	return mask;
}

int main(int argc, char* argv[]) {
	progname = argv[0];
	pass.push_back("");//Avoid having user nr 0
	if (progname.find_last_of('/') != string::npos) {
		progname.erase(0,progname.find_last_of('/')+1);
	}
	signal(SIGUSR1, die); //let user kill the server
	makeSocket();

	while(1) {
		fd_set fsRmask = getRmask(), fsWmask=getWmask();
		timeval tTimeout;
		tTimeout.tv_sec = 5;
		tTimeout.tv_usec = 0;
		
		cerr<<progname<<": New connections: "<<newConnections.size()<<" Logged in: "<<connections.size()<<endl;
		cerr<<progname<<": select... "<<flush;
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
			socklen_t nTmp = sizeof(sockaddr);
			nClientSocket = accept(nSocket, (sockaddr*)&stClientAddr, &nTmp);
			if (nClientSocket < 0) {
				cerr<<progname<<": Can't create a connection's socket. ("<<errno<<")\n";
				exit(1);
			}
			newConnections.insert(nClientSocket);
			cerr<<progname<<": [connection from "<<inet_ntoa((in_addr)stClientAddr.sin_addr)<<"] fd: "<<nClientSocket<<"\n";
		}

		/* writing */
		for (map<int,string>::iterator i=writeBuffor.begin(); i!=writeBuffor.end();) {
			map<int,string>::iterator current = i++;
			int fd = current->first;
			if (FD_ISSET(fd,&fsWmask)) {
				int writtenBytes = write(fd,current->second.c_str(), current->second.length());
				// TODO zamykanie połączeń
				current->second.erase(0,writtenBytes);
				cerr<<progname<<": written "<<writtenBytes<<" to fd "<<fd
					<<", "<<current->second.length()<<" bytes left\n";
			}
			//Delete from writeBuffer, when there is no bytes left to write
			if (!current->second.length()) {
				writeBuffor.erase(current);
				bool isThereLoggedInConnection = false;
				for_each(connections.begin(), connections.end(), [&](pair<int,int> x){ if (x.second == fd) isThereLoggedInConnection=true;} );
				if (newConnections.count(fd)==0 && !isThereLoggedInConnection)
					close(fd);
			}
		}
		
		/* logged in users */
		for (map<int,int>::iterator i=connections.begin(); i!=connections.end();) {
			map<int,int>::iterator current = i++;
			int fd = current->second;
			int id = current->first;
			if (FD_ISSET(fd,&fsRmask)) {
				char buf[1600];
				int readedBytes = read(fd,buf, 1600);
				if (readedBytes == 0) { //connection gets closed
					cerr<<progname<<": connection closed fd: "<<fd<<" id: "<<id<<endl;
					readBuffor.erase(fd);
					close(fd);
					connections.erase(current);
					continue;
				}
				readBuffor[fd].append(buf,readedBytes);
				if (readBuffor[fd].length()<2)
					continue;
				uint16_t messageLength = unsigned(readBuffor[fd][1])*256 + unsigned(readBuffor[fd][0]);
				cerr<<progname<<": readed "<<readedBytes<<" from fd "<<fd<<"(id:"<<id<<")"
					<<", "<<readBuffor[fd].length()<<" in buffor, "<<messageLength<<" message length\n";
				if (readBuffor[fd].length() >= messageLength+2u) {
					if (messageLength == 0) {
						writeBuffor.erase(fd);
						readBuffor.erase(fd);
						close(fd);
						connections.erase(id);
						continue;
					}
					string data(readBuffor[fd],2,messageLength);
					readBuffor[fd].erase(0,messageLength+2);
					talkWithClient(id,data);
				}
				if (!readBuffor[fd].length()) {
					readBuffor.erase(fd);
				}
			}
		}
		
		/* logins or registrations */
		for (set<int>::iterator i=newConnections.begin(); i!=newConnections.end();) {
			auto current = i++;
			int fd = *current;
			if (FD_ISSET(fd,&fsRmask)) {
				char buf[1600];
				int readedBytes = read(fd,buf, 1600);
				if (readedBytes == 0) { //connection gets closed
					cerr<<progname<<": connection closed fd: "<<fd<<endl;
					readBuffor.erase(fd);
					close(fd);
					newConnections.erase(current);
					continue;
				}
				readBuffor[fd].append(buf,readedBytes);
				if (readBuffor[fd].length()<2)
					continue;
				uint16_t messageLength = unsigned(readBuffor[fd][1])*256 + unsigned(readBuffor[fd][0]);
				cerr<<progname<<": readed "<<readedBytes<<" from fd "<<fd
					<<", "<<readBuffor[fd].length()<<" in buffor, "<<messageLength<<" message length\n";
				if (readBuffor[fd].length() >= messageLength+2u) {
					if (messageLength == 0) {
						readBuffor.erase(fd);
						close(fd);
						newConnections.erase(current);
						continue;
					}
					string data(readBuffor[fd],2,messageLength);
					readBuffor[fd].erase(0,messageLength+2);
					login(fd,data);
					newConnections.erase(fd);
				}
				if (!readBuffor[fd].length()) {
					readBuffor.erase(fd);
				}
			}
		}
		
		/* move messages to write buffer */
		for (map<int,int>::iterator i=connections.begin(); i!=connections.end(); i++) {
			if (!writeBuffor.count(i->second) && bufferedMessages.count(i->first)) {
				writeBuffor[i->second] = bufferedMessages[i->first].front();
				bufferedMessages[i->first].pop();
				if (bufferedMessages[i->first].empty())
					bufferedMessages.erase(i->first);
			}
		}
	}

	return(0);
}