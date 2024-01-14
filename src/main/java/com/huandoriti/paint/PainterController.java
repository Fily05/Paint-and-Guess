package com.huandoriti.paint;

import com.huandoriti.paint.game.*;
import com.huandoriti.paint.game.canvastransfer.Forma;
import com.huandoriti.paint.game.canvastransfer.Oval;
import com.huandoriti.paint.game.canvastransfer.Rect;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.Finishings;
import java.io.*;
import java.net.SocketException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.*;

public class PainterController {
    private Giocatore giocatore;
    private ClientPainterApplication application;
    @FXML
    private Pane panePrincipale;
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
    @FXML
    private Label orario;
    private Timeline sendCanvasTimeline;
    private ScheduledExecutorService receiveDataService;
    private ScheduledExecutorService timeService;
    private boolean isStopped;
    private Duration tempoRimasto = Partita.MAX_TEMPO;

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
        chat.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                send.fire();
            }
        });
        chat.setLineSpacing(0.5);

        brushSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 35, 12, 4));

        sendCanvasTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> Platform.runLater(() -> sendCanvas())));
        sendCanvasTimeline.setCycleCount(Animation.INDEFINITE);
        sendCanvasTimeline.play();

        receiveDataService = Executors.newSingleThreadScheduledExecutor();
        receiveDataService.scheduleAtFixedRate(new Task<>() {
            @Override
            protected Object call() throws Exception {
                while (true) {
                    if (receiveDataService.isShutdown()) {
                        return null;
                    }
                    if (isStopped) {
                        return null;
                    }
                    receiveData();
                }
            }
        }, 1, Integer.MAX_VALUE, TimeUnit.SECONDS);


        timeService = Executors.newSingleThreadScheduledExecutor();
        timeService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if ((int) tempoRimasto.toSeconds() == 0 || isStopped) {
                    //TODO: finisce tempo del gioco o termina gioco
                    stopGame();
                    timeService.shutdown();
                } else {
                    tempoRimasto = tempoRimasto.subtract(Duration.seconds(1));
                    orario.setText(LocalTime.MIN.plusSeconds((int) tempoRimasto.toSeconds()).toString());
                }
            });
        }, 1, 1, TimeUnit.SECONDS);



    }

    public void stopGame() {
        chatArea.setDisable(true);
        send.setDisable(true);
        clear.setDisable(true);
        sendCanvasTimeline.stop();
//        receiveDataService.shutdown();
        timeService.shutdown();
        isStopped = true;

    }

    /**
     * Inviare una parola allo server. e
     */
    public void sendWords() {
        try {
            if (chatArea.getText() != null && !chatArea.getText().trim().isEmpty()) {
                Text text = new Text("Tu: " + chatArea.getText() + "\n");
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
            System.out.println("Aspetto che ricevo");
            Object o;
            synchronized (giocatore.getInputStream()) {
                try {
                    o = giocatore.getInputStream().readObject();
                } catch (IOException | ClassNotFoundException exception) {
                    exception.printStackTrace();
                    return;
                }
            }
            System.out.println("ho ricevuto oggetto");
            if (o instanceof ArrayList) {
                receiveCanvas((ArrayList<Forma>) o);
                return;
            }
            if (o instanceof String s) {
                Platform.runLater(() -> {
                    System.out.println("Ho ricevuto stringa " + s);
                    Text text = new Text(s + "\n");
                    text.setFont(Font.loadFont("Comic Sans MS", 18));
                    chat.getChildren().add(text);
                });
                return;
            }
            if (o instanceof Instruction instruction && instruction == Instruction.FINISH) {
                do {
                    Object oggetto;
                    synchronized (giocatore.getInputStream()) {
                        try {
                            oggetto = giocatore.getInputStream().readObject();
                        } catch (IOException | ClassNotFoundException exception) {
                            exception.printStackTrace();
                            continue;
                        }
                    }
                    if (oggetto instanceof String s) {
                        System.out.println(s);
                        Platform.runLater(() -> {
                            Text text = new Text(s + "\n");
                            text.setFont(Font.loadFont("Comic Sans MS", 18));
                            chat.getChildren().add(text);
                        });
                    } else if (oggetto instanceof Instruction) {
                        instruction = (Instruction) oggetto;
                    }
                } while (instruction != Instruction.DONE);
                stopGame();
                receiveDataService.shutdown();
                try {
                    System.out.println("chiudo socket");
                    giocatore.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                if (!forme.isEmpty()) {
                    System.out.println("Send canvas");
                    System.out.println(forme.toString());
                    synchronized (giocatore.getOutputStream()) {
                        giocatore.getOutputStream().writeObject(forme);
                        giocatore.getOutputStream().flush();
                    }
                    forme = new ArrayList<>();
                }

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


    public void onExit(WindowEvent event) {
        event.consume();
        if (!isStopped) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Paint and Guess");
            alert.setTitle("Impossibile chiudere il gioco");
            alert.setContentText("La partita non è ancora terminata!!!");
            alert.showAndWait();
        } else {
            Platform.exit();
        }

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
