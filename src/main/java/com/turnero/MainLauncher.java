package com.turnero;

/**
 * Esta clase sirve como el punto de entrada real para el JAR empaquetado.
 * Su única función es llamar al main de la clase principal de JavaFX.
 * Esto evita problemas con el class-loader en entornos de fat-jar.
 */
public class MainLauncher {
    public static void main(String[] args) {
        VisorPantalla.main(args);
    }
}