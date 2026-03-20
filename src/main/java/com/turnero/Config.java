package com.turnero;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    public static String getIp() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return null;
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            String ip = props.getProperty("server.ip");
            return (ip != null && !ip.trim().isEmpty()) ? ip.trim() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getPort() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return 8080;
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            String portStr = props.getProperty("server.port", "8080");
            return Integer.parseInt(portStr);
        } catch (Exception e) {
            return 8080;
        }
    }

    public static void saveIp(String ip) {
        Properties props = new Properties();
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                }
            }
            props.setProperty("server.ip", ip.trim());
            if (!props.containsKey("server.port")) {
                props.setProperty("server.port", "8080");
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "Configuración del Sistema de Turnos");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
