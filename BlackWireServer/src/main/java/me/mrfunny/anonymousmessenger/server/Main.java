package me.mrfunny.anonymousmessenger.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new MessageSocket(6666);
    }
}
