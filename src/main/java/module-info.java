module com.huandoriti.paint {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;


    opens com.huandoriti.paint to javafx.fxml, javafx.swing;
    exports com.huandoriti.paint;

    opens com.huandoriti.paint.game to javafx.fxml, javafx.swing;
    exports com.huandoriti.paint.game;
}