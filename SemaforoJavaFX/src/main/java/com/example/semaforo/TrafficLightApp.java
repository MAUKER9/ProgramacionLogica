package com.example.semaforo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.GridPane;
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
    private Slider speedSlider;
    private Label stateLabel;
    private Label statusLabel;
    private Label aciertosLabel;
    private Label erroresLabel;
    private Label puntajeLabel;
    private Label logicLightLabel;
    private Label logicActionLabel;
    private Label logicResultLabel;
    private Label logicRuleLabel;
    private Label juegoLabel;
    private ComboBox<String> accionComboBox;
    private ObservableList<String> historialInferencias;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        redCircle = createLightCircle(Color.web("#d7263d"));
        yellowCircle = createLightCircle(Color.web("#f6c667"));
        greenCircle = createLightCircle(Color.web("#26c281"));

        speedSlider = createSpeedSlider();
        Label sliderLabel = new Label();
        sliderLabel.getStyleClass().add("accent-text");
        sliderLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(() -> {
                    double speed = speedSlider.getValue();
                    double seconds = secondsPerState(speed);
                    return String.format("Velocidad: %.1fx · %.1f s por luz", speed, seconds);
                }, speedSlider.valueProperty()));

        HBox controlButtons = createControls();
        VBox trafficLightBox = createTrafficLightBox();

        VBox simulationPanel = new VBox(14,
                titledLabel("Simulación"),
                buildContextRow(),
                trafficLightBox,
                sliderLabel,
                speedSlider,
                controlButtons,
                buildStatusRow());
        simulationPanel.getStyleClass().add("panel");
        simulationPanel.setPrefWidth(460);

        VBox gamePanel = createGamePanel();
        gamePanel.setPrefWidth(340);
        VBox logicPanel = createLogicPanel();

        HBox content = new HBox(18, simulationPanel, gamePanel);
        content.setAlignment(Pos.TOP_CENTER);

        VBox mainColumn = new VBox(20, createHero(), content, logicPanel);
        mainColumn.setAlignment(Pos.TOP_CENTER);
        mainColumn.setPadding(new Insets(26));
        mainColumn.setMaxWidth(1240);

        VBox centerWrapper = new VBox(mainColumn);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setPadding(new Insets(16));

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(0));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("app-scroll");

        Scene scene = new Scene(scrollPane, 1240, 820);
        scene.setFill(Color.web("#0f1116"));
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        stage.setTitle("Simulador de Semáforo");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(760);
        stage.show();

        configureTimeline();
        updateLightColors();
        updateStatus();
        updateScoreLabels();
        actualizarRegla();
    }

    private VBox createHero() {
        Label title = titledLabel("Simulador de Semáforo");
        Label subtitle = new Label("Simulación, lógica declarativa y mini-juego de reacción en una sola interfaz moderna.");
        subtitle.getStyleClass().add("subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(920);

        HBox badges = new HBox(10,
                badge("Programación funcional"),
                badge("Programación lógica"),
                badge("JavaFX UI"));
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox hero = new VBox(10, title, subtitle, badges);
        hero.getStyleClass().add("hero");
        hero.setAlignment(Pos.CENTER_LEFT);
        return hero;
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

    private HBox buildContextRow() {
        Label cycleInfo = new Label("Ciclo RED → GREEN → YELLOW");
        cycleInfo.getStyleClass().add("stat-chip");

        Label modeInfo = new Label("Cronometrado con Timeline");
        modeInfo.getStyleClass().add("stat-chip");

        Label speedInfo = new Label();
        speedInfo.getStyleClass().add("stat-chip");
        speedInfo.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(() -> {
                    double speed = speedSlider.getValue();
                    double seconds = secondsPerState(speed);
                    return String.format("Velocidad %.1fx (%.1f s)", speed, seconds);
                }, speedSlider.valueProperty()));

        HBox row = new HBox(10, cycleInfo, modeInfo, speedInfo);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox createGamePanel() {
        Button reactionButton = new Button("¡YA!");
        reactionButton.getStyleClass().add("primary-button");
        reactionButton.setOnAction(event -> registrarIntento());

        juegoLabel = new Label("Pulsa cuando la luz se ponga en verde.");
        aciertosLabel = new Label();
        erroresLabel = new Label();
        puntajeLabel = new Label();

        VBox statsRow = new VBox(6,
                labeledValue("Aciertos", aciertosLabel),
                labeledValue("Errores", erroresLabel),
                labeledValue("Puntaje", puntajeLabel));
        statsRow.getStyleClass().add("info-box");

        VBox gamePanel = new VBox(10,
                titledLabel("Modo Juego"),
                juegoLabel,
                reactionButton,
                statsRow);
        gamePanel.getStyleClass().add("panel");
        gamePanel.setAlignment(Pos.TOP_CENTER);
        return gamePanel;
    }

    private VBox createLogicPanel() {
        historialInferencias = FXCollections.observableArrayList();
        accionComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "avanzar", "esperar", "detenerse", "cruzar"));
        accionComboBox.getSelectionModel().selectFirst();

        Button evaluarButton = new Button("Evaluar acción");
        evaluarButton.getStyleClass().add("primary-button");
        evaluarButton.setOnAction(event -> evaluarAccionSeleccionada());

        logicLightLabel = createValueLabel();
        logicActionLabel = createValueLabel();
        logicResultLabel = createValueLabel();
        logicRuleLabel = createValueLabel();
        logicLightLabel.getStyleClass().add("value");
        logicActionLabel.getStyleClass().add("value");
        logicResultLabel.getStyleClass().add("value");
        logicRuleLabel.getStyleClass().add("value");

        ObservableList<String> conocimiento = FXCollections.observableArrayList();
        lightRules.getReglas().forEach(regla -> conocimiento.add(
                regla.luz().name().toLowerCase() + " + " + regla.accion() + " → " + regla.resultado()
                        + " — " + regla.descripcion()));

        ListView<String> knowledgeListView = createWrappedListView(conocimiento);
        knowledgeListView.setPrefHeight(240);
        knowledgeListView.setMinHeight(180);
        knowledgeListView.setPlaceholder(new Label("Sin reglas cargadas"));

        VBox knowledgeBox = new VBox(8,
                new Label("Base de conocimiento declarativa"),
                knowledgeListView);
        knowledgeBox.getStyleClass().add("knowledge-box");
        knowledgeBox.setPrefWidth(420);
        knowledgeBox.setMinWidth(360);
        knowledgeBox.setMaxWidth(520);

        GridPane detalleGrid = new GridPane();
        detalleGrid.setHgap(12);
        detalleGrid.setVgap(6);
        detalleGrid.add(rowLabel("Luz actual"), 0, 0);
        detalleGrid.add(logicLightLabel, 1, 0);
        detalleGrid.add(rowLabel("Acción seleccionada"), 0, 1);
        detalleGrid.add(logicActionLabel, 1, 1);
        detalleGrid.add(rowLabel("Resultado"), 0, 2);
        detalleGrid.add(logicResultLabel, 1, 2);
        detalleGrid.add(rowLabel("Regla aplicada"), 0, 3);
        detalleGrid.add(logicRuleLabel, 1, 3);
        detalleGrid.getStyleClass().add("info-grid");

        VBox resumenBox = new VBox(10,
                new Label("Detalle de inferencia"),
                detalleGrid);
        resumenBox.getStyleClass().add("info-box");
        resumenBox.setPrefWidth(420);
        resumenBox.setMinWidth(360);
        resumenBox.setMaxWidth(520);

        VBox accionBox = new VBox(10,
                new Label("Acción"),
                accionComboBox,
                evaluarButton);
        accionBox.setAlignment(Pos.CENTER_LEFT);
        accionBox.getStyleClass().add("info-box");
        accionBox.setPrefWidth(240);
        accionBox.setMinWidth(220);
        accionBox.setMaxWidth(320);

        ListView<String> historialListView = createWrappedListView(historialInferencias);
        historialListView.setPrefHeight(220);
        historialListView.setMinHeight(180);
        historialListView.setPlaceholder(new Label("Aún no hay inferencias"));
        VBox.setVgrow(historialListView, Priority.ALWAYS);

        VBox rightColumn = new VBox(12, resumenBox, accionBox);
        rightColumn.setFillWidth(true);
        rightColumn.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        HBox.setHgrow(resumenBox, Priority.ALWAYS);

        HBox infoRow = new HBox(18, knowledgeBox, rightColumn);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setFillHeight(true);
        HBox.setHgrow(knowledgeBox, Priority.SOMETIMES);

        Label subtitle = new Label("Reglas declarativas + inferencias en tiempo real");
        subtitle.getStyleClass().add("section-subtitle");

        VBox logicPanel = new VBox(12,
                titledLabel("Programación lógica"),
                subtitle,
                infoRow,
                new Label("Historial de inferencias"),
                historialListView);
        logicPanel.getStyleClass().add("panel");
        logicPanel.setAlignment(Pos.CENTER_LEFT);
        logicPanel.setMaxWidth(Double.MAX_VALUE);
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
        timeline = createTimeline(secondsPerState(speedSlider.getValue()));
    }

    private double secondsPerState(double speedFactor) {
        double clamped = Math.max(0.6, Math.min(6.0, speedFactor));
        return 3.0 / clamped;
    }

    private Timeline createTimeline(double secondsPerState) {
        KeyFrame frame = new KeyFrame(Duration.seconds(Math.max(0.3, secondsPerState)), event -> {
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
        rebuildTimelineWithDuration(secondsPerState(speedSlider.getValue()));
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

    private Slider createSpeedSlider() {
        Slider slider = new Slider(0.8, 6.0, 1.4);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(4);
        slider.setBlockIncrement(0.1);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> rebuildTimelineWithDuration(secondsPerState(newVal.doubleValue())));
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

    private void evaluarAccionSeleccionada() {
        evaluarReglaActual(true);
    }

    private void actualizarRegla() {
        evaluarReglaActual(false);
    }

    private void evaluarReglaActual(boolean registrarHistorial) {
        String accionSeleccionada = accionComboBox.getSelectionModel().getSelectedItem();
        LightRules.Regla regla = lightRules.evaluarAccion(currentLight, accionSeleccionada);

        logicLightLabel.setText("Luz actual: " + currentLight);
        logicActionLabel.setText("Acción seleccionada: " + regla.accion());
        logicResultLabel.setText("Resultado: " + regla.resultado());
        logicRuleLabel.setText("Regla aplicada: " + regla.descripcion());

        if (registrarHistorial) {
            historialInferencias.add("[Luz: " + currentLight + ", Acción: " + regla.accion()
                    + "] → " + regla.resultado() + " | " + regla.descripcion());
        }
    }

    private Label titledLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("heading");
        return label;
    }

    private Label rowLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("caption");
        return label;
    }

    private VBox labeledValue(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("caption");
        valueLabel.getStyleClass().add("value");
        return new VBox(2, label, valueLabel);
    }

    private ListView<String> createWrappedListView(ObservableList<String> items) {
        ListView<String> listView = new ListView<>(items);
        listView.setCellFactory(list -> new ListCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.getStyleClass().add("list-cell-label");
                label.maxWidthProperty().bind(listView.widthProperty().subtract(26));
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                }
            }
        });
        listView.widthProperty().addListener((obs, oldW, newW) -> listView.refresh());
        return listView;
    }

    private Label createValueLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(440);
        return label;
    }

    private Label badge(String text) {
        Label badge = new Label(text);
        badge.getStyleClass().add("badge");
        return badge;
    }
}
