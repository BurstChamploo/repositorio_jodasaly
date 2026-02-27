package restrictor;
import com.sun.jna.platform.win32.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * Neftaly Hardy
 */

public class ProcessManager {
    public List<Integer> getAllProcessIds(String processName) {
        List<Integer> pids = new ArrayList<>();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS,
                new WinDef.DWORD(0));
        Tlhelp32.PROCESSENTRY32 processEntry = new Tlhelp32.PROCESSENTRY32();

        while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry)) {
            if (processName.equalsIgnoreCase(new String(processEntry.szExeFile).trim())) {
                pids.add(processEntry.th32ProcessID.intValue());
            }
        }
        Kernel32.INSTANCE.CloseHandle(snapshot);
        return pids;
    }
}