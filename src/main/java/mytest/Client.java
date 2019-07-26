package mytest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client {
    private DatagramSocket socket;

    public Client() throws SocketException {
        socket = new DatagramSocket();
    }

    public void run() throws IOException {
        InetAddress ip = InetAddress.getByName("11.111.111.111");
        String message = "b";
        byte[] buffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, ip, 12345);
        socket.send(sendPacket);
    }

    public static void main(String[] args) throws IOException {
        new Client().run();
}
}
