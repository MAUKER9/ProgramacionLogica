# Semáforo JavaFX

Aplicación JavaFX simple que simula un semáforo con tres luces (roja, amarilla y verde) controladas por un temporizador ajustable.

## Requisitos

- JDK 17 o superior
- JavaFX SDK (por ejemplo, la carpeta descomprimida `javafx-sdk-21`)

## Compilación

1. Ubica la ruta al directorio `lib` de tu JavaFX SDK (por ejemplo `/ruta/a/javafx-sdk-21/lib`).
2. Desde la raíz del repositorio, compila la aplicación:

```bash
javac --module-path /ruta/a/javafx-sdk-21/lib \
      --add-modules javafx.controls,javafx.graphics \
      -d out SemaforoJavaFX/src/main/java/TrafficLightApp.java
```

Esto generará los binarios en el directorio `out`.

## Ejecución

Ejecuta la aplicación desde la raíz del repositorio utilizando el mismo `module-path`:

```bash
java --module-path /ruta/a/javafx-sdk-21/lib \
     --add-modules javafx.controls,javafx.graphics \
     -cp out TrafficLightApp
```

## Uso

- **Iniciar**: comienza el ciclo de luces desde rojo.
- **Pausar**: detiene temporalmente el cambio de luces.
- **Reanudar**: continúa el ciclo después de pausar.
- **Slider**: ajusta los segundos que dura cada luz antes de cambiar al siguiente estado.
