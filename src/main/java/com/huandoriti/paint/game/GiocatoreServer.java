package com.huandoriti.paint.game;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.io.ObjectOutput;
import java.net.Socket;

public class GiocatoreServer extends Giocatore{
    private String nomeGiocatore;
    private Partita partita;
    public GiocatoreServer(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        while (true) {
            if (isDisegnatore()) {
                //Disegnatore non deve terminare in quanto altrimenti muore thread
                continue;
            }
            try {
                Object o;
                    System.out.println("Giocatore server ha letto");
                    o = this.getInputStream().readObject();
                    if (o instanceof String s) {
                        this.getPartita().getChatService().receiveText(this, s);
                    } else {
                        System.out.println("Da indovinatore ho ricevuto qualcosa di storto");
                    }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }
    public Partita getPartita() {
        return partita;
    }

    public void setPartita(Partita partita) {
        this.partita = partita;
    }
    public String getNomeGiocatore() {
        return nomeGiocatore;
    }

    public void setNomeGiocatore(String nomeGiocatore) {
        this.nomeGiocatore = nomeGiocatore;
    }
}
