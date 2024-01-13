package com.huandoriti.paint.game;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;

public class Partita implements Runnable{
    private ArrayList<GiocatoreServer> giocatori = new ArrayList<>();
    private ArrayList<GiocatoreServer> vincitori = new ArrayList<>();
    private ArrayList<Thread> threads = new ArrayList<>();
    private String[] paroleDaIndovinare = {"cane", "gatto", "libro", "pollo"};
    private String parolaDaIndovinare;
    private ChatService chatService;
    private boolean started;
    private int turn;

    public Partita() {
    }

    @Override
    public void run() {
        iniziaPartita();
        System.out.println("Inizio chat service");
        chatService = new ChatService(giocatori, this);
        while (true) {
            Object object;
            System.out.println("Sincronizzio input stream disegnatore");
            synchronized (giocatori.get(0).getInputStream()) {
                System.out.println("leggo canvas");
                try {
                    object = giocatori.get(0).getInputStream().readObject();
                    System.out.println("Ho letto oggetto");
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
            //TODO: finisci quiiii
            }
            ArrayList<Forma> list;
            if (object instanceof ArrayList<?>) {
                System.out.println("converto canvas");
                list = (ArrayList<Forma>) object;
            } else {
                System.out.println("Non ho ricevurto array da disegnatore");
                continue;
            }
            System.out.println("invio i canvas");
            try {
                for (int i = 1; i < giocatori.size(); i++) {
                    System.out.println("Invio giocatore " + i);
                    System.out.println("Lista: " + list.toString());
                    giocatori.get(i).getOutputStream().writeObject(list);
                    giocatori.get(i).getOutputStream().flush();
                    System.out.println("Ho inviato al giocatore");
                }
            } catch (IOException e) {
                //Se un giocatore si scollega dal gioco
                e.printStackTrace();
            }
        }

    }
    //TODO: finisci send canva, c' bug
    public void sendCanvasToAll() {

    }

    public void iniziaPartita(){
        System.out.println("partita inizia");
        started = true;
        GiocatoreServer disegnatore = giocatori.get(0);
        disegnatore.setDisegnatore(true);
        String parolaDaIndovinare = paroleDaIndovinare[new Random().nextInt(paroleDaIndovinare.length)];
        disegnatore.setParolaDaDisegnare(parolaDaIndovinare);
        try {
            System.out.println("Server invia ruoli");
            synchronized (disegnatore.getOutputStream()) {
                disegnatore.getOutputStream().writeObject(Ruolo.DISEGNATORE);
                disegnatore.getOutputStream().writeObject(disegnatore.getParolaDaDisegnare());
                disegnatore.getOutputStream().writeObject("0");
                disegnatore.getOutputStream().flush();
            }
            disegnatore.setId(0);
            threads.add(new Thread(disegnatore));
            threads.get(0).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: continua
        for (int i = 1; i < giocatori.size(); i++) {
            Giocatore giocatore = giocatori.get(i);
            giocatore.setId(i);
            try {
                synchronized (giocatore.getOutputStream()) {
                    giocatore.getOutputStream().writeObject(Ruolo.INDOVINATORE);
                    giocatore.getOutputStream().writeObject("" + i);
                    giocatore.getOutputStream().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            threads.add(new Thread(giocatore));
            threads.get(i).start();
        }
    }

    public boolean isTerminatoPartita() {
        return vincitori.size() == giocatori.size() - 1;
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



    public void aggGiocatore(GiocatoreServer giocatore) {
        System.out.println("Aggiungi giocatore");
        giocatori.add(giocatore);
        System.out.println("Aggiunto");
    }

    public ArrayList<GiocatoreServer> getGiocatori() {
        return giocatori;
    }

    public void setGiocatori(ArrayList<GiocatoreServer> giocatori) {
        this.giocatori = giocatori;
    }

    public ChatService getChatService() {
        return chatService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public String getParolaDaIndovinare() {
        return parolaDaIndovinare;
    }

    public void setParolaDaIndovinare(String parolaDaIndovinare) {
        this.parolaDaIndovinare = parolaDaIndovinare;
    }

    public ArrayList<GiocatoreServer> getVincitori() {
        return vincitori;
    }

    public void setVincitori(ArrayList<GiocatoreServer> vincitori) {
        this.vincitori = vincitori;
    }
}
