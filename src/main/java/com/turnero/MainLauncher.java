package com.turnero;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class MainLauncher {
    public static void main(String[] args) {
        if (!obtenerBloqueoInstancia()) {
            // Ya hay una instancia corriendo, salir silenciosamente
            System.out.println("La aplicación ya está en ejecución.");
            System.exit(0);
            return;
        }
        VisorPantalla.main(args);
    }

    /**
     * Intenta adquirir un FileLock sobre un archivo en el directorio temporal
     * del sistema. Devuelve true si esta es la única instancia, false si ya
     * hay otra corriendo.
     * El lock se libera automáticamente cuando el proceso termina.
     */
    private static boolean obtenerBloqueoInstancia() {
        try {
            File lockFile = new File(System.getProperty("java.io.tmpdir"), "turnero_pantalla.lock");
            RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
            FileChannel channel = raf.getChannel();
            FileLock lock = channel.tryLock();
            if (lock == null) {
                raf.close();
                return false;
            }
            // Mantener la referencia para que no sea recolectada
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { lock.release(); channel.close(); raf.close(); } catch (Exception ignored) {}
            }));
            return true;
        } catch (Exception e) {
            // Si no se puede crear el lock (permisos, etc.) se deja iniciar igual
            return true;
        }
    }
}