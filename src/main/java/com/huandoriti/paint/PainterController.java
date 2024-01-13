package com.huandoriti.paint;

import com.huandoriti.paint.game.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import javax.crypto.spec.DESedeKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.TileObserver;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.io.*;
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
    private Spinner<Integer> brushSize;

    @FXML
    private CheckBox eraser;
    @FXML
    private Button clear;
    @FXML
    private Label nomeGiocatore;
    @FXML
    private ArrayList<Forma> forme = new ArrayList<>();
    @FXML
    private TextField chatArea;
    @FXML
    private TextFlow chat;
    @FXML
    private Button send;



    public void initialize() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged(e -> {
            if (giocatore != null && !giocatore.isDisegnatore())
                return;

            double size = brushSize.getValue();
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
        send.setOnAction(actionEvent -> {
            if (giocatore != null && giocatore.isDisegnatore())
                return;
            System.out.println("Send words");
            sendWords();
        });

        chat.setLineSpacing(0.5);
        brushSize.setEditable(true);
        brushSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 48, 12, 2));
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), e -> Platform.runLater(() -> sendCanvas())));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        Timeline timeline2 = new Timeline(new KeyFrame(Duration.millis(2000), e -> new Thread(new Task<>() {
            @Override
            protected Object call() throws Exception {
                receiveData();
                return null;
            }
        }).start()));
        timeline2.setCycleCount(Animation.INDEFINITE);
        timeline2.play();
    }


    public void sendWords() {
        try {
            if (chatArea.getText() != null && !chatArea.getText().trim().isEmpty()) {
                Text text = new Text(chatArea.getText() + "\n");
                text.setFont(Font.loadFont("Comic Sans MS", 18));
                chat.getChildren().add(text);
                synchronized (giocatore.getOutputStream()) {
                    giocatore.getOutputStream().writeObject(chatArea.getText().trim());
                    giocatore.getOutputStream().flush();
                }
                chatArea.setText(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveData() {
        if (giocatore != null) {
            try {
                System.out.println("Aspetto che ricevo");
                Object o;
                o = giocatore.getInputStream().readObject();
                System.out.println("ho ricevuto oggetto");
                if (o instanceof ArrayList) {
                    receiveCanvas((ArrayList<Forma>) o);
                } else if (o instanceof String s) {
                    Platform.runLater(() -> {
                        System.out.println("Ho ricevuto stringa " + s);
                        Text text = new Text(s + "\n");
                        text.setFont(Font.loadFont("Comic Sans MS", 18));
                        chat.getChildren().add(text);
                    });

                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveCanvas(ArrayList<Forma> formaArrayList) {
        if (giocatore != null && !giocatore.isDisegnatore()) {
            ArrayList<Forma> o = formaArrayList;
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
        }
    }

    public void sendCanvas() {
        if (giocatore != null && giocatore.isDisegnatore()) {
            try {
                System.out.println("Send canvas");
                System.out.println(forme.toString());
                synchronized (giocatore.getOutputStream()) {
                    giocatore.getOutputStream().writeObject(forme);
                    giocatore.getOutputStream().flush();
                }
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
            String numero;
            synchronized (giocatore.getInputStream()) {
                 numero = (String) giocatore.getInputStream().readObject();
            }
            nomeGiocatore.setText((giocatore.isDisegnatore() ? "Tu: Disegnatore " : "Tu: Indovinatore ") + numero);
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
