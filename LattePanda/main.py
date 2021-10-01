import bluetooth
import datetime


import time
import threading

global SendList
global Sending

rowList = []
SendList = []
Sending = ""

server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
server_sock.bind(('', bluetooth.PORT_ANY))
server_sock.listen(1)

uuid = 'ef7ce24a-a1eb-45d4-9208-f896b0ae8336'
bluetooth.advertise_service(
    server_sock, "MyServer", service_id=uuid,
    service_classes=[ uuid, bluetooth.SERIAL_PORT_CLASS ],
    profiles=[ bluetooth.SERIAL_PORT_PROFILE ],
)

def sendBluetooth():
    global rowList
    global SendList
    global Sending

    while True:
        client_sock, client_addrport = server_sock.accept()

        dt_now = datetime.datetime.now()
        data = client_sock.recv(1024)
        print(data) # bytes
        print(data.decode('ascii'))

        rowList = []
        SendList = []
        Sending = ""
        time.sleep(2)
        Sending = ",".join(SendList)

        if len(Sending) == 0:
            Sending = "null"
        print(str(dt_now))
        print(str(dt_now) + ": Sending → "+str(Sending))
        try:
            client_sock.send(Sending)
        except:
            print("send error")
            client_sock, client_addrport = server_sock.accept()
        else:
            pass
        rowList = []
        SendList = []
        Sending = ""

Thread1 = threading.Thread(target=sendBluetooth)
Thread1.start()

while True:
    dt_now = datetime.datetime.now()
    print(str(dt_now) + ": input please")
    rowList.append(input())
    SendList = list(set(rowList))
