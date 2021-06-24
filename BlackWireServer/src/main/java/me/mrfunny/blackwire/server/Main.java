package me.mrfunny.blackwire.server;

import me.mrfunny.blackwire.server.socket.Client;

import java.io.Console;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashSet;

public class Main {
    public static final HashSet<Client> connectedClients = new HashSet<>();
    public static void main(String[] args) throws Exception {
        System.out.println("Starting BlackWire server...");
        Console console = System.console();
        System.out.println("Enter port host:port of server to bind");
        String[] hostData = console.readLine().split(":");
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(InetAddress.getByName(hostData[0]), Integer.parseInt(hostData[1])));
        System.out.println("Started BlackWire server on " + hostData[0] + ":" + hostData[1]);
        while(true){
            try {
                Client client = new Client(serverSocket.accept());
                client.start();
                connectedClients.add(client);
            } catch (InterruptedException e){
                e.printStackTrace();
                break;
            }
        }
    }
}
