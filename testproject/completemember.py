
import socket

s = socket.create_connection(("localhost", 9081))

msg = "{"
msg += "}"

print "sending: ", msg
s.sendall(msg)
s.sendall("\nENDREQUEST\n")

data = ""
while 1:
    print "receiving..."
    tmp = s.recv(1024)
    print "got data:", tmp
    if not tmp:
        break
    data += tmp
print "FINISHED:", data


