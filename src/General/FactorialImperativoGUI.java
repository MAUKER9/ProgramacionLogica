package General;// Importamos la clase JOptionPane, que nos permite mostrar cuadros de diálogo (ventanas emergentes).
import javax.swing.JOptionPane;

// Importamos la clase BigInteger, que sirve para trabajar con números enteros muy grandes
// sin límite de tamaño (más allá de lo que soporta int o long).
import java.math.BigInteger;

// Declaramos la clase pública principal del programa.
// En Java, todo debe estar dentro de una clase.
public class FactorialImperativoGUI {

    // Método estático que calcula el factorial de un número de manera imperativa usando un bucle.
    // Recibe un entero n y devuelve un BigInteger (para soportar factoriales grandes).
    public static BigInteger factorialImperativo(int n) {
        // Inicializamos la variable resultado con el valor 1, representado como BigInteger.
        BigInteger resultado = BigInteger.ONE;

        // Bucle for que va desde 1 hasta n, multiplicando cada número en resultado.
        for (int i = 1; i <= n; i++) {
            // En cada iteración, multiplicamos el resultado por i (convertido a BigInteger).
            resultado = resultado.multiply(BigInteger.valueOf(i));
        }

        // Al terminar el bucle, devolvemos el valor calculado del factorial.
        return resultado;
    }

    // Método main: el punto de entrada del programa.
    public static void main(String[] args) {
        // Mostramos un cuadro de diálogo para pedir al usuario un número entero.
        // La entrada se guarda como texto (String).
        String input = JOptionPane.showInputDialog(null, "Ingresa un número para calcular su factorial:");

        // Usamos try-catch para manejar errores si el usuario ingresa algo que no es un número válido.
        try {
            // Convertimos el texto introducido en un número entero.
            int numero = Integer.parseInt(input);

            // Validamos que el número no sea negativo, porque el factorial no está definido en esos casos.
            if (numero < 0) {
                // Mostramos un mensaje de error y terminamos el programa con return.
                JOptionPane.showMessageDialog(null, "El factorial no está definido para números negativos.");
                return;
            }

            // Llamamos al método factorialImperativo para calcular el factorial del número.
            BigInteger resultado = factorialImperativo(numero);

            // Mostramos el resultado en una ventana emergente.
            JOptionPane.showMessageDialog(null, "El factorial de " + numero + " es:\n" + resultado);

            // Si el usuario ingresa un valor no numérico (ejemplo: letras), se captura la excepción.
        } catch (NumberFormatException e) {
            // Mostramos un mensaje de error indicando que la entrada no es válida.
            JOptionPane.showMessageDialog(null, "Entrada inválida. Por favor ingresa un número entero.");
        }
    }
}
