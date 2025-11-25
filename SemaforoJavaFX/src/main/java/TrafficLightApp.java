import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.UnaryOperator;

public class TrafficLightApp extends Application {
    public enum Light {
        RED,
        YELLOW,
        GREEN
    }

    private final UnaryOperator<Light> nextStateFunction = light -> switch (light) {
        case RED -> Light.GREEN;
        case GREEN -> Light.YELLOW;
        case YELLOW -> Light.RED;
    };

    private Light currentLight = Light.RED;
    private Timeline timeline;
    private final GameLogic gameLogic = new GameLogic();

    private Circle redCircle;
    private Circle yellowCircle;
    private Circle greenCircle;
    private Slider durationSlider;
    private Label aciertosLabel;
    private Label erroresLabel;
    private Label puntajeLabel;
    private Label statusLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        redCircle = createLightCircle(Color.RED);
        yellowCircle = createLightCircle(Color.YELLOW);
        greenCircle = createLightCircle(Color.LIMEGREEN);

        VBox lightBox = new VBox(10, redCircle, yellowCircle, greenCircle);
        lightBox.setAlignment(Pos.CENTER);

        durationSlider = createDurationSlider();
        Label sliderLabel = new Label();
        sliderLabel.textProperty().bind(durationSlider.valueProperty().asString("Segundos por luz: %.1f"));

        HBox controls = createControls();

        Button reactionButton = new Button("¡Luz verde!");
        reactionButton.setOnAction(event -> registrarIntento());

        statusLabel = new Label("Pulsa cuando la luz sea VERDE.");
        aciertosLabel = new Label();
        erroresLabel = new Label();
        puntajeLabel = new Label();
        updateScoreLabels();

        VBox root = new VBox(20, lightBox, sliderLabel, durationSlider, controls, reactionButton, statusLabel, aciertosLabel, erroresLabel, puntajeLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        configureTimeline();
        updateLightColors();

        durationSlider.valueProperty().addListener((obs, oldVal, newVal) -> rebuildTimelineWithDuration(newVal.doubleValue()));

        Scene scene = new Scene(root, 320, 420);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Semáforo JavaFX");
        primaryStage.show();
    }

    private HBox createControls() {
        Button startButton = new Button("Iniciar");
        Button pauseButton = new Button("Pausar");
        Button resumeButton = new Button("Reanudar");

        startButton.setOnAction(event -> startTimeline());
        pauseButton.setOnAction(event -> {
            if (timeline != null) {
                timeline.pause();
            }
        });
        resumeButton.setOnAction(event -> {
            if (timeline != null) {
                timeline.play();
            }
        });

        HBox controls = new HBox(10, startButton, pauseButton, resumeButton);
        controls.setAlignment(Pos.CENTER);
        return controls;
    }

    private void configureTimeline() {
        timeline = createTimeline(durationSlider.getValue());
    }

    private Timeline createTimeline(double secondsPerState) {
        KeyFrame frame = new KeyFrame(Duration.seconds(secondsPerState), event -> {
            currentLight = nextState(currentLight);
            gameLogic.onLightChange(currentLight, System.currentTimeMillis());
            updateLightColors();
            updateStatusLabel();
        });

        Timeline newTimeline = new Timeline(frame);
        newTimeline.setCycleCount(Animation.INDEFINITE);
        return newTimeline;
    }

    private void rebuildTimelineWithDuration(double secondsPerState) {
        Animation.Status previousStatus = timeline != null ? timeline.getStatus() : Animation.Status.STOPPED;
        if (timeline != null) {
            timeline.stop();
        }
        timeline = createTimeline(secondsPerState);

        if (previousStatus == Animation.Status.RUNNING) {
            timeline.play();
        } else if (previousStatus == Animation.Status.PAUSED) {
            timeline.pause();
        }
    }

    private void startTimeline() {
        if (timeline == null) {
            configureTimeline();
        }
        currentLight = Light.RED;
        gameLogic.reset();
        updateLightColors();
        updateStatusLabel();
        updateScoreLabels();
        timeline.playFromStart();
    }

    public Light nextState(Light actual) {
        return nextStateFunction.apply(actual);
    }

    private Slider createDurationSlider() {
        Slider slider = new Slider(1, 10, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setBlockIncrement(0.5);
        return slider;
    }

    private Circle createLightCircle(Color color) {
        Circle circle = new Circle(50);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        circle.setFill(color.darker().darker());
        return circle;
    }

    private void updateLightColors() {
        setCircleState(redCircle, currentLight == Light.RED, Color.RED);
        setCircleState(yellowCircle, currentLight == Light.YELLOW, Color.YELLOW);
        setCircleState(greenCircle, currentLight == Light.GREEN, Color.LIMEGREEN);
    }

    private void setCircleState(Circle circle, boolean active, Color color) {
        if (circle == null) {
            return;
        }
        circle.setFill(active ? color : color.darker().darker());
    }

    private void registrarIntento() {
        GameLogic.AttemptResult result = gameLogic.registrarIntento(System.currentTimeMillis(), currentLight);
        updateScoreLabels();
        statusLabel.setText(result.message());
    }

    private void updateStatusLabel() {
        if (currentLight == Light.GREEN) {
            statusLabel.setText("¡Es VERDE! Pulsa el botón para sumar puntos.");
        } else if (currentLight == Light.YELLOW) {
            statusLabel.setText("Amarillo: prepárate para el rojo.");
        } else {
            statusLabel.setText("Rojo: espera el verde.");
        }
    }

    private void updateScoreLabels() {
        aciertosLabel.setText("Aciertos: " + gameLogic.getAciertos());
        erroresLabel.setText("Errores: " + gameLogic.getErrores());
        puntajeLabel.setText("Puntaje: " + gameLogic.getPuntajeTotal());
    }
}

class GameLogic {
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

    public void onLightChange(TrafficLightApp.Light light, long timestampMs) {
        if (light == TrafficLightApp.Light.GREEN) {
            ultimoVerdeMs = timestampMs;
        }
    }

    public AttemptResult registrarIntento(long timestampMs, TrafficLightApp.Light currentLight) {
        if (currentLight == TrafficLightApp.Light.GREEN && ultimoVerdeMs != null) {
            long diferencia = Math.max(0, timestampMs - ultimoVerdeMs);
            long puntos = calcularPuntaje(diferencia);
            aciertos++;
            puntajeTotal += puntos;
            return new AttemptResult(true, aciertos, errores, puntajeTotal, "¡Acierto! (+" + puntos + ") reacción: " + diferencia + " ms");
        }

        errores++;
        return new AttemptResult(false, aciertos, errores, puntajeTotal, "No es verde, pierdes oportunidad.");
    }

    public long calcularPuntaje(long diferenciaMs) {
        long base = Math.max(0, 2000 - diferenciaMs);
        return base / 10;
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

    record AttemptResult(boolean acierto, int aciertos, int errores, long puntaje, String message) {
    }
}
