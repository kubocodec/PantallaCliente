package com.turnero;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
        } catch (IOException e) {
            System.err.println("⚠️ Error cargando el archivo de configuración: " + e.getMessage());
        }
    }

    public static String getIp() {
        return properties.getProperty("server.ip", "127.0.0.1");
    }

    public static int getPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8080"));
    }
}
