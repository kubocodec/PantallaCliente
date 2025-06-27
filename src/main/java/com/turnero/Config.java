package com.turnero;

import java.io.IOException;
import java.io.InputStream; // Importante cambiar a InputStream
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties"; // El nombre del archivo en resources
    private static final Properties properties = new Properties();

    // El bloque static se ejecuta una sola vez cuando la clase es cargada
    static {
        // CAMBIO CLAVE: Usamos getResourceAsStream para leer desde dentro del JAR
        try (InputStream input = Config.class.getResourceAsStream("/" + CONFIG_FILE)) {

            if (input == null) {
                System.err.println("❌ No se pudo encontrar el archivo config.properties dentro del JAR.");
            } else {
                // Cargar las propiedades desde el stream de entrada
                properties.load(input);
            }

        } catch (IOException e) {
            System.err.println("⚠️ Error cargando el archivo de configuración interno.");
            // En una aplicación real, podrías querer lanzar una excepción aquí para detener la app
            e.printStackTrace();
        }
    }

    public static String getIp() {
        return properties.getProperty("server.ip", "127.0.0.1");
    }

    public static int getPort() {
        // Es más seguro parsear con un try-catch por si el valor no es un número válido
        try {
            return Integer.parseInt(properties.getProperty("server.port", "8080"));
        } catch (NumberFormatException e) {
            System.err.println("El puerto en config.properties no es un número válido. Usando puerto por defecto 8080.");
            return 8080;
        }
    }
}