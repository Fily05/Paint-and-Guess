package com.huandoriti.paint.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Giocatore implements Runnable{
    private int id;
    private Partita partita;
    private boolean isDisegnatore;
    private String parolaDaDisegnare;

    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public Giocatore(Socket socket) {
        this.socket = socket;
        System.out.println("Assegno socket");
        try {
            System.out.println("assegno stream");
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("termino stream");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (true) {
            //TODO: eliminare ciclo infinito, ora lasciato per testare evitare server termina
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Partita getPartita() {
        return partita;
    }

    public void setPartita(Partita partita) {
        this.partita = partita;
    }

    public boolean isDisegnatore() {
        return isDisegnatore;
    }

    public void setDisegnatore(boolean disegnatore) {
        isDisegnatore = disegnatore;
    }

    public String getParolaDaDisegnare() {
        return parolaDaDisegnare;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(ObjectInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setParolaDaDisegnare(String parolaDaDisegnare) {
        this.parolaDaDisegnare = parolaDaDisegnare;
    }
}
