package com.huandoriti.paint;

import com.huandoriti.paint.game.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javax.crypto.spec.DESedeKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.TileObserver;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

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
    @FXML
    private ArrayList<Forma> forme = new ArrayList<>();
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
                forme.add(new Rect(x,y,size,size));
            } else {
                Color color = (Color) colorPicker.getValue();
                g.setFill(color);
                g.fillOval(x, y, size, size);
                forme.add(new Oval(x,y,size,size, color.toString()));
            }
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> sendCanvas()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        Timeline timeline2 = new Timeline(new KeyFrame(Duration.millis(50), e -> receiveCanvas()));
        timeline2.setCycleCount(Animation.INDEFINITE);
        timeline2.play();
    }

    public void receiveCanvas() {
        if (giocatore != null && !giocatore.isDisegnatore()) {
            try {
                    ArrayList<Forma> o = (ArrayList<Forma>) giocatore.getInputStream().readObject();
                    System.out.println("Receive canvas");
                    System.out.println(o.toString());
                    for (Forma forma : o) {
                        if (forma instanceof Rect rect) {
                            canvas.getGraphicsContext2D().clearRect(rect.x, rect.y, rect.width, rect.height);
                        } else if (forma instanceof Oval oval) {
                            canvas.getGraphicsContext2D().setFill(Color.valueOf(oval.color));
                            canvas.getGraphicsContext2D().fillOval(oval.x, oval.y, oval.width, oval.height);
                        }
                    }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCanvas() {
        if (giocatore != null && giocatore.isDisegnatore()) {
            try {

                    System.out.println("Send canvas");
                    System.out.println(forme.toString());
                    giocatore.getOutputStream().writeObject(forme);
                    giocatore.getOutputStream().flush();
                    forme = new ArrayList<>();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        //TODO: trasmettere cliear
        if (giocatore != null && giocatore.isDisegnatore()) {
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            forme.add(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()));
        }

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
