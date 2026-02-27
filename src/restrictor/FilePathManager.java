package restrictor;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

/**
 * @author
 * Neftaly Hardy
 * 
 * 
 * Nota: Clase centralizada para manejar las rutas de archivos de la aplicación
 */

public class FilePathManager {
    private static final String APP_FOLDER_NAME = "ProcessMonitor";
    private static Path appDataPath;
    
    static {
        initializeAppDataPath();
    }
    
    /**
     * Inicializa la ruta base de la aplicación en la carpeta Documentos
     */
    private static void initializeAppDataPath() {
        // Obtener la carpeta Documentos del usuario
        String documentsPath = System.getProperty("user.home") + File.separator + "Documents";
        
        // En algunos sistemas puede ser "Documentos" en lugar de "Documents"
        File documentsDir = new File(documentsPath);
        if (!documentsDir.exists()) {
            documentsPath = System.getProperty("user.home") + File.separator + "Documentos";
        }
        
        // Crear la ruta completa de la aplicación
        appDataPath = Paths.get(documentsPath, APP_FOLDER_NAME);
        
        // Crear el directorio si no existe
        try {
            Files.createDirectories(appDataPath);
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de la aplicación: " + e.getMessage());
            // Como fallback, usar el directorio actual
            appDataPath = Paths.get(System.getProperty("user.dir"));
        }
    }
    
    /**
     * Obtiene la ruta base de la aplicación
     */
    public static Path getAppDataPath() {
        return appDataPath;
    }
    
    /**
     * Obtiene la ruta completa para un archivo específico
     */
    public static Path getFilePath(String filename) {
        return appDataPath.resolve(filename);
    }
    
    /**
     * Obtiene la ruta del archivo de configuración
     */
    public static Path getConfigFilePath() {
        return getFilePath("config.txt");
    }
    
    /**
     * Obtiene la ruta del archivo de bloqueo
     */
    public static Path getLockFilePath() {
        return getFilePath("app.lock");
    }
    
    /**
     * Obtiene la ruta por defecto del archivo de contraseña
     */
    public static Path getDefaultPasswordFilePath() {
        return getFilePath("confCon.txt");
    }
    
    /**
     * Verifica si el directorio de la aplicación existe y es escribible
     */
    public static boolean isAppDataPathWritable() {
        return Files.exists(appDataPath) && Files.isWritable(appDataPath);
    }
    
    /**
     * Obtiene información sobre la ubicación de los archivos
     */
    public static String getAppDataInfo() {
        return "Directorio de datos: " + appDataPath.toAbsolutePath().toString();
    }
}