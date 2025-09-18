import javax.swing.JOptionPane;
import java.math.BigInteger;

public class FactorialImperativoGUI {

    public static BigInteger factorialImperativo(int n) {
        BigInteger resultado = BigInteger.ONE;
        for (int i = 1; i <= n; i++) {
            resultado = resultado.multiply(BigInteger.valueOf(i));
        }
        return resultado;
    }

    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(null, "Ingresa un número para calcular su factorial:");

        try {
            int numero = Integer.parseInt(input);

            if (numero < 0) {
                JOptionPane.showMessageDialog(null, "El factorial no está definido para números negativos.");
                return;
            }

            BigInteger resultado = factorialImperativo(numero);
            JOptionPane.showMessageDialog(null, "El factorial de " + numero + " es:\n" + resultado);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Entrada inválida. Por favor ingresa un número entero.");
        }
    }
}
