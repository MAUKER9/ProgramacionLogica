package com.example.semaforo;

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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class TrafficLightApp extends Application {

    private final TrafficLightLogic trafficLightLogic = new TrafficLightLogic();
    private final GameLogic gameLogic = new GameLogic();
    private final LightRules lightRules = new LightRules();

    private Light currentLight = Light.RED;
    private Timeline timeline;

    private Circle redCircle;
    private Circle yellowCircle;
    private Circle greenCircle;
    private Slider durationSlider;
    private Label stateLabel;
    private Label statusLabel;
    private Label aciertosLabel;
    private Label erroresLabel;
    private Label puntajeLabel;
    private Label reglaLabel;
    private Label juegoLabel;
    private TextField accionInput;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        redCircle = createLightCircle(Color.web("#d7263d"));
        yellowCircle = createLightCircle(Color.web("#f6c667"));
        greenCircle = createLightCircle(Color.web("#26c281"));

        durationSlider = createDurationSlider();
        Label sliderLabel = new Label();
        sliderLabel.getStyleClass().add("accent-text");
        sliderLabel.textProperty().bind(durationSlider.valueProperty().asString("Segundos por luz: %.1f"));

        HBox controlButtons = createControls();
        VBox trafficLightBox = createTrafficLightBox();

        VBox simulationPanel = new VBox(12,
                titledLabel("Simulación"),
                trafficLightBox,
                sliderLabel,
                durationSlider,
                controlButtons,
                buildStatusRow());
        simulationPanel.getStyleClass().add("panel");

        VBox gamePanel = createGamePanel();
        VBox logicPanel = createLogicPanel();

        HBox content = new HBox(18, simulationPanel, gamePanel);
        content.setAlignment(Pos.TOP_CENTER);

        VBox root = new VBox(18, titledLabel("Simulador de Semáforo"), content, logicPanel);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 960, 560);
        scene.setFill(Color.web("#0f1116"));
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        stage.setTitle("Simulador de Semáforo");
        stage.setScene(scene);
        stage.show();

        configureTimeline();
        updateLightColors();
        updateStatus();
        updateScoreLabels();
    }

    private VBox createTrafficLightBox() {
        VBox lights = new VBox(12, redCircle, yellowCircle, greenCircle);
        lights.setAlignment(Pos.CENTER);

        VBox container = new VBox(lights);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(16));
        container.getStyleClass().add("traffic-box");
        return container;
    }

    private VBox createGamePanel() {
        Button reactionButton = new Button("¡YA!");
        reactionButton.getStyleClass().add("primary-button");
        reactionButton.setOnAction(event -> registrarIntento());

        juegoLabel = new Label("Pulsa cuando la luz se ponga en verde.");
        aciertosLabel = new Label();
        erroresLabel = new Label();
        puntajeLabel = new Label();

        VBox gamePanel = new VBox(10,
                titledLabel("Modo Juego"),
                juegoLabel,
                reactionButton,
                aciertosLabel,
                erroresLabel,
                puntajeLabel);
        gamePanel.getStyleClass().add("panel");
        gamePanel.setAlignment(Pos.TOP_CENTER);
        return gamePanel;
    }

    private VBox createLogicPanel() {
        accionInput = new TextField("avanzar");
        Button evaluarButton = new Button("Evaluar acción");
        evaluarButton.setOnAction(event -> actualizarRegla());
        reglaLabel = new Label();

        HBox accionBox = new HBox(8, new Label("Acción:"), accionInput, evaluarButton);
        accionBox.setAlignment(Pos.CENTER_LEFT);

        VBox logicPanel = new VBox(10,
                titledLabel("Programación lógica"),
                new Label("Reglas: rojo/avanzar → Infracción, verde/avanzar → Permitido, amarillo/avanzar → Precaución"),
                accionBox,
                reglaLabel);
        logicPanel.getStyleClass().add("panel");
        logicPanel.setAlignment(Pos.CENTER_LEFT);
        return logicPanel;
    }

    private HBox buildStatusRow() {
        stateLabel = new Label();
        statusLabel = new Label();
        HBox statusRow = new HBox(10, stateLabel, statusLabel);
        statusRow.setAlignment(Pos.CENTER);
        return statusRow;
    }

    private HBox createControls() {
        Button startButton = new Button("Iniciar");
        startButton.getStyleClass().add("primary-button");
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
            currentLight = trafficLightLogic.nextState(currentLight);
            gameLogic.onLightChange(currentLight, System.currentTimeMillis());
            updateLightColors();
            updateStatus();
            actualizarRegla();
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
        updateStatus();
        actualizarRegla();
        updateScoreLabels();
        timeline.playFromStart();
    }

    private void registrarIntento() {
        GameLogic.AttemptResult result = gameLogic.registrarIntento(System.currentTimeMillis(), currentLight);
        juegoLabel.setText(result.mensaje());
        updateScoreLabels();
    }

    private Slider createDurationSlider() {
        Slider slider = new Slider(1, 10, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setBlockIncrement(0.5);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> rebuildTimelineWithDuration(newVal.doubleValue()));
        return slider;
    }

    private Circle createLightCircle(Color activeColor) {
        Circle circle = new Circle(45);
        circle.getStyleClass().add("traffic-light");
        circle.setStroke(Color.web("#0d0f14"));
        circle.setStrokeWidth(3);
        circle.setUserData(activeColor);
        circle.setFill(dimColor(activeColor));
        return circle;
    }

    private void updateLightColors() {
        setCircleState(redCircle, currentLight == Light.RED);
        setCircleState(yellowCircle, currentLight == Light.YELLOW);
        setCircleState(greenCircle, currentLight == Light.GREEN);
    }

    private void setCircleState(Circle circle, boolean active) {
        Color base = (Color) circle.getUserData();
        circle.setFill(active ? base : dimColor(base));
    }

    private Color dimColor(Color color) {
        return color.darker().darker();
    }

    private void updateStatus() {
        stateLabel.setText("Estado: " + currentLight);
        switch (currentLight) {
            case GREEN -> statusLabel.setText("¡Es verde! Puedes avanzar.");
            case YELLOW -> statusLabel.setText("Amarillo: precaución.");
            case RED -> statusLabel.setText("Rojo: detente.");
        }
    }

    private void updateScoreLabels() {
        aciertosLabel.setText("Aciertos: " + gameLogic.getAciertos());
        erroresLabel.setText("Errores: " + gameLogic.getErrores());
        puntajeLabel.setText("Puntaje: " + gameLogic.getPuntajeTotal());
    }

    private void actualizarRegla() {
        String accion = accionInput.getText();
        String resultado = lightRules.evaluarAccion(currentLight, accion);
        reglaLabel.setText("Resultado acción '" + accion + "' con luz " + currentLight + ": " + resultado);
    }

    private Label titledLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("heading");
        return label;
    }
}
