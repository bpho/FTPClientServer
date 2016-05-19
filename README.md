# File Transfer Protocol (FTP), Client and Server

##Program Details

The role of the FTP client program is to provide a user interface that allows a human user to enter high-level requests and generate
the appropriate FTP protocol commands to accomplish the user's request. It also provides feedback to the user on the validity of user inputs 
and the success or failure of the requets. Both the client and server interoperates over a network using TCP sockets. 

All elements of the FTP protocol along with some stdin/stdout I/O and parts of file I/O is incorporated with socket I/O. 

###FTP Client Program

The FTP Client program takes one command line argument: an initial port number for a "welcoming" socket that the client will use to 
allow the server to make an FTP-data connection. It should accept input requests from a human user using standard input. These include:
* CONNECT
* GET
* QUIT

When a valid CONNECT request is accepted from the user, the client should attempt to establish a TCP
socket connection to the server program which it expects to be running on the host and port specified in
the user’s input. This connection is the FTP-control connection and it should not be closed until the user
enters another valid CONNECT request (which will initiate a new FTP-control connection).

When a valid GET request is accepted from the user, the client should send the two-command sequence 
(PORT, RETR) to the server on the FTP-control connection and process the server’s
reply to each command before proceeding to the next. The PORT command’s parameter should specify
the IP address of the host where the client program is running and the port number specified as a
command line argument to the client. Each time a new PORT command is sent to the server, the port
number should be incremented by 1 (NOTE: the client will be using a different “welcoming” socket and
associated port for each FTP-data connection). Before sending the PORT command to the server, the
client should create a “welcoming” socket specifying the port number used in the PORT command to be
sure that port is ready for the server’s FTP-data connection. If the “welcoming” socket cannot be created,
the client should write “GET failed, FTP-data port not allocated.” to standard output and read the next
user input line. 

After the RETR command has been sent to the server, the client should accept the server’s FTP-data
connection on the “welcoming” socket and then read the bytes for the requested file from the new socket
created for that connection. The client should continue to read data bytes from the server until the Endof-File
(EOF) is indicated when the server closes the FTP-data connection (the client should also close
the FTP-data connection at EOF). Note that the server replies to the RETR command are received on the
FTP-control connection. Only file data is received on the FTP-data connection

A user’s QUIT request should send the FTP QUIT command to the server,
receive the reply shown below, close the FTP-control connection and then exit. 

##FTP Server Program

If establishing the FTP-data connection is successful, the server then reads the bytes from the requested
file and writes them to the client on the FTP-data connection. When all the bytes from the file have been
sent to the client, the server closes the FTP-data connection. Note that the server replies to the RETR
command are sent on the FTP-control connection. Only file data is sent on the FTP-data connection.

The your server program should also output the following to standard output. The server echoes
all commands received from the client, as well as all responses sent to the client. Specifically, when the
server receives a command from the client, it first echoes the command, and then output its reply to the
client.

The server program, like most servers, will conceptually never terminate. It will be terminated by some
external means such typing control-C in the shell.
