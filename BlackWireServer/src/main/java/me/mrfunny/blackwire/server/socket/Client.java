package me.mrfunny.blackwire.server.socket;

import java.net.Socket;

public class Client extends Thread {

    private final Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Connecting new client: " + socket.getInetAddress());
    }
}
