package com.huandoriti.paint.game;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;

public class Partita implements Runnable{
    private ArrayList<Giocatore> giocatori = new ArrayList<>();
    private ArrayList<Thread> threads = new ArrayList<>();
    private String[] paroleDaIndovinare = {"cane", "gatto", "libro", "pollo"};
    private boolean started;

    private int turn;

    public Partita() {
    }

    @Override
    public void run() {
        iniziaPartita();
    }

    public void iniziaPartita(){
        System.out.println("partita inizia");
        started = true;
        giocatori.get(0).setDisegnatore(true);
        giocatori.get(0).setParolaDaDisegnare(paroleDaIndovinare[new Random().nextInt(paroleDaIndovinare.length)]);
        try {
            System.out.println("Server invia ruoli");
            giocatori.get(0).getOutputStream().writeObject(Ruolo.DISEGNATORE);
            giocatori.get(0).getOutputStream().writeObject(giocatori.get(0).getParolaDaDisegnare());
            giocatori.get(0).getOutputStream().writeObject("0");
            giocatori.get(0).setId(0);
            new Thread(giocatori.get(0)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: continua
        for (int i = 1; i < giocatori.size(); i++) {
            Giocatore giocatore = giocatori.get(i);
            giocatore.setId(i);
            try {
                giocatore.getOutputStream().writeObject(Ruolo.INDOVINATORE);
                giocatore.getOutputStream().writeObject(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
            threads.add(new Thread(giocatore));
            threads.get(i).start();
        }
    }


    /**
     * Metodo da richiamare quando inizia un giocatore che deve attendere
     * il gioco che inizia, ovvero quando il disegnatore può iniziare. Si mette
     * in pausa fino quando
     * @param giocatore
     */
    public void startGiocatore(Giocatore giocatore) {
        System.out.println("Giocatore " + giocatore.getId() + " park");
        LockSupport.park();
    }

    public void draw(Giocatore giocatore) {
        if (turn != giocatore.getId()) {
            return;
        }

    }

    public void aggGiocatore(Giocatore giocatore) {
        System.out.println("Aggiungi giocatore");
        giocatori.add(giocatore);
        System.out.println("Aggiunto");
    }
}
