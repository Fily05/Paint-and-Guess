package com.huandoriti.paint;

import com.huandoriti.paint.game.Giocatore;
import com.huandoriti.paint.game.Partita;
import com.huandoriti.paint.game.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.border.TitledBorder;
import java.io.IOException;
import java.net.Socket;

public class ClientPainterApplication extends Application {
    private Giocatore giocatore;
    private Stage primaryStage;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("inizia.fxml"));
        Parent parent = loader.load();
        IniziaController iniziaController = loader.getController();
        iniziaController.setApplication(this);
        stage.setScene(new Scene(parent));
        stage.setTitle("Paint App");
        stage.show();
        primaryStage = stage;
    }

    public void loadGame(String resourceURL, String title, Giocatore giocatore) throws IOException {
        this.giocatore = giocatore;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourceURL));
        Parent parent = loader.load();
        PainterController painterController = loader.getController();
        painterController.setApplication(this);
        painterController.setGiocatore(giocatore);
        painterController.caricaTestoDisegnare();
        painterController.caricaNomeGiocatore();
//        primaryStage.hide();
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle(title);
//        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Giocatore getGiocatore() {
        return giocatore;
    }

    public void setGiocatore(Giocatore giocatore) {
        this.giocatore = giocatore;
    }
}
