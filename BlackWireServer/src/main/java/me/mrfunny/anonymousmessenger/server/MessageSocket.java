package me.mrfunny.anonymousmessenger.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class MessageSocket {
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    public MessageSocket(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new EchoClientHandler(serverSocket.accept()).start();
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    private static class EchoClientHandler extends Thread {
        private final Socket clientSocket;

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
            setDaemon(true);
        }

        @Override
        public void run() {
            try{
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if ("/stop".equals(inputLine)) {
                        out.println("Bye");
                        in.close();
                        out.close();
                        clientSocket.close();
                        break;
                    }
                    System.out.println(inputLine);
                    out.println(inputLine);
                }
                in.close();
                out.close();
            } catch (SocketException socketException){
                if(!socketException.getMessage().contains("Connection reset")){
                    socketException.printStackTrace();
                }
            } catch (IOException exception){
                exception.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
