package com.turnero;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VisorPantalla extends Application {

    private Label lblTurno = new Label("Turno...");
    private Label lblCategoria = new Label("");

    @Override
    public void start(Stage stage) {
        lblTurno.setStyle("-fx-font-size: 80px; -fx-text-fill: #2a2a2a;");
        lblCategoria.setStyle("-fx-font-size: 30px;");

        VBox root = new VBox(30, lblTurno, lblCategoria);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Pantalla de Turnos");
        stage.setFullScreen(true);
        stage.show();

        // Actualizar cada 3 segundos
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> cargarTurno()),
                new KeyFrame(Duration.seconds(3))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void cargarTurno() {
        try {
            URL url = new URL("http://localhost:8080/api/turnos/actual");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream input = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                Turno turno = mapper.readValue(input, Turno.class);

                lblTurno.setText("Turno N° " + turno.getNumero());
                lblCategoria.setText(turno.getCategoria());
            } else {
                lblTurno.setText("Esperando...");
                lblCategoria.setText("");
            }
        } catch (Exception e) {
            lblTurno.setText("Sin conexión");
            lblCategoria.setText("");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
