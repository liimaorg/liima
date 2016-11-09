import socket, sys

def checkConnection(hostPorts):
    for hostPort in hostPorts:
        host = hostPort.split(":")[0]
        port = hostPort.split(":")[1]
        print("Trying to connect to: " + host + " " + port)
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        result = sock.connect_ex((host, int(port)))
        if result == 0:
            print("Connection to " + host + " " + port + " successful!")
        else:
            raise Exception("Connection to " + host + " " + port + " failed!")

if __name__ == '__main__':
    try:
        sys.argv.pop(0)
        #example arg: host1:port1 host2:port2
        checkConnection(sys.argv)
    except Exception as e:
        raise