package com.huandoriti.paint.game;

import com.huandoriti.paint.game.canvastransfer.Forma;
import javafx.application.Platform;
import javafx.util.Duration;

import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.security.UnrecoverableEntryException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class Partita implements Runnable{
    private ArrayList<GiocatoreServer> giocatori = new ArrayList<>();
    private ArrayList<GiocatoreServer> vincitori = new ArrayList<>();
    private ArrayList<Thread> threads = new ArrayList<>();
    private String[] paroleDaIndovinare = {"cane", "gatto", "libro", "pollo"};
    private GiocatoreServer disegnatore;
    private String parolaDaIndovinare;
    private ChatService chatService;
    public static final Duration MAX_TEMPO = Duration.minutes(3);
    /**
     * Partita è terminato quando il tempo è scaduto, o tutti hanno indovinato
     */
    private AtomicBoolean isTerminated = new AtomicBoolean(false);
    private int turn;
    private Duration tempoRimasto = Partita.MAX_TEMPO;

    public Partita() {
    }

    @Override
    public void run() {
        iniziaPartita();
        iniziaTimer();
        System.out.println("Inizio chat service");
        chatService = new ChatService(giocatori, this);
        while (true) {
            Object object;
            FutureTask futureTask = new FutureTask(() -> {
                    synchronized (giocatori.get(0).getInputStream()) {
                        System.out.println("leggo canvas");
                        try {
                            Object obj;
                            obj = giocatori.get(0).getInputStream().readObject();
                            System.out.println("Ho letto oggetto");
                            return obj;
                        } catch (IOException | ClassNotFoundException e) {
                            System.out.println("Socket stato chiuso");
                        }
                    }
                    return null;
            });
            Thread readCanvas = new Thread(futureTask);
            readCanvas.start();

            if (isTerminatoPartita()) {
                readCanvas.interrupt();
                terminaPartita();
                System.out.println("Partita terminata");
                return;
            }

            try {
                object = futureTask.get(3000, TimeUnit.MILLISECONDS);
                System.out.println("future task get");
            } catch (Exception e) {
                continue;
            }
            sendCanvasToAll(object);
        }
    }
    //TODO: finisci send canva, c' bug
    private void sendCanvasToAll(Object object) {
        ArrayList<Forma> list;
        if (object instanceof ArrayList<?>) {
            System.out.println("converto canvas");
            list = (ArrayList<Forma>) object;
        } else {
            System.out.println("Non ho ricevurto array da disegnatore");
            return;
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

    public void terminaPartita() {
        PunteggioCalculatore punteggioCalculatore = new PunteggioCalculatore(giocatori, vincitori, disegnatore);
        LinkedHashMap<GiocatoreServer, Integer> punteggiGiocatore = punteggioCalculatore.calcolaPunteggi();
        for (GiocatoreServer giocatoreServer : giocatori) {
            try {
                giocatoreServer.getOutputStream().writeObject(Instruction.FINISH);
                System.out.println("invio punteggio");
                punteggiGiocatore.forEach((g, punteggio) -> {
                    synchronized (g.getOutputStream()) {
                        try {
                            giocatoreServer.getOutputStream().writeObject((g.getNomeGiocatore())
                                    + ": punteggi raggiunti = " + punteggiGiocatore.get(g));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                giocatoreServer.getOutputStream().writeObject(Instruction.DONE);
                giocatoreServer.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void iniziaTimer() {
        ScheduledExecutorService timeService = Executors.newSingleThreadScheduledExecutor();
        timeService.scheduleAtFixedRate(() -> {
            if ((int) tempoRimasto.toSeconds() == 0 || isTerminatoPartita()) {
                System.out.println("Timer: Tempo scaduto o partita terminata");
                isTerminated.set(true);
                timeService.shutdown();
            } else {
                tempoRimasto = tempoRimasto.subtract(Duration.seconds(1));
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void iniziaPartita(){
        System.out.println("partita inizia");
        disegnatore = giocatori.get(0);
        disegnatore.setDisegnatore(true);
        disegnatore.setNomeGiocatore("Disegnatore " + 0);
        String parolaDaIndovinare = paroleDaIndovinare[new Random().nextInt(paroleDaIndovinare.length)];
        disegnatore.setParolaDaDisegnare(parolaDaIndovinare);
        this.parolaDaIndovinare = disegnatore.getParolaDaDisegnare();
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
            //disegnatore non parte
//            threads.get(0).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: continua
        for (int i = 1; i < giocatori.size(); i++) {
            GiocatoreServer giocatore = giocatori.get(i);
            giocatore.setId(i);
            giocatore.setNomeGiocatore("Indovinatore " + i);
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
        return vincitori.size() == giocatori.size() - 1 || isTerminated.get();
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
