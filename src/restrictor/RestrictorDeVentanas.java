package restrictor;
import javax.swing.*;

/**
 * @author
 * Neftaly Hardy
 */

public class RestrictorDeVentanas {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (!InstanceManager.ensureSingleInstance()) {
                JOptionPane.showMessageDialog(null, "La aplicación ya está en ejecución.");
                System.exit(0);
            }
            new UIManager();
        });
    }
}