package com.turnero;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ConfigWindow extends Application {

    private Stage stage;
    private TextField txtIp;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        // Estilo moderno
        root.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #3498db; -fx-border-width: 2px;");

        Label lblTitle = new Label("⚙️ Configuración Inicial");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblDesc = new Label("Por favor, ingrese la IP de la computadora\ndel servidor principal (Ej: 192.168.1.156)");
        lblDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");

        txtIp = new TextField();
        txtIp.setPromptText("Ejemplo: 192.168.x.x o localhost");
        txtIp.setPrefHeight(45);
        txtIp.setStyle("-fx-font-size: 16px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #bdc3c7; -fx-padding: 10;");
        
        // Auto-enfocar el campo de IP
        txtIp.requestFocus();

        Button btnSave = new Button("Guardar IP e Iniciar");
        btnSave.setPrefHeight(45);
        btnSave.setPrefWidth(250);
        btnSave.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #2980b9); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
        
        btnSave.setOnMouseEntered(e -> btnSave.setStyle("-fx-background-color: linear-gradient(to right, #2980b9, #1f618d); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnSave.setOnMouseExited(e -> btnSave.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #2980b9); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;"));

        btnSave.setOnAction(e -> handleSave());
        txtIp.setOnAction(e -> handleSave());

        DropShadow shadow = new DropShadow();
        shadow.setRadius(25);
        shadow.setColor(Color.rgb(0,0,0,0.3));
        shadow.setOffsetY(5);
        root.setEffect(shadow);

        root.getChildren().addAll(lblTitle, lblDesc, txtIp, btnSave);

        Scene scene = new Scene(root, 450, 320);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setTitle("Configuración IP");
        primaryStage.show();
    }
    
    private void handleSave() {
        String ip = txtIp.getText().trim();
        if (!ip.isEmpty()) {
            Config.saveIp(ip);
            stage.close();
            // Arrancar la app principal
            iniciarApp();
        }
    }

    private void iniciarApp() {
        try {
            VisorPantalla visor = new VisorPantalla();
            visor.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
