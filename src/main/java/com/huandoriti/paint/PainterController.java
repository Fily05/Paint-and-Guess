package com.huandoriti.paint;

import com.huandoriti.paint.game.Giocatore;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class PainterController {
    private Giocatore giocatore;
    private ClientPainterApplication application;

    @FXML
    private Label parolaDaDisegnare;

    @FXML
    private Canvas canvas;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private TextField brushSize;

    @FXML
    private CheckBox eraser;
    @FXML
    private Button clear;
    @FXML
    private Label nomeGiocatore;

    public void initialize() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged(e -> {
            if (giocatore != null && !giocatore.isDisegnatore())
                return;

            double size = Double.parseDouble(brushSize.getText());
            double x = e.getX() - size / 2;
            double y = e.getY() - size / 2;

            if (eraser.isSelected()) {
                g.clearRect(x, y, size, size);
            } else {
                g.setFill(colorPicker.getValue());
                g.fillRect(x, y, size, size);
            }
        });
    }

    /**
     * NOn inserire nel initialize siccome initialize viene eseguitio prima quando
     * giocatore non viene ancora caricato o  esegui
     */
    public void caricaTestoDisegnare() {
        if (giocatore != null && giocatore.isDisegnatore()) {
            System.out.println("Sono disegnatorre painter disegno" + giocatore.getParolaDaDisegnare());
            parolaDaDisegnare.setText(giocatore.getParolaDaDisegnare());
        }
    }

    public void caricaNomeGiocatore() {
        try {
            String numero = (String) giocatore.getInputStream().readObject();
            nomeGiocatore.setText((giocatore.isDisegnatore() ? "Disegnatore " : "Indovinatore ") + numero);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onSave() {
        try {
            Image snapshot = canvas.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File("paint.png"));
        } catch (Exception e) {
            System.out.println("Failed to save image: " + e);
        }
    }

    public void onExit() {
        Platform.exit();
    }
    public void onClear() {
        if (giocatore != null && giocatore.isDisegnatore())
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public ClientPainterApplication getApplication() {
        return application;
    }

    public void setApplication(ClientPainterApplication application) {
        this.application = application;
    }

    public Giocatore getGiocatore() {
        return giocatore;
    }

    public void setGiocatore(Giocatore giocatore) {
        this.giocatore = giocatore;
    }
}
