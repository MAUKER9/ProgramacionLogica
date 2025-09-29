package General;// Importamos la clase JOptionPane para mostrar cuadros de diálogo (ventanas emergentes).
import javax.swing.JOptionPane;

// Importamos la clase BigInteger, que permite manejar enteros muy grandes
// sin límite de tamaño (más allá de int o long).
import java.math.BigInteger;

// Definimos la clase pública principal del programa.
public class FactorialFuncionalGUI {

    // Método estático que calcula el factorial de un número usando recursión (estilo funcional).
    // Recibe un entero n y devuelve un BigInteger.
    public static BigInteger factorialFuncional(int n) {
        // Caso base: cuando n es 0 o 1, el factorial siempre es 1.
        if (n == 0 || n == 1) {
            return BigInteger.ONE;  // devolvemos 1 como BigInteger
        } else {
            // Caso recursivo: n! = n * (n-1)!
            // Se multiplica n (convertido a BigInteger) por el factorial de (n-1).
            return BigInteger.valueOf(n).multiply(factorialFuncional(n - 1));
        }
    }

    // Método principal: punto de entrada del programa.
    public static void main(String[] args) {
        // Mostramos un cuadro de diálogo que pide un número entero al usuario.
        String input = JOptionPane.showInputDialog(null, "Ingresa un número para calcular su factorial (recursivo):");

        // Bloque try-catch para manejar entradas no válidas.
        try {
            // Convertimos la entrada (String) a un número entero.
            int numero = Integer.parseInt(input);

            // Validamos que el número no sea negativo.
            if (numero < 0) {
                // Si es negativo, mostramos mensaje de error y salimos del programa.
                JOptionPane.showMessageDialog(null, "El factorial no está definido para números negativos.");
                return;
            }

            // Llamamos al método recursivo factorialFuncional para calcular el resultado.
            BigInteger resultado = factorialFuncional(numero);

            // Mostramos el resultado en un cuadro de diálogo.
            JOptionPane.showMessageDialog(null, "El factorial de " + numero + " es:\n" + resultado);

            // Capturamos el error si el usuario ingresó un valor no numérico (ejemplo: texto).
        } catch (NumberFormatException e) {
            // Mostramos un mensaje de error indicando que la entrada no es válida.
            JOptionPane.showMessageDialog(null, "Entrada inválida. Por favor ingresa un número entero.");
        }
    }
}
