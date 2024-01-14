package com.huandoriti.paint;

import com.huandoriti.paint.game.Giocatore;
import com.huandoriti.paint.game.Ruolo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class IniziaController {
    private Giocatore giocatore;
    private ClientPainterApplication application;
    @FXML
    private Button inizia;
    @FXML
    private Label message;
    private boolean isConnected;

    public void onStart() {
        if (isConnected) {
            return;
        }

        caricaGiocare();
    }

    public void caricaGiocare() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    System.out.println("ciaooo");
                    giocatore = new Giocatore(new Socket("localhost", 5544));
                    isConnected = true;
                    Platform.runLater(() -> message.setText("In attesa degli altri giocatori... (in tutto 3 giocatori)"));
                    synchronized (giocatore.getInputStream()) {
                        if (giocatore.getInputStream().readObject() instanceof Ruolo ruolo) {
                            if (ruolo.ordinal() == Ruolo.DISEGNATORE.ordinal()) {
                                System.out.println("Sono disegnatore");
                                giocatore.setDisegnatore(true);
                                String parolaDisegnare = (String) giocatore.getInputStream().readObject();
                                giocatore.setParolaDaDisegnare(parolaDisegnare);
                            }
                        }
                    }
                    System.out.println("fatto");
                }catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        task.setOnSucceeded((event) -> {
            try {
                System.out.println("LoadGame");
                application.loadGame("home.fxml", "PAINT AND GUESS", giocatore);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        new Thread(task).start();
    }
    public void onExit(WindowEvent event) {
        event.consume();
        if (isConnected) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Paint and Guess");
            alert.setTitle("Impossibile chiudere il gioco");
            alert.setContentText("La partita non è ancora iniziata, aspetta!!!");
            alert.showAndWait();
        } else {
            Platform.exit();
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
