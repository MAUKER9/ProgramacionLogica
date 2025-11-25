package com.example.semaforo;

import java.util.function.UnaryOperator;

/**
 * Lógica funcional del semáforo. La función {@link #nextState(Light)} es pura
 * porque para una entrada dada (el estado actual) siempre devuelve el mismo
 * resultado, sin modificar estado interno ni depender de variables externas.
 */
public class TrafficLightLogic {

    private final UnaryOperator<Light> nextStateFunction = light -> switch (light) {
        case RED -> Light.GREEN;
        case GREEN -> Light.YELLOW;
        case YELLOW -> Light.RED;
    };

    public Light nextState(Light actual) {
        return nextStateFunction.apply(actual);
    }

    public UnaryOperator<Light> getNextStateFunction() {
        return nextStateFunction;
    }
}
