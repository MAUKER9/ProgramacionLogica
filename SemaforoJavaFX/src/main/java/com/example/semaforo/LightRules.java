package com.example.semaforo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Reglas declarativas (estilo programación lógica) para evaluar acciones
 * permitidas o no según el color de la luz.
 */
public class LightRules {

    private final Map<Light, Map<String, String>> reglas = new EnumMap<>(Light.class);

    public LightRules() {
        Map<String, String> rojo = new HashMap<>();
        rojo.put("avanzar", "Infracción");
        rojo.put("detenerse", "Correcto");

        Map<String, String> verde = new HashMap<>();
        verde.put("avanzar", "Permitido");
        verde.put("detenerse", "Puedes avanzar con seguridad");

        Map<String, String> amarillo = new HashMap<>();
        amarillo.put("avanzar", "Precaución");
        amarillo.put("detenerse", "Recomendado");

        reglas.put(Light.RED, Map.copyOf(rojo));
        reglas.put(Light.GREEN, Map.copyOf(verde));
        reglas.put(Light.YELLOW, Map.copyOf(amarillo));
    }

    public String evaluarAccion(Light luz, String accion) {
        if (accion == null) {
            return "Sin regla definida";
        }
        String normalizada = accion.trim().toLowerCase(Locale.ROOT);
        if (normalizada.isEmpty()) {
            return "Sin regla definida";
        }
        return reglas.getOrDefault(luz, Collections.emptyMap())
                .getOrDefault(normalizada, "Sin regla definida");
    }

    public Map<Light, Map<String, String>> getReglas() {
        return Collections.unmodifiableMap(reglas);
    }
}
