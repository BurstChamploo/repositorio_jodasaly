package restrictor;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;

/**
 * @author
 * Neftaly Hardy
 */

public class ProcessMonitor {
    private static final String TARGET_PROCESS = "runcobol.exe";
    private ScheduledExecutorService scheduler;
    private ExecutorService threadPool;
    private ProcessManager processManager = new ProcessManager();
    private boolean monitoring = false;
    private int maxInstances = 2;
    private UIManager uiManager;

    public ProcessMonitor(UIManager uiManager, int maxInstances) {
        this.uiManager = uiManager;
        this.maxInstances = maxInstances; // Inicializar con el valor guardado
    }

    public void setMaxInstances(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    public void startMonitoring() {
        if (monitoring)
            return;
        monitoring = true;
        uiManager.log("Monitoreo iniciado...");
        uiManager.updateButtons(true);

        scheduler = Executors.newScheduledThreadPool(1);
        threadPool = Executors.newFixedThreadPool(10);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Integer> pids = processManager.getAllProcessIds(TARGET_PROCESS);
                if (pids.size() > maxInstances) {
                    for (int i = maxInstances; i < pids.size(); i++) {
                        int pid = pids.get(i);
                        uiManager.log("Cerrando instancia PID: " + pid);
                        threadPool.submit(new ProcessTerminate(pid, processManager));
                    }
                }
            } catch (Exception ex) {
                uiManager.log("Error al verificar procesos: " + ex.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
        if (!monitoring)
            return;
        monitoring = false;
        uiManager.log("Deteniendo monitoreo...");
        uiManager.updateButtons(false);

        scheduler.shutdown();

        new Thread(() -> {
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    uiManager.log("Forzando cierre de tareas pendientes...");
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                uiManager.log("Error al detener el monitoreo: " + ex.getMessage());
                threadPool.shutdownNow();
            }

            while (true) {
                List<Integer> pids = processManager.getAllProcessIds(TARGET_PROCESS);
                if (pids.size() <= maxInstances) {
                    SwingUtilities.invokeLater(() -> uiManager.log("Monitoreo detenido correctamente."));
                    break;
                }
            }
        }).start();
    }
}