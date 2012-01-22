all: serv
serv: main.o
	g++-4.5 $< -g -Wall -o $@
main.o: main.cpp
	g++-4.5 $< -c -g -Wall -std=c++0x -o $@
clean:
	rm main.o serv
