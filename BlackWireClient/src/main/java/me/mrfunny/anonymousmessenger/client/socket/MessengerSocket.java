package me.mrfunny.anonymousmessenger.client.socket;

import me.mrfunny.anonymousmessenger.client.command.CommandInfo;
import me.mrfunny.anonymousmessenger.client.command.CommandManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MessengerSocket {
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final CommandManager commandManager = new CommandManager();

    public MessengerSocket(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        commandManager.registerCommand(new CommandInfo("/register", "Registers you in database"), () -> {

        });
        while(true){
            System.out.print("> ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input = reader.readLine();
            if(input.equalsIgnoreCase("/stop")){
                System.out.println(sendMessage("/stop"));
                in.close();
                out.close();
                clientSocket.close();
                System.exit(0);
            }
            if(!commandManager.parseInput(input)){
                String response = sendMessage(input);
                if(!response.trim().intern().equals("")){

                }
            }
        }
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public String sendMessage(String msg) throws IOException  {
        out.println(msg);
        return in.readLine();
    }

    public void stopConnection() throws IOException  {
        in.close();
        out.close();
        clientSocket.close();
    }
}
