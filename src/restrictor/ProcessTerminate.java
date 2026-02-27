package restrictor;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author
 * Neftaly Hardy
 */

public class ProcessTerminate implements Runnable {
    private final int pid;
    private final ProcessManager processManager; // Dependencia inyectada

    public ProcessTerminate(int pid, ProcessManager processManager) {
        this.pid = pid;
        this.processManager = processManager;
    }

    @Override
    public void run() {
        closeProcessSafely();
    }

    private void closeProcessSafely() {
        List<WinDef.HWND> windows = getProcessWindows();
        closeWindows(windows);

        if (isProcessRunning()) {
            System.out.println("El proceso " + pid + " sigue en ejecución, cerrándolo de manera segura...");
            forceTerminateProcess();
        }
    }

    private List<WinDef.HWND> getProcessWindows() {
        List<WinDef.HWND> windows = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            IntByReference processId = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, processId);
            if (processId.getValue() == pid) {
                windows.add(hWnd);
            }
            return true;
        }, null);
        return windows;
    }

    private void closeWindows(List<WinDef.HWND> windows) {
        if (!windows.isEmpty()) {
            Collections.reverse(windows);
            for (WinDef.HWND hWnd : windows) {
                User32.INSTANCE.PostMessage(hWnd, WinUser.WM_CLOSE, new WinDef.WPARAM(0), new WinDef.LPARAM(0));
                System.out.println("Se envió solicitud de cierre a ventana del PID " + pid);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private boolean isProcessRunning() {
        List<Integer> pids = processManager.getAllProcessIds("runcobol.exe");
        return pids.contains(pid);
    }

    private void forceTerminateProcess() {
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_TERMINATE, false, pid);
        if (processHandle != null) {
            Kernel32.INSTANCE.TerminateProcess(processHandle, 0);
            Kernel32.INSTANCE.CloseHandle(processHandle);
            System.out.println("Proceso " + pid + " finalizado de manera segura.");
        } else {
            System.out.println("No se pudo abrir el proceso " + pid + " para finalizarlo.");
        }
    }
}
