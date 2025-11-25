package com.example.semaforo;

/**
 * Lógica del modo de juego de reacción.
 */
public class GameLogic {

    private int aciertos;
    private int errores;
    private long puntajeTotal;
    private Long ultimoVerdeMs;

    public void reset() {
        aciertos = 0;
        errores = 0;
        puntajeTotal = 0;
        ultimoVerdeMs = null;
    }

    public void onLightChange(Light light, long timestampMs) {
        if (light == Light.GREEN) {
            ultimoVerdeMs = timestampMs;
        }
    }

    public AttemptResult registrarIntento(long timestampMs, Light currentLight) {
        if (currentLight == Light.GREEN && ultimoVerdeMs != null) {
            long diferencia = Math.max(0, timestampMs - ultimoVerdeMs);
            long puntos = calcularPuntaje(diferencia);
            aciertos++;
            puntajeTotal += puntos;
            return new AttemptResult(true, diferencia, puntos, aciertos, errores, puntajeTotal,
                    "¡Acierto! Reacción: " + diferencia + " ms (+" + puntos + ")");
        }

        errores++;
        return new AttemptResult(false, null, 0, aciertos, errores, puntajeTotal,
                "No era verde. Espera la siguiente.");
    }

    /**
     * Función pura de puntaje: solo depende del tiempo de reacción y no lee ni
     * modifica estado interno.
     */
    public long calcularPuntaje(long diferenciaMs) {
        long ventana = Math.max(0, 2000 - diferenciaMs);
        return ventana / 5;
    }

    public int getAciertos() {
        return aciertos;
    }

    public int getErrores() {
        return errores;
    }

    public long getPuntajeTotal() {
        return puntajeTotal;
    }

    public record AttemptResult(boolean acierto, Long reaccionMs, long puntos, int aciertos,
                                int errores, long puntajeTotal, String mensaje) {
    }
}
