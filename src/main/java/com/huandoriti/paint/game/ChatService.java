package com.huandoriti.paint.game;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChatService {
    private ArrayList<GiocatoreServer> giocatori;
    private Partita partita;
    private ArrayList<String> chats = new ArrayList<>();

    public ChatService(ArrayList<GiocatoreServer> giocatori, Partita partita) {
        this.giocatori = giocatori;
        this.partita = partita;
    }

    public void receiveText(GiocatoreServer indovinatore, String text) {
        boolean haIndovinato = text.equalsIgnoreCase(partita.getParolaDaIndovinare());
        if (haIndovinato) {
            partita.getVincitori().add(indovinatore);
        }
        for (int i = 0; i < giocatori.size(); i++) {
            GiocatoreServer giocatoreServer = giocatori.get(i);
            if (!indovinatore.equals(giocatoreServer)) {
                try {
                    synchronized (giocatoreServer.getOutputStream()) {
                        if (haIndovinato) {
                            giocatoreServer.getOutputStream().writeObject("*****");
                            giocatoreServer.getOutputStream().flush();
                        } else {
                            giocatoreServer.getOutputStream().writeObject(text);
                            giocatoreServer.getOutputStream().flush();
                        }
                        System.out.println("Chat: Ho inviato ai giocatore" + i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
