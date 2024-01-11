package com.huandoriti.paint;

import com.huandoriti.paint.game.Giocatore;
import com.huandoriti.paint.game.Ruolo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class IniziaController {
    private Giocatore giocatore;
    private ClientPainterApplication application;
    @FXML
    private Button inizia;

    public void onStart() {
        try {
            caricaGiocare();
            application.loadGame("home.fxml", "PAINT AND GUESS", giocatore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void caricaGiocare() {
        try {
            System.out.println("ciaooo");
            giocatore = new Giocatore(new Socket("localhost", 5544));
            System.out.println("bbbbbbb");
            if (giocatore.getInputStream().readObject() instanceof Ruolo ruolo) {
                if (ruolo.ordinal() == Ruolo.DISEGNATORE.ordinal()) {
                    System.out.println("Sono disegnatore");
                    giocatore.setDisegnatore(true);
                    String parolaDisegnare = (String) giocatore.getInputStream().readObject();
                    giocatore.setParolaDaDisegnare(parolaDisegnare);
                } else {

                }
            }
            System.out.println("dddd");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Giocatore getGiocatore() {
        return giocatore;
    }

    public void setGiocatore(Giocatore giocatore) {
        this.giocatore = giocatore;
    }

    public ClientPainterApplication getApplication() {
        return application;
    }

    public void setApplication(ClientPainterApplication application) {
        this.application = application;
    }
}
