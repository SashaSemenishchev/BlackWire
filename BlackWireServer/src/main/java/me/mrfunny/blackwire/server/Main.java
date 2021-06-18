package me.mrfunny.blackwire.server;

import me.mrfunny.blackwire.server.socket.Client;

import java.io.Console;
import java.util.HashSet;

public class Main {
    private final HashSet<Client> connectedClients = new HashSet<>();
    public static void main(String[] args) {
        Console console = System.console();

    }
}
