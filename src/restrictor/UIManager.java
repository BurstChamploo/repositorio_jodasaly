package restrictor;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.nio.file.Path;
import java.nio.file.Files;

/**
 * @author
 * Neftaly Hardy
 */

public class UIManager {
    private JFrame frame;
    private JTextArea logArea;
    private JButton startButton, stopButton;
    private JSpinner instanceLimitSpinner;
    private ProcessMonitor processMonitor;
    private String PASSWORD = PasswordManager.loadPassword();
    private TrayIcon trayIcon;

    public UIManager() {
        int maxInstances = loadMaxInstances(); // Cargar el valor guardado
        processMonitor = new ProcessMonitor(this, maxInstances);
        initialize();
        setupSystemTray(); // Configurar system tray antes de iniciar monitoreo
        processMonitor.startMonitoring();
        
        // NO mostrar la ventana inicialmente - iniciar en segundo plano
        frame.setVisible(false);
        
        // Mostrar mensaje de que la aplicación está ejecutándose en segundo plano
        if (trayIcon != null) {
            showTrayMessage("Aplicación iniciada en segundo plano");
        }
        
        // Agregar shutdown hook para limpiar archivos al cerrar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            InstanceManager.cleanup();
        }));
    }

    private void initialize() {
        int maxInstances = loadMaxInstances();
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        startButton = new JButton("Iniciar Monitoreo");
        stopButton = new JButton("Detener Monitoreo");
        stopButton.setEnabled(false);

        instanceLimitSpinner = new JSpinner(new SpinnerNumberModel(maxInstances, 1, 10, 1));
        instanceLimitSpinner.addChangeListener(e -> {
            int value = (int) instanceLimitSpinner.getValue();
            processMonitor.setMaxInstances(value);
            saveMaxInstances(value);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.add(new JLabel("Máx instancias:"));
        buttonPanel.add(instanceLimitSpinner);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Panel para las opciones de contraseña
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton changePasswordButton = new JButton("Cambiar Contraseña");
        JButton changePathButton = new JButton("Cambiar Ruta de Contraseña");
        JButton showDataPathButton = new JButton("Ver Ubicación de Archivos");
        
        passwordPanel.add(changePasswordButton);
        passwordPanel.add(changePathButton);
        passwordPanel.add(showDataPathButton);

        // Panel para combinar todos los controles
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(passwordPanel, BorderLayout.SOUTH);

        frame = new JFrame("Monitor de Procesos Cobol");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Cambiado para manejar el cierre manualmente
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        
        // Intentar cargar el ícono, si no existe usar uno por defecto
        Image icon = loadIcon("img/icono.png");
        if (icon != null) {
            frame.setIconImage(icon);
        }

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> processMonitor.startMonitoring());
        stopButton.addActionListener(e -> processMonitor.stopMonitoring());
        changePasswordButton.addActionListener(e -> changePassword());
        changePathButton.addActionListener(e -> changePasswordPath());
        showDataPathButton.addActionListener(e -> showDataPathInfo());

        // Mostrar la ruta actual de la contraseña y archivos al iniciar
        logCurrentPasswordPath();
        logAppDataPath();
        
        // Manejar el evento de cierre de la ventana
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Al cerrar la ventana, minimizar al system tray si está disponible
                if (SystemTray.isSupported() && trayIcon != null) {
                    frame.setVisible(false);
                    showTrayMessage("Aplicación minimizada al system tray");
                } else {
                    // Si no hay system tray, cerrar la aplicación
                    InstanceManager.cleanup();
                    System.exit(0);
                }
            }
            
            @Override
            public void windowIconified(java.awt.event.WindowEvent e) {
                // Minimizar al system tray si está disponible
                if (SystemTray.isSupported() && trayIcon != null) {
                    frame.setVisible(false);
                    showTrayMessage("Aplicación minimizada al system tray");
                }
            }
        });
    }

    private void logAppDataPath() {
        log("═══════════════════════════════════════════════════════════");
        log("📁 " + FilePathManager.getAppDataInfo());
        log("📄 Archivo de configuración: " + FilePathManager.getConfigFilePath().toString());
        log("🔒 Archivo de bloqueo: " + FilePathManager.getLockFilePath().toString());
        log("═══════════════════════════════════════════════════════════");
    }

    private void showDataPathInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Ubicación de archivos de la aplicación:\n\n");
        info.append("Directorio principal:\n").append(FilePathManager.getAppDataPath().toAbsolutePath()).append("\n\n");
        info.append("Archivos:\n");
        info.append("• Configuración: ").append(FilePathManager.getConfigFilePath().getFileName()).append("\n");
        info.append("• Bloqueo: ").append(FilePathManager.getLockFilePath().getFileName()).append("\n");
        info.append("• Contraseña: ").append(PasswordManager.getPasswordFilePath().toAbsolutePath()).append("\n\n");
        info.append("Estado del directorio: ");
        info.append(FilePathManager.isAppDataPathWritable() ? "✅ Escribible" : "❌ No escribible");
        
        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(frame, scrollPane, "Ubicación de Archivos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logCurrentPasswordPath() {
        String currentPath = PasswordManager.getPasswordFileDirectory();
        Path filePath = PasswordManager.getPasswordFilePath();
        
        if (currentPath == null || currentPath.equals(FilePathManager.getAppDataPath().toString())) {
            log("🔑 Ruta actual de contraseña: Directorio de la aplicación (" + filePath.toAbsolutePath().toString() + ")");
        } else {
            log("🔑 Ruta actual de contraseña: " + filePath.toAbsolutePath().toString());
        }
    }

    private void changePasswordPath() {
        if (!authenticate()) {
            JOptionPane.showMessageDialog(frame, "Autenticación requerida para cambiar la ruta.", 
                                        "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Seleccionar directorio para el archivo de contraseña");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = fileChooser.showDialog(frame, "Seleccionar");
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            
            try {
                // Verificar si tenemos permisos de escritura en el directorio
                File testFile = new File(selectedDir, "test_write_permission.tmp");
                if (testFile.createNewFile()) {
                    testFile.delete(); // Limpiar el archivo de prueba
                    
                    // Guardar la nueva ruta
                    String oldPath = PasswordManager.getPasswordFileDirectory();
                    String newPath = selectedDir.getAbsolutePath();
                    
                    if (oldPath == null || !oldPath.equals(newPath)) {
                        // Si la ruta ha cambiado, obtener la contraseña actual
                        String currentPassword = PasswordManager.loadPassword();
                        
                        // Establecer la nueva ruta
                        PasswordManager.setPasswordFilePath(newPath);
                        
                        // Guardar la contraseña en la nueva ubicación
                        PasswordManager.savePassword(currentPassword);
                        
                        log("🔑 Ruta de contraseña cambiada a: " + selectedDir.getAbsolutePath());
                        JOptionPane.showMessageDialog(frame, "Ruta del archivo de contraseña actualizada correctamente.");
                        
                        // Actualizar la visualización de la ruta actual
                        logCurrentPasswordPath();
                    } else {
                        log("ℹ️ Se seleccionó la misma ruta actual. No se realizaron cambios.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "No se tienen permisos de escritura en el directorio seleccionado.",
                                                "Error de permisos", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                log("❌ Error al cambiar la ruta: " + e.getMessage());
                JOptionPane.showMessageDialog(frame, "Error al cambiar la ruta: " + e.getMessage(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changePassword() {
        if (!authenticate()) {
            JOptionPane.showMessageDialog(frame, "Autenticación requerida para cambiar la contraseña.", 
                                        "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JPasswordField newPasswordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);
        
        panel.add(new JLabel("Nueva contraseña: "));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirmar contraseña: "));
        panel.add(confirmPasswordField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Cambiar contraseña", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "La contraseña no puede estar vacía.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(frame, "Las contraseñas no coinciden.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            PasswordManager.savePassword(newPassword);
            PASSWORD = newPassword; // Actualizar la contraseña en memoria
            
            JOptionPane.showMessageDialog(frame, "Contraseña actualizada correctamente.");
            log("✅ Contraseña actualizada correctamente");
            
            // Limpiar las contraseñas de los campos
            java.util.Arrays.fill(newPasswordField.getPassword(), ' ');
            java.util.Arrays.fill(confirmPasswordField.getPassword(), ' ');
        }
    }

    public void clearPassword() {
        if (PASSWORD != null) {
            // Sobrescribir la contraseña en memoria con datos aleatorios
            char[] overwrite = new char[PASSWORD.length()];
            java.util.Arrays.fill(overwrite, '*');
            PASSWORD = new String(overwrite); // Sobrescribir la variable con caracteres irrelevantes
        }
        PASSWORD = null; // Liberar la memoria de la contraseña
        System.gc(); // Sugerir al Garbage Collector que limpie la memoria
    }

    private boolean authenticate() {
        // Crear un JDialog sin padre para que aparezca en el centro de la pantalla
        JDialog authDialog = new JDialog((Frame)null, "Acceso restringido", true);
        authDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPasswordField passwordField = new JPasswordField(15);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Ingrese la contraseña: "));
        inputPanel.add(passwordField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("Aceptar");
        JButton cancelButton = new JButton("Cancelar");
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        authDialog.add(panel);
        authDialog.setSize(400, 150);
        authDialog.setLocationRelativeTo(null); // Centrar en pantalla
        
        final boolean[] authenticated = {false};
        
        okButton.addActionListener(e -> {
            String enteredPassword = new String(passwordField.getPassword());
            String currentPassword = PasswordManager.loadPassword();
            
            if (enteredPassword.equals(currentPassword)) {
                authenticated[0] = true;
                authDialog.dispose();
                clearPassword(); // Limpiar la memoria de la contraseña después de autenticar
            } else {
                JOptionPane.showMessageDialog(authDialog, "Contraseña incorrecta.", 
                                            "Error de autenticación", JOptionPane.ERROR_MESSAGE);
                passwordField.setText(""); // Limpiar el campo
                passwordField.requestFocus();
            }
            
            // Limpiar la memoria de la contraseña ingresada
            java.util.Arrays.fill(passwordField.getPassword(), ' ');
        });
        
        cancelButton.addActionListener(e -> authDialog.dispose());
        
        // Enter para aceptar, Escape para cancelar
        passwordField.addActionListener(e -> okButton.doClick());
        
        authDialog.getRootPane().setDefaultButton(okButton);
        passwordField.requestFocus();
        
        authDialog.setVisible(true);
        
        return authenticated[0];
    }

    private void saveMaxInstances(int value) {
        try {
            Path configPath = FilePathManager.getConfigFilePath();
            Files.write(configPath, String.valueOf(value).getBytes());
        } catch (IOException e) {
            log("❌ Error al guardar configuración: " + e.getMessage());
        }
    }

    private int loadMaxInstances() {
        try {
            Path configPath = FilePathManager.getConfigFilePath();
            if (!Files.exists(configPath)) {
                return 2;
            }
            
            String content = new String(Files.readAllBytes(configPath)).trim();
            return Integer.parseInt(content);
        } catch (IOException | NumberFormatException e) {
            log("⚠️ Error al cargar configuración, usando valor por defecto: " + e.getMessage());
            return 2;
        }
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            log("⚠️ El sistema no soporta System Tray");
            return;
        }
        
        SystemTray systemTray = SystemTray.getSystemTray();
        Image trayIconImage = loadIcon("img/icono.png");
        
        // Si no se puede cargar el ícono personalizado, crear uno simple
        if (trayIconImage == null) {
            trayIconImage = createDefaultIcon();
        }
        
        trayIcon = new TrayIcon(trayIconImage, "Monitor de Procesos Cobol");
        trayIcon.setImageAutoSize(true);

        PopupMenu popupMenu = new PopupMenu();
        MenuItem openItem = new MenuItem("Abrir");
        openItem.addActionListener(e -> {
            if (authenticate()) {
                frame.setVisible(true);
                frame.setState(Frame.NORMAL); // Restaurar si está minimizado
                frame.toFront(); // Traer al frente
                frame.requestFocus(); // Solicitar el foco
            }
        });

        MenuItem exitItem = new MenuItem("Salir");
        exitItem.addActionListener(e -> {
            if (authenticate()) {
                InstanceManager.cleanup();
                System.exit(0);
            }
        });

        popupMenu.add(openItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);
        trayIcon.setPopupMenu(popupMenu);

        // Doble clic para abrir la ventana (con autenticación)
        trayIcon.addActionListener(e -> {
            if (authenticate()) {
                frame.setVisible(true);
                frame.setState(Frame.NORMAL);
                frame.toFront();
                frame.requestFocus();
            }
        });

        try {
            systemTray.add(trayIcon);
            log("✅ System Tray configurado correctamente");
        } catch (AWTException e) {
            log("❌ Error al configurar System Tray: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showTrayMessage(String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage("Monitor de Procesos", message, TrayIcon.MessageType.INFO);
        }
    }
    
    private Image createDefaultIcon() {
        // Crear un ícono simple si no se encuentra el archivo
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fondo azul
        g2d.setColor(Color.BLUE);
        g2d.fillOval(0, 0, size, size);
        
        // Texto "M" en blanco
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth("M")) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString("M", x, y);
        
        g2d.dispose();
        return image;
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll al final
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateButtons(boolean monitoring) {
        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(!monitoring);
            stopButton.setEnabled(monitoring);
        });
    }

    private Image loadIcon(String path) {
        try {
            // Intentar cargar desde resources
            java.net.URL resource = getClass().getResource("/" + path);
            if (resource != null) {
                return Toolkit.getDefaultToolkit().getImage(resource);
            }
            
            // Intentar cargar desde el directorio actual
            File iconFile = new File(path);
            if (iconFile.exists()) {
                return Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
            }
            
            log("⚠️ No se encontró el archivo de ícono: " + path);
            return null;
        } catch (Exception e) {
            log("❌ Error al cargar el ícono: " + e.getMessage());
            return null;
        }
    }
}