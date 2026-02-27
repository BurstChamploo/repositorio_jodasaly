package restrictor;
import java.nio.file.*;
import java.io.*;
import java.util.prefs.Preferences;

/**
 * @author
 * Neftaly Hardy
 */

public class PasswordManager {
    private static final String DEFAULT_PASSWORD = "Admin123";
    private static final String CONFIG_FILENAME = "confCon.txt";
    private static final String PREF_KEY_PATH = "passwordFilePath";
    
    // Preferencias para guardar la ruta del archivo de contraseña
    private static final Preferences prefs = Preferences.userNodeForPackage(PasswordManager.class);
    
    /**
     * Carga la contraseña desde la ubicación configurada
     */
    public static String loadPassword() {
        try {
            Path path = getPasswordFilePath();
            if (!Files.exists(path)) {
                savePassword(DEFAULT_PASSWORD); // Si no existe, guarda la contraseña por defecto
                return DEFAULT_PASSWORD;
            }
            return new String(Files.readAllBytes(path)).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return DEFAULT_PASSWORD; // En caso de error, devuelve la contraseña por defecto
        }
    }

    /**
     * Guarda la contraseña en la ubicación configurada
     */
    public static void savePassword(String password) {
        try {
            Path path = getPasswordFilePath();
            Path parentDir = path.getParent();
            
            // Solo crear directorios si hay un directorio padre (no es el directorio actual)
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            
            Files.write(path, password.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene la ruta donde se guarda el archivo de contraseña
     */
    public static Path getPasswordFilePath() {
        String savedPath = prefs.get(PREF_KEY_PATH, null);
        if (savedPath == null || savedPath.isEmpty()) {
            // Si no hay ruta guardada, usar la ubicación por defecto (carpeta Documents/ProcessMonitor)
            return FilePathManager.getDefaultPasswordFilePath();
        }
        return Paths.get(savedPath, CONFIG_FILENAME);
    }
    
    /**
     * Establece una nueva ruta para el archivo de contraseña
     */
    public static void setPasswordFilePath(String directoryPath) {
        if (directoryPath != null && !directoryPath.trim().isEmpty()) {
            prefs.put(PREF_KEY_PATH, directoryPath);
        }
    }
    
    /**
     * Devuelve el directorio actual configurado
     */
    public static String getPasswordFileDirectory() {
        String savedPath = prefs.get(PREF_KEY_PATH, null);
        if (savedPath == null || savedPath.isEmpty()) {
            return FilePathManager.getAppDataPath().toString(); // Directorio Documents/ProcessMonitor
        }
        return savedPath;
    }
    
    /**
     * Elimina la ruta personalizada y vuelve a usar la ruta por defecto
     */
    public static void resetToDefaultPath() {
        prefs.remove(PREF_KEY_PATH);
    }
}