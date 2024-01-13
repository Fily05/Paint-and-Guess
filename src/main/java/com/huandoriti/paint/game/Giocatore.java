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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Giocatore giocatore = (Giocatore) o;

        return id == giocatore.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
