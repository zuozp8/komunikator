all: serv
serv: main.o
	g++ $< -g -Wall -o $@
main.o: main.cpp
	g++ $< -c -g -Wall -std=c++0x -o $@
clean:
	rm main.o serv