# Simulador de Semáforo en JavaFX

Aplicación JavaFX que simula un semáforo con una línea de tiempo animada, incluye un modo de juego de reacción y reglas declarativas de programación lógica para evaluar acciones según la luz.

## Requisitos
- JDK 21+ (probado con la configuración del `pom.xml`).
- Maven 3.9+
- Dependencias JavaFX gestionadas por Maven (`javafx-controls` y `javafx-fxml`).

## Estructura del proyecto
- `src/main/java/com/example/semaforo/TrafficLightApp.java`: arranque JavaFX con interfaz moderna, controles, timeline y paneles.
- `src/main/java/com/example/semaforo/TrafficLightLogic.java`: lógica funcional del cambio de estado.
- `src/main/java/com/example/semaforo/Light.java`: enum de estados RED, GREEN, YELLOW.
- `src/main/java/com/example/semaforo/GameLogic.java`: lógica del modo juego y función pura de puntaje.
- `src/main/java/com/example/semaforo/LightRules.java`: reglas declarativas estilo programación lógica.
- `src/main/resources/style.css`: estilos oscuros para la interfaz.

## Compilar y ejecutar con Maven
Desde la carpeta `SemaforoJavaFX`:

```bash
mvn clean package
mvn javafx:run
```

El plugin `javafx-maven-plugin` usa la clase principal `com.example.semaforo.TrafficLightApp`.

## Uso de la aplicación
1. **Simulación del semáforo**
   - Pulsa **Iniciar** para arrancar el `Timeline`. Cambia de RED → GREEN → YELLOW en bucle.
   - Ajusta el **Slider** para definir los segundos por estado; el `Timeline` se reconstruye con la nueva duración.
   - Botones **Pausar** y **Reanudar** controlan la animación.
   - El panel oscuro muestra las tres luces en un contenedor que simula el armazón del semáforo.

2. **Modo juego (“¡YA!”)**
   - Cuando creas que el semáforo está en verde, pulsa el botón **¡YA!**.
   - `GameLogic` guarda la marca de tiempo del último cambio a verde y calcula la diferencia con el clic.
   - La función pura `calcularPuntaje(long diferenciaMs)` reparte más puntos cuanto menor sea la reacción.
   - Se muestran aciertos, errores y puntaje acumulado en vivo.

3. **Programación lógica (LightRules)**
   - Selecciona una acción desde el ComboBox (avanzar, esperar, detenerse, cruzar) y pulsa **Evaluar acción**.
   - `LightRules` usa una base de conocimiento declarativa (`Regla`) y busca con streams la coincidencia de luz + acción; si no existe, devuelve "Desconocido".
   - Se muestran la luz actual, la acción evaluada, el resultado, la descripción de la regla aplicada y un historial (ListView) de inferencias generadas.

## Dónde se usa programación funcional
- `TrafficLightLogic#nextState` está implementado con una `UnaryOperator<Light>` que, dada una luz, devuelve siempre la siguiente. Es pura porque no depende de estado global ni produce efectos secundarios.
- `GameLogic#calcularPuntaje` es otra función pura: se basa únicamente en el tiempo de reacción que recibe como parámetro.

## Dónde se usa programación lógica
- `LightRules` almacena una base de conocimiento como lista de `Regla` (luz, acción, resultado, descripción) y la recorre con streams para inferir la consecuencia. El ListView muestra el historial de inferencias realizadas por el usuario.

## Timeline y ciclo de luces
- El `Timeline` tiene un `KeyFrame` que se dispara cada *n* segundos (valor del slider) y avanza el estado RED → GREEN → YELLOW → RED usando `TrafficLightLogic`.
- La reconstrucción del `Timeline` al mover el slider permite ajustar la duración sin reiniciar la aplicación.

## Capturas
Si tienes acceso a un entorno gráfico, ejecuta la aplicación y toma una captura de la ventana principal. (No se adjunta imagen aquí por limitaciones del entorno.)

## Créditos
Creado para demostrar combinación de JavaFX, programación funcional y lógica.
