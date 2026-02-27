package launcher; // no cambie el nombre de este package

import org.update4j.LaunchContext;
import org.update4j.service.Launcher;
import menu.MenuJodasaly;

/**
 * @author
 * Daros Ledezma
 */

public class Lanzador implements Launcher{ // no cambie el nombre de esta clase
    public void run(LaunchContext ctx){
        MenuJodasaly.main(null);
    }
}
