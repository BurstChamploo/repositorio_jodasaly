package menu;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import restrictor.RestrictorDeVentanas;
import Activopago.MainTest;

/**
 * @author
 * Daros Ledezma
 */

public class MenuJodasaly {
    public static void main(String[] args){
        
        ImageIcon icono = new ImageIcon(new ImageIcon("img/menu.png").getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
        String[] opcion = {"Restrictor de ventanas", "Ventana Facturacion (MISP)"};
        String eleccion = (String)JOptionPane.showInputDialog(null, "Seleccione un aplicativo para ejecutar", "MENÚ JODASALY", JOptionPane.QUESTION_MESSAGE, icono, opcion, opcion[0]);
        switch (eleccion) {
            
            case "Restrictor de ventanas":
                RestrictorDeVentanas.main(args); // @author: Neftaly Hardy
                break;

            case "Ventana Facturacion (MISP)":
                MainTest.main(args);            // @author: Saul Abrego
                break;

            default:
                break;
        }
        
    }
}
