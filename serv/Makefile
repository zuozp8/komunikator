all: serv
serv: main.o
	g++ $< -Wall -o $@
main.o: main.cpp
	g++ $< -c -Wall -std=c++0x -o $@