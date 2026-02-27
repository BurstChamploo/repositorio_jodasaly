package restrictor;
import java.io.*;
import java.util.Scanner;
import java.nio.file.Path;

/**
 * @author
 * Neftaly Hardy
 */

public class InstanceManager {
    
    public static boolean ensureSingleInstance() {
        try {
            Path lockFilePath = FilePathManager.getLockFilePath();
            File lockFile = lockFilePath.toFile();
            
            if (lockFile.exists()) {
                Scanner scanner = new Scanner(lockFile);
                if (scanner.hasNextLong()) {
                    long previousPid = scanner.nextLong();
                    scanner.close();
                    if (ProcessHandle.of(previousPid).isPresent()) {
                        return false;
                    }
                }
                scanner.close();
            }
            
            try (FileWriter writer = new FileWriter(lockFile)) {
                writer.write(String.valueOf(ProcessHandle.current().pid()));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error en InstanceManager: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Limpia el archivo de bloqueo al salir de la aplicación
     */
    public static void cleanup() {
        try {
            Path lockFilePath = FilePathManager.getLockFilePath();
            File lockFile = lockFilePath.toFile();
            if (lockFile.exists()) {
                lockFile.delete();
            }
        } catch (Exception e) {
            System.err.println("Error al limpiar archivo de bloqueo: " + e.getMessage());
        }
    }
}