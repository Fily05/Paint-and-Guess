package com.huandoriti.paint.game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;

public class Server {
    private ServerSocket serverSocket;

    public Server() throws IOException {
        serverSocket = new ServerSocket(5544);
    }

    public static void main(String[] args) throws IOException{
        new Server().start();
    }

    public void start() {
        while (true) {
            int count = 0;
            Partita partita = new Partita();
            try {
                while (count < 2) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Server accetta");
                    System.out.println(socket);
                    partita.aggGiocatore(new GiocatoreServer(socket));
                    partita.getGiocatori().get(count).setPartita(partita);
                    count++;
                }
                System.out.println("partita run");
                partita.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
