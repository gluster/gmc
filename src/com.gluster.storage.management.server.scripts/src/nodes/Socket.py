import socket
import sys
import Globals

def openServerSocket(bindAddress="", port=Globals.SERVER_AGENT_PORT):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((bindAddress, port))
    sock.listen(1)
    return sock


def connectToServer(serverName, port=Globals.SERVER_AGENT_PORT):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((serverName, port))
    print "__DEBUG__ connected to ", serverName, " on port ", port
    inputStream = sock.makefile("r")
    outputStream = sock.makefile("w")
    return sock, inputStream, outputStream


def acceptClient(serverSocket):
    clientSocket, clientAddress = serverSocket.accept()
    clientInputStream = clientSocket.makefile("r")
    clientOutputStream = clientSocket.makefile("w")
    return clientSocket, clientAddress, clientInputStream, clientOutputStream


def readPacket(inputStream):
    packetString = ""
    while True:
        line = inputStream.readline()
        print "__DEBUG__", line
        if not line:
            break
        if line.strip() == "":
            # end of input received
            return packetString
        packetString += line
    return packetString


def writePacket(outputStream, packetString):
    rv = outputStream.write(packetString.strip() + "\n\n")
    outputStream.flush()


