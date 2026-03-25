package com.turnero;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import com.turnero.Config;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class VisorPantalla extends Application {

    private VBox filasContainer = new VBox(8);
    private Label relojLabel = new Label();
    private Label fechaLabel = new Label();

    // Carrusel
    private List<Image> imagenesCarrusel = new ArrayList<>();
    private ImageView imagenCentral = new ImageView();
    private int indiceImagenActual = 0;

    // Tracker para el último turno llamado
    private Long ultimoIdTurno = null;
    private Integer ultimaCantidadLlamadas = null;

    private final Map<String, String> prefijos = new LinkedHashMap<String, String>() {{
        put("General", "G");
        put("Preferencial", "P");
        put("Discapacidad", "D");
        put("AdultoMayor", "A");
    }};

    @Override
    public void start(Stage stage) {
        if (Config.getIp() == null) {
            ConfigWindow cw = new ConfigWindow();
            cw.start(new Stage());
            return;
        }
        // Cargar las imágenes del carrusel
        //cargarImagenesDesdeURLs();
        cargarImagenesDesdeRecursos();

        relojLabel.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        relojLabel.setTextFill(Color.WHITE);
        fechaLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 32));
        fechaLabel.setTextFill(Color.WHITE);
        actualizarReloj();

        BorderPane root = new BorderPane();
        root.setPrefSize(1920, 1080);
        
        root.setTop(crearHeaderCompleto());
        root.setCenter(crearContenidoCentral());
        root.setBottom(crearFooter());

        // Escalar proporcionalmente todo el contenido para adaptarse a la pantalla
        Group scaleGroup = new Group(root);
        StackPane appRoot = new StackPane(scaleGroup);
        appRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f2f5, #ffffff);");
        
        Scale scaleTransform = new Scale(1, 1, 0, 0);
        root.getTransforms().add(scaleTransform);

        appRoot.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scale = Math.min(appRoot.getWidth() / 1920.0, appRoot.getHeight() / 1080.0);
            scaleTransform.setX(scale);
            scaleTransform.setY(scale);
        });
        
        appRoot.heightProperty().addListener((obs, oldVal, newVal) -> {
            double scale = Math.min(appRoot.getWidth() / 1920.0, appRoot.getHeight() / 1080.0);
            scaleTransform.setX(scale);
            scaleTransform.setY(scale);
        });

        Scene scene = new Scene(appRoot, 1920, 1080);
        
        // --- Doble clic para pantalla completa ---
        scene.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                stage.setFullScreen(true);
            }
        });

        stage.setScene(scene);
        stage.setTitle("Sistema Profesional de Turnos - KuboCode");

        // --- Mostrar en la pantalla complementaria (si existe) ---
        List<Screen> screens = Screen.getScreens();
        if (screens.size() > 1) {
            Screen secondaryScreen = screens.get(1);
            Rectangle2D bounds = secondaryScreen.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        }

        stage.show();
        stage.setFullScreen(true);

        // Timeline reloj
        Timeline relojTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> actualizarReloj()),
                new KeyFrame(Duration.seconds(1))
        );
        relojTimeline.setCycleCount(Timeline.INDEFINITE);
        relojTimeline.play();

        // Timeline turnos
        Timeline turnosTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> cargarTurnos()),
                new KeyFrame(Duration.seconds(3))
        );
        turnosTimeline.setCycleCount(Timeline.INDEFINITE);
        turnosTimeline.play();

        // Timeline carrusel imágenes
        Timeline carruselTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> cambiarImagenCarrusel()),
                new KeyFrame(Duration.seconds(8))
        );
        carruselTimeline.setCycleCount(Timeline.INDEFINITE);
        carruselTimeline.play();
    }

    // ---- HEADER ----
    private VBox crearHeaderCompleto() {
        VBox headerCompleto = new VBox();

        HBox barraInfo = new HBox();
        barraInfo.setPrefHeight(100);
        barraInfo.setStyle("-fx-background-color: #34495e;");
        barraInfo.setAlignment(Pos.CENTER);
        barraInfo.setSpacing(100);
        barraInfo.setPadding(new Insets(20));

        VBox tiempoContainer = new VBox(5);
        tiempoContainer.setAlignment(Pos.CENTER);
        tiempoContainer.getChildren().addAll(relojLabel, fechaLabel);

        Label bienvenida = new Label("¡Bienvenidos! Manténganse atentos a su turno");
        bienvenida.setFont(Font.font("Arial", FontWeight.NORMAL, 28));
        bienvenida.setTextFill(Color.WHITE);

        barraInfo.getChildren().addAll(bienvenida, tiempoContainer);
        headerCompleto.getChildren().addAll(barraInfo);
        return headerCompleto;
    }

    // ---- CENTRO ----
    private HBox crearContenidoCentral() {
        HBox contenidoCentral = new HBox(60);
        contenidoCentral.setAlignment(Pos.CENTER);
        contenidoCentral.setPadding(new Insets(60));
        contenidoCentral.setPrefHeight(700);

        VBox imagenContainer = new VBox();
        imagenContainer.setAlignment(Pos.CENTER);

        imagenCentral.setFitWidth(800);
        imagenCentral.setFitHeight(500);
        imagenCentral.setPreserveRatio(false);
        imagenCentral.setImage(imagenesCarrusel.isEmpty() ? null : imagenesCarrusel.get(0));

        StackPane marcoImagen = new StackPane();
        marcoImagen.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 15;");
        marcoImagen.getChildren().add(imagenCentral);

        DropShadow sombraImagen = new DropShadow();
        sombraImagen.setRadius(25);
        sombraImagen.setColor(Color.rgb(0, 0, 0, 0.3));
        marcoImagen.setEffect(sombraImagen);

        imagenContainer.getChildren().add(marcoImagen);
        VBox tablaContainer = crearTablaProfesional();
        contenidoCentral.getChildren().addAll(imagenContainer, tablaContainer);
        return contenidoCentral;
    }

    // ---- TABLA ----
    private VBox crearTablaProfesional() {
        VBox tablaContainer = new VBox();
        tablaContainer.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        tablaContainer.setPrefWidth(700);
        tablaContainer.setPrefHeight(500);

        DropShadow sombra = new DropShadow();
        sombra.setRadius(30);
        sombra.setColor(Color.rgb(0, 0, 0, 0.4));
        tablaContainer.setEffect(sombra);

        HBox headerTabla = new HBox();
        headerTabla.setStyle("-fx-background: linear-gradient(to right, #2980b9, #3498db); -fx-background-radius: 20 20 0 0;");
        headerTabla.setPrefHeight(80);
        headerTabla.setAlignment(Pos.CENTER);

        Label headerTurno = new Label("TURNO");
        headerTurno.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        headerTurno.setTextFill(Color.BLACK);
        headerTurno.setPrefWidth(350);
        headerTurno.setAlignment(Pos.CENTER);

        Label headerPuesto = new Label("PUESTO");
        headerPuesto.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        headerPuesto.setTextFill(Color.BLACK);
        headerPuesto.setPrefWidth(350);
        headerPuesto.setAlignment(Pos.CENTER);

        headerTabla.getChildren().addAll(headerTurno, headerPuesto);
        filasContainer.setSpacing(8);
        filasContainer.setPadding(new Insets(20));
        filasContainer.setPrefHeight(400);

        tablaContainer.getChildren().addAll(headerTabla, filasContainer);
        return tablaContainer;
    }

    // ---- FILA ----
    private HBox crearFilaTurno(String turno, String puesto, boolean esPrimero) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER);
        fila.setPrefHeight(70);
        fila.setPadding(new Insets(15));
        fila.setStyle(esPrimero ? "-fx-background-color: #27ae60; -fx-background-radius: 12;"
                : "-fx-background-color: #ecf0f1; -fx-background-radius: 12;");

        Label turnoLabel = new Label(turno);
        turnoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        turnoLabel.setPrefWidth(350);
        turnoLabel.setAlignment(Pos.CENTER);
        turnoLabel.setTextFill(esPrimero ? Color.WHITE : Color.web("#2c3e50"));

        Label puestoLabel = new Label(puesto);
        puestoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        puestoLabel.setPrefWidth(350);
        puestoLabel.setAlignment(Pos.CENTER);
        puestoLabel.setTextFill(esPrimero ? Color.WHITE : Color.web("#2c3e50"));

        fila.getChildren().addAll(turnoLabel, puestoLabel);
        return fila;
    }

    // ---- FOOTER ----
    private HBox crearFooter() {
        HBox footer = new HBox();
        footer.setPrefHeight(60);
        footer.setStyle("-fx-background-color: #2c3e50;");
        footer.setAlignment(Pos.CENTER);

        Label footerText = new Label("Sistema desarrollado por KuboCode - Mantenga la distancia y espere su turno");
        footerText.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        footerText.setTextFill(Color.WHITE);

        footer.getChildren().add(footerText);
        return footer;
    }

    // ---- ACTUALIZAR RELOJ ----
    private void actualizarReloj() {
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        relojLabel.setText(LocalDateTime.now().format(horaFormatter));
        fechaLabel.setText(LocalDateTime.now().format(fechaFormatter));
    }

    private void cargarTurnos() {
        try {
            URL url = new URL("http://" + Config.getIp() + ":" + Config.getPort() + "/api/turnos/ultimos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                InputStream input = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper()
                        .registerModule(new JavaTimeModule())          // para LocalDateTime
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                List<Turno> turnos = mapper.readValue(
                        input,
                        new TypeReference<List<Turno>>() {}
                );

                if (!turnos.isEmpty()) {
                    Turno primerTurno = turnos.get(0);
                    Integer actualCantidadLlamadas = primerTurno.getCantidadLlamadas() != null ? primerTurno.getCantidadLlamadas() : 1;

                    if (ultimoIdTurno == null) {
                        ultimoIdTurno = primerTurno.getId();
                        ultimaCantidadLlamadas = actualCantidadLlamadas;
                    } else if (!ultimoIdTurno.equals(primerTurno.getId()) || (ultimoIdTurno.equals(primerTurno.getId()) && actualCantidadLlamadas > (ultimaCantidadLlamadas != null ? ultimaCantidadLlamadas : 0))) {
                        ultimoIdTurno = primerTurno.getId();
                        ultimaCantidadLlamadas = actualCantidadLlamadas;
                        reproducirSonido();
                    }
                }

                filasContainer.getChildren().clear();

                // Mostramos máximo 5, resaltando el primero
                for (int i = 0; i < Math.min(turnos.size(), 5); i++) {
                    Turno t = turnos.get(i);
                    String turnoTexto  = t.getNumero();               // «P003», «I002», etc.
                    String puestoTexto = String.valueOf(t.getPuesto());
                    filasContainer.getChildren().add(
                            crearFilaTurno(turnoTexto, puestoTexto, i == 0)
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- CARGAR CARRUSEL DESDE resources/carrusel ----
//    private void cargarImagenesDesdeRecursos() {
//        List<String> rutas = Arrays.asList(
//                "/carrusel/imagen2.jpg",
//                "/carrusel/imagen3.jpg",
//                "/carrusel/imagen4.jpg"
//        );
//
//        for (String ruta : rutas) {
//            try {
//                URL url = getClass().getResource(ruta); // busca en el classpath
//                if (url == null) {
//                    System.err.println("No se encontró " + ruta);
//                    continue;
//                }
//                Image img = new Image(url.toExternalForm(), false);
//                imagenesCarrusel.add(img);
//            } catch (Exception e) {
//                System.err.println("Error cargando " + ruta);
//                e.printStackTrace();
//            }
//        }
//    }

    private void cargarImagenesDesdeRecursos() {

        List<String> rutas = Arrays.asList(
                "/carrusel/imagen1.jpg",
                "/carrusel/imagen2.jpg",
                "/carrusel/imagen3.jpg",
                "/carrusel/imagen4.jpg"
        );

        for (String ruta : rutas) {
            try (InputStream in = getClass().getResourceAsStream(ruta)) {

                // 1️⃣  ¿realmente lo encontró?
                System.out.print("--> " + ruta + "  :  ");
                if (in == null) {
                    System.out.println("NO ENCONTRADO");
                    continue;
                } else {
                    System.out.println("OK");
                }

                // 2️⃣  ¿JavaFX lo decodifica?
                Image img = new Image(in);
                if (img.isError()) {
                    System.err.println("    ⚠  ERROR : " + img.getException());
                    continue;
                } else {
                    System.out.println("    ancho × alto = "
                            + (int) img.getWidth() + " × " + (int) img.getHeight());
                }

                imagenesCarrusel.add(img);

            } catch (Exception ex) {
                System.err.println("    EXCEPCIÓN : " + ex.getMessage());
            }
        }
    }


    // ---- CAMBIAR IMAGEN ----
    private void cambiarImagenCarrusel() {
        if (imagenesCarrusel.isEmpty()) return;
        imagenCentral.setImage(imagenesCarrusel.get(indiceImagenActual));
        indiceImagenActual = (indiceImagenActual + 1) % imagenesCarrusel.size();
    }

    // ---- METODO SONIDO ----
    private void reproducirSonido() {
        new Thread(() -> {
            try {
                // Leer el audio completo en memoria para evitar problemas con JARs empaquetados
                InputStream rawStream = getClass().getResourceAsStream("/sonidos/timbre.wav");
                if (rawStream == null) {
                    System.err.println("No se encontró el archivo de sonido: /sonidos/timbre.wav");
                    return;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[4096];
                int bytesRead;
                while ((bytesRead = rawStream.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                rawStream.close();
                byte[] audioBytes = buffer.toByteArray();

                for (int i = 0; i < 2; i++) {
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(audioBytes);
                    AudioInputStream ais = AudioSystem.getAudioInputStream(byteStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);

                    // Subir volumen al máximo
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        volume.setValue(volume.getMaximum());
                    }

                    clip.start();

                    // Esperar a que termine el sonido antes de repetir
                    Thread.sleep(clip.getMicrosecondLength() / 1000 + 300);
                    clip.close();
                }
            } catch (Exception e) {
                System.err.println("Error al reproducir el sonido: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        if (Config.getIp() == null) {
            Application.launch(ConfigWindow.class, args);
        } else {
            launch(args);
        }
    }
}
