import javax.swing.SwingUtilities;
import vista.MenuInicio;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuInicio();
        });
    }
}
