package com.huandoriti.paint.game;

import javax.sound.midi.Soundbank;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutput;
import java.net.Socket;
import java.net.SocketException;

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
                //Disegnatore non partecipa alla chat
                //Disegnatore non deve terminare altrimenti muore thread
                continue;
            }
            try {
                Object o;
                System.out.println("Giocatore server ha letto");
                o = this.getInputStream().readObject();
                if (o instanceof String s) {
                    boolean hoIndovinato = this.getPartita().getChatService().receiveText(this, s);
                    if (hoIndovinato) return;
                } else {
                    System.out.println("Da indovinatore ho ricevuto qualcosa di storto");
                }

            } catch (SocketException | EOFException exception) {
                System.out.println("Giocatore cannot read and terminate");
                return;
            } catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
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
