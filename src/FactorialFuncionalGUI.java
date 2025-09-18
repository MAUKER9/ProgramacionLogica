import javax.swing.JOptionPane;
import java.math.BigInteger;

public class FactorialFuncionalGUI {

    // Definición recursiva del factorial
    public static BigInteger factorialFuncional(int n) {
        if (n == 0 || n == 1) {
            return BigInteger.ONE;  // caso base
        } else {
            return BigInteger.valueOf(n).multiply(factorialFuncional(n - 1));
        }
    }

    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(null, "Ingresa un número para calcular su factorial (recursivo):");

        try {
            int numero = Integer.parseInt(input);

            if (numero < 0) {
                JOptionPane.showMessageDialog(null, "El factorial no está definido para números negativos.");
                return;
            }

            BigInteger resultado = factorialFuncional(numero);
            JOptionPane.showMessageDialog(null, "El factorial de " + numero + " es:\n" + resultado);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Entrada inválida. Por favor ingresa un número entero.");
        }
    }
}
