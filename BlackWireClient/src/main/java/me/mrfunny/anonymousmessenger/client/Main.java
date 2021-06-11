package me.mrfunny.anonymousmessenger.client;

import me.mrfunny.anonymousmessenger.client.socket.MessengerSocket;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in); TODO: uncomment
//        String[] loginData = scanner.next().split(":");
//        scanner.close();

        try {
            MessengerSocket socket = new MessengerSocket("localhost", 6666);
            socket.stopConnection();
        } catch (IOException exception) {
            if(exception.getMessage().toLowerCase().contains("connection reset")){
                System.out.println("Connection with server lost.");
                System.exit(0);
            } else if(exception.getMessage().toLowerCase().contains("connection refused")){
                System.out.println("Couldn't connect to server with ip: ");
                System.exit(0);
            }
            else {
                exception.printStackTrace();
            }
        }
    }
}
