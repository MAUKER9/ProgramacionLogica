package com.example.semaforo;

import java.util.Collections;
import java.util.Locale;
import java.util.List;

/**
 * Reglas declarativas (estilo programación lógica) para evaluar acciones
 * permitidas o no según el color de la luz.
 */
public class LightRules {

    public record Regla(Light luz, String accion, String resultado, String descripcion) {}

    private final List<Regla> reglas = List.of(
            new Regla(Light.RED, "avanzar", "Infracción", "rojo + avanzar → Infracción"),
            new Regla(Light.GREEN, "avanzar", "Permitido", "verde + avanzar → Permitido"),
            new Regla(Light.YELLOW, "avanzar", "Precaución", "amarillo + avanzar → Precaución"),
            new Regla(Light.RED, "esperar", "Correcto", "rojo + esperar → Correcto"),
            new Regla(Light.GREEN, "esperar", "Correcto", "verde + esperar → Correcto")
    );

    public Regla evaluarAccion(Light luz, String accion) {
        if (accion == null) {
            return new Regla(luz, null, "Desconocido", "No existe una regla para esta combinación");
        }
        String normalizada = accion.trim().toLowerCase(Locale.ROOT);
        if (normalizada.isEmpty()) {
            return new Regla(luz, normalizada, "Desconocido", "No existe una regla para esta combinación");
        }
        return reglas.stream()
                .filter(regla -> regla.luz() == luz && regla.accion().equals(normalizada))
                .findFirst()
                .orElse(new Regla(luz, normalizada, "Desconocido", "No existe una regla para esta combinación"));
    }

    public List<Regla> getReglas() {
        return Collections.unmodifiableList(reglas);
    }
}
