package cola;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Cola de clientes con núcleo FUNCIONAL e interfaz Swing.
 * (Diseño de UI mejorado; lógica intacta)
 *
 * [PA06] En este proyecto se aplica el modelo de programación funcional:
 *   - **Estructuras inmutables**: Lista, Pila, Cola y State no mutan; se crean nuevos valores.
 *   - **Funciones puras**: encolar, atender, limpiar, promedioEspera, estimadoSiguiente.
 *   - **Recursión**: Lista.tam() e invertir() usan recursión (incluida recursión de cola).
 *   - **Composición**: Cola funcional implementada con dos listas (front/back).
 *   - **Separación**: la GUI (Swing) solo refleja el estado; el núcleo no depende de la UI.
 */
public class ColaClientesProGUI extends JFrame {

    /* ================== Núcleo funcional (estructuras) ================== */

    // -------- Lista enlazada inmutable --------
    private interface Lista<T> {
        boolean esVacia();
        T cabeza();                  // throws si vacía
        Lista<T> cola();             // throws si vacía
        int tam();                   // [PA06] Recursión estructural
        Lista<T> agregarInicio(T x); // O(1)
        Lista<T> invertir();         // [PA06] Recursión de cola (tail recursion)

        // [PA06] Conversión a lista de Java sin mutar la estructura original
        default java.util.List<T> aJavaList() {
            var out = new ArrayList<T>();
            var it = this;
            while (!it.esVacia()) { out.add(it.cabeza()); it = it.cola(); }
            return out;
        }
        static <T> Lista<T> vacia() { return new Vacia<>(); }
    }

    // [PA06] Implementación inmutable que representa la lista vacía
    private static final class Vacia<T> implements Lista<T> {
        public boolean esVacia() { return true; }
        public T cabeza() { throw new IllegalStateException("Lista vacía"); }
        public Lista<T> cola() { throw new IllegalStateException("Lista vacía"); }
        public int tam() { return 0; }
        public Lista<T> agregarInicio(T x) { return new Nodo<>(x, this); }
        public Lista<T> invertir() { return this; }
        public String toString() { return "[]"; }
    }

    // [PA06] Nodo inmutable (campos final, sin setters)
    private static final class Nodo<T> implements Lista<T> {
        private final T h; private final Lista<T> t;
        Nodo(T h, Lista<T> t) { this.h = h; this.t = t; }
        public boolean esVacia() { return false; }
        public T cabeza() { return h; }
        public Lista<T> cola() { return t; }

        // [PA06] Recursión: tamaño = 1 + tamaño de la cola
        public int tam() { return 1 + t.tam(); }

        public Lista<T> agregarInicio(T x) { return new Nodo<>(x, this); }

        // [PA06] Recursión de cola para invertir sin mutar
        public Lista<T> invertir() { return inv(this, Lista.vacia()); }
        private static <T> Lista<T> inv(Lista<T> src, Lista<T> acc) {
            return src.esVacia() ? acc : inv(src.cola(), new Nodo<>(src.cabeza(), acc));
        }
    }

    // -------- Pila funcional (para Undo) --------
    // [PA06] Estructura inmutable (push/pop crean nuevas pilas). Soporta "deshacer" sin efectos colaterales.
    private static final class Pila<T> {
        private final Lista<T> lista;
        private Pila(Lista<T> l) { this.lista = l; }
        public static <T> Pila<T> vacia() { return new Pila<>(Lista.vacia()); }
        public Pila<T> push(T x) { return new Pila<>(lista.agregarInicio(x)); }        // [PA06] inmutabilidad
        public boolean esVacia() { return lista.esVacia(); }
        public Optional<T> peek() { return esVacia() ? Optional.empty() : Optional.of(lista.cabeza()); }

        // [PA06] pop devuelve una nueva Pila y el valor extraído (sin mutar la original)
        public ResultadoPop<T> pop() {
            if (esVacia()) return new ResultadoPop<>(this, Optional.empty());
            return new ResultadoPop<>(new Pila<>(lista.cola()), Optional.of(lista.cabeza()));
        }
        private static final class ResultadoPop<T> {
            final Pila<T> pila; final Optional<T> valor;
            ResultadoPop(Pila<T> pila, Optional<T> valor) { this.pila = pila; this.valor = valor; }
            Pila<T> pila() { return pila; } Optional<T> valor() { return valor; }
        }
    }

    // -------- Cola funcional (dos pilas/listas) --------
    // [PA06] Implementación clásica: front para desencolar, back acumulada para encolar; sin mutabilidad.
    public static final class Cola<T> {
        private final Lista<T> front, back;
        private Cola(Lista<T> front, Lista<T> back) { this.front = front; this.back = back; }
        public static <T> Cola<T> vacia() { return new Cola<>(Lista.vacia(), Lista.vacia()); }
        public boolean esVacia() { return front.esVacia() && back.esVacia(); }
        public int tam() { return front.tam() + back.tam(); } // [PA06] Computación derivada, sin estado mutable
        public Cola<T> encolar(T x) { return new Cola<>(front, back.agregarInicio(x)); } // [PA06] regresa nueva cola

        // [PA06] Desencolar es puro: puede invertir back -> front cuando es necesario, devolviendo nueva cola + valor
        public ResultadoDesencolar<T> desencolar() {
            if (esVacia()) return new ResultadoDesencolar<>(this, Optional.empty());
            if (!front.esVacia()) {
                Nodo<T> f = (Nodo<T>) front;
                return new ResultadoDesencolar<>(new Cola<>(f.cola(), back), Optional.of(f.cabeza()));
            }
            // front vacío: pasamos back invertida a front (sin mutar back original)
            Lista<T> nf = back.invertir();
            Nodo<T> f = (Nodo<T>) nf;
            return new ResultadoDesencolar<>(new Cola<>(f.cola(), Lista.vacia()), Optional.of(f.cabeza()));
        }

        public Optional<T> primero() {
            if (!front.esVacia()) return Optional.of(front.cabeza());
            if (!back.esVacia())  return Optional.of(back.invertir().cabeza());
            return Optional.empty();
        }

        // [PA06] Vista FIFO sin alterar la estructura
        public java.util.List<T> comoListaFIFO() {
            var l = new ArrayList<T>();
            Lista<T> it = front;
            while (!it.esVacia()) { l.add(it.cabeza()); it = it.cola(); }
            it = back.invertir();
            while (!it.esVacia()) { l.add(it.cabeza()); it = it.cola(); }
            return l;
        }
        public static final class ResultadoDesencolar<T> {
            private final Cola<T> cola; private final Optional<T> valor;
            public ResultadoDesencolar(Cola<T> cola, Optional<T> valor) { this.cola = cola; this.valor = valor; }
            public Cola<T> cola() { return cola; } public Optional<T> valor() { return valor; }
        }
    }

    /* ====================== Dominio + Estado puro ====================== */

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // [PA06] Entidad inmutable de dominio
    private static final class Cliente {
        final String nombre, turno; final LocalDateTime llegada;
        Cliente(String nombre, String turno, LocalDateTime llegada) { this.nombre = nombre; this.turno = turno; this.llegada = llegada; }
        @Override public String toString() { return String.format("%s (Turno %s - %s)", nombre, turno, HORA.format(llegada)); }
    }

    // [PA06] Registro de atención inmutable, derivado de dos tiempos
    private static final class Atendido {
        final Cliente cliente; final LocalDateTime atendido; final Duration espera;
        Atendido(Cliente c, LocalDateTime t, Duration e) { this.cliente = c; this.atendido = t; this.espera = e; }
    }

    // [PA06] Estado global inmutable del sistema (tupla de valores)
    private static final class State {
        final Cola<Cliente> cola; final List<Atendido> atendidos; final int consecutivo;
        State(Cola<Cliente> cola, List<Atendido> atendidos, int consecutivo) {
            this.cola = cola; this.atendidos = List.copyOf(atendidos); this.consecutivo = consecutivo;
        }
        static State inicial() { return new State(Cola.vacia(), List.of(), 1); }
    }

    // -------------------- Operaciones puras sobre State --------------------

    // [PA06] encolar: función pura. Entrada (State, datos) -> salida (nuevo State); no efectos colaterales.
    private static State encolar(State s, String nombre, LocalDateTime ahora) {
        String turno = String.format("%03d", s.consecutivo);
        Cliente c = new Cliente(nombre, turno, ahora);
        return new State(s.cola.encolar(c), s.atendidos, s.consecutivo + 1);
    }

    // [PA06] atender: pura; calcula espera y devuelve State actualizado + dato derivado
    private static ResultadoAtender atender(State s, LocalDateTime ahora) {
        Cola.ResultadoDesencolar<Cliente> res = s.cola.desencolar();
        if (res.valor().isEmpty()) return new ResultadoAtender(s, Optional.empty());
        Cliente cli = res.valor().get();
        Duration espera = Duration.between(cli.llegada, ahora);
        Atendido at = new Atendido(cli, ahora, espera);
        var nueva = new ArrayList<>(s.atendidos); nueva.add(at);
        return new ResultadoAtender(new State(res.cola(), nueva, s.consecutivo), Optional.of(at));
    }

    // [PA06] limpiar: regresa el mismo estado inicial (puro)
    private static State limpiar(State s) { return State.inicial(); }

    private static final class ResultadoAtender {
        final State state; final Optional<Atendido> atendido;
        ResultadoAtender(State s, Optional<Atendido> a) { this.state = s; this.atendido = a; }
    }

    // -------------------- Métricas puras (sin estado mutable) --------------------

    private static String formato(Duration d) {
        long m = d.getSeconds() / 60, s = d.getSeconds() % 60;
        return String.format("%dm %02ds", m, s);
    }

    // [PA06] promedioEspera: reduce la lista a un agregado; pura
    private static Optional<Duration> promedioEspera(State s) {
        if (s.atendidos.isEmpty()) return Optional.empty();
        long total = 0; for (Atendido a : s.atendidos) total += a.espera.getSeconds();
        return Optional.of(Duration.ofSeconds(total / s.atendidos.size()));
    }

    // [PA06] estimadoSiguiente: cálculo derivado del estado (puro)
    private static Optional<Duration> estimadoSiguiente(State s, LocalDateTime ahora) {
        return s.cola.primero().map(c -> Duration.between(c.llegada, ahora));
    }

    /* ============================= GUI ============================= */
    // [PA06] La GUI solo orquesta eventos y muestra datos; no muta estructuras internas directamente.

    private State state = State.inicial();
    private Pila<State> undo = Pila.vacia(); // [PA06] Undo funcional: pila de estados

    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modelo);

    private final JTextField txtNombre = new JTextField();
    private final JLabel lblSig = new JLabel("Siguiente: —");
    private final JLabel lblEnFila = new JLabel("En fila: 0");
    private final JLabel lblAtendidos = new JLabel("Atendidos: 0");
    private final JLabel lblProm = new JLabel("Prom. espera: —");
    private final JLabel lblEst = new JLabel("Espera del siguiente: —");

    private final JTextArea mensajes = new JTextArea(3, 20);

    // ======== Constructor: SOLO DISEÑO (núcleo funcional queda intacto) ========
    public ColaClientesProGUI() {
        super("Cola de clientes — Ventanilla (Funcional)");

        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
        Font base = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("Label.font", base);
        UIManager.put("Button.font", base.deriveFont(Font.BOLD));
        UIManager.put("TextField.font", base);
        UIManager.put("TextArea.font", new Font(Font.MONOSPACED, Font.PLAIN, 12));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(940, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));

        add(panelTop(), BorderLayout.NORTH);

        // [PA06] La vista se alimenta del estado, pero no lo altera directamente
        JScrollPane centerScroll = new JScrollPane(lista);
        JScrollPane logScroll = new JScrollPane(mensajes);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                box("Clientes en espera (frente arriba)", centerScroll),
                box("Mensajes", logScroll));
        split.setResizeWeight(0.72);
        add(split, BorderLayout.CENTER);

        lista.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        lista.setCellRenderer(new ZebraRenderer());
        mensajes.setEditable(false);
        mensajes.setLineWrap(true);
        mensajes.setWrapStyleWord(true);

        // Atajos de teclado (eventos)
        mapKeyStroke("ENTER", 0, this::onAgregar);
        mapKeyStroke("D", InputEvent.CTRL_DOWN_MASK, this::onAtender);
        mapKeyStroke("Z", InputEvent.CTRL_DOWN_MASK, this::onUndo);

        refrescar();
    }

    private JPanel panelTop() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(6, 8, 6, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton btnAgregar = new JButton("➕  Agregar cliente (Enter)");
        JButton btnAtender = new JButton("⏭  Atender (Ctrl+D)");
        JButton btnUndo    = new JButton("↩  Deshacer (Ctrl+Z)");
        JButton btnReset   = new JButton("⟲  Reiniciar");
        JButton btnCSV     = new JButton("⬇  Exportar atendidos CSV");

        btnAgregar.setToolTipText("Agrega el nombre a la cola (Enter)");
        btnAtender.setToolTipText("Atiendes al primero en la fila (Ctrl+D)");
        btnUndo.setToolTipText("Deshace la última acción (Ctrl+Z)");
        btnCSV.setToolTipText("Genera un CSV legible con tiempos de espera");

        c.gridx = 0; c.gridy = 0; c.weightx = 0; p.add(new JLabel("Nombre:"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        txtNombre.setPreferredSize(new Dimension(260, 30));
        p.add(txtNombre, c);

        c.gridx = 0; c.gridy = 1; p.add(btnAgregar, c);
        c.gridx = 1; c.gridy = 1; p.add(btnAtender, c);

        c.gridx = 0; c.gridy = 2; p.add(btnUndo, c);
        c.gridx = 1; c.gridy = 2; p.add(btnReset, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; p.add(btnCSV, c);

        JPanel metrics = new JPanel(new GridLayout(5, 1, 6, 4));
        metrics.setBorder(BorderFactory.createTitledBorder("Métricas"));
        for (JLabel lab : List.of(lblSig, lblEnFila, lblAtendidos, lblProm, lblEst)) {
            lab.setFont(lab.getFont().deriveFont(Font.BOLD));
            metrics.add(lab);
        }
        c.gridx = 2; c.gridy = 0; c.gridheight = 4; c.weightx = 0.7;
        c.fill = GridBagConstraints.BOTH;
        p.add(metrics, c);

        // [PA06] Los listeners invocan funciones puras para producir un NUEVO estado
        btnAgregar.addActionListener(_ -> onAgregar());
        btnAtender.addActionListener(_ -> onAtender());
        btnUndo.addActionListener(_ -> onUndo());
        btnReset.addActionListener(_ -> onReset());
        btnCSV.addActionListener(_ -> onExportCSV());
        txtNombre.addActionListener(_ -> onAgregar());

        return box("Controles", p);
    }

    private JPanel centerPanel() {  // conservado por compatibilidad
        return box("Clientes en espera (frente arriba)", new JScrollPane(lista));
    }

    private JPanel box(String titulo, JComponent inner) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    private void mapKeyStroke(String key, int mods, Runnable action) {
        InputMap input = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap act = getRootPane().getActionMap();
        KeyStroke ks = key.equals("ENTER")
                ? KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, mods)
                : KeyStroke.getKeyStroke(key.charAt(0), mods);
        String name = key + mods;
        input.put(ks, name);
        act.put(name, new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); }});
    }

    /* ============================ Acciones (orquestan el núcleo) ============================ */

    private void onAgregar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isBlank()) { log("Nombre vacío, no se agrega."); return; }
        undo = undo.push(state);                        // [PA06] guardamos estado previo en Pila<State>
        state = encolar(state, nombre, LocalDateTime.now());   // [PA06] transición pura de estado
        List<Cliente> fifo = state.cola.comoListaFIFO();
        log("Entra: " + fifo.get(fifo.size()-1));
        txtNombre.setText("");
        refrescar();
    }

    private void onAtender() {
        undo = undo.push(state);                       // [PA06] soporte de deshacer por inmutabilidad
        ResultadoAtender res = atender(state, LocalDateTime.now()); // [PA06] función pura
        state = res.state;
        if (res.atendido.isPresent()) {
            Atendido a = res.atendido.get();
            log("Atendido: " + a.cliente + " | espera " + formato(a.espera));
        } else {
            log("No hay clientes en fila.");
            undo = undo.pop().pila(); // no guardamos un undo inútil
        }
        refrescar();
    }

    private void onUndo() {
        Pila.ResultadoPop<State> res = undo.pop();
        if (res.valor().isEmpty()) { log("Nada que deshacer."); return; }
        state = res.valor().get();    // [PA06] restaurar estado anterior es trivial por inmutabilidad
        undo = res.pila();
        log("Deshacer: restaurado estado anterior.");
        refrescar();
    }

    private void onReset() {
        undo = undo.push(state);
        state = limpiar(state);       // [PA06] función pura
        log("Fila reiniciada.");
        refrescar();
    }

    // Exportación sin afectar estado del modelo
    private void onExportCSV() {
        try {
            Path tmp = Files.createTempFile("atendidos-", ".csv");
            try (FileWriter w = new FileWriter(tmp.toFile())) {
                w.write("turno,nombre,llegada,atendido,espera_segundos,espera_hhmmss\n");
                for (Atendido a : state.atendidos) {
                    long seg = a.espera.getSeconds();
                    String hhmmss = String.format("%02d:%02d:%02d", seg/3600, (seg%3600)/60, seg%60);
                    w.write(String.format("%s,%s,%s,%s,%d,%s\n",
                            a.cliente.turno.replace(",", " "),
                            a.cliente.nombre.replace(",", " "),
                            FECHA_HORA.format(a.cliente.llegada),
                            FECHA_HORA.format(a.atendido),
                            seg,
                            hhmmss));
                }
            }
            Desktop.getDesktop().open(tmp.toFile());
            log("CSV exportado: " + tmp);
        } catch (Exception ex) {
            log("Error al exportar CSV: " + ex.getMessage());
        }
    }

    /* ============================ UI helpers ============================ */

    private void refrescar() {
        // [PA06] La vista se reconstruye a partir del estado actual (render puro)
        modelo.clear();
        List<Cliente> l = state.cola.comoListaFIFO();
        for (int i = 0; i < l.size(); i++) {
            String etiqueta = (i == 0 ? "→ " : "  ") + l.get(i);
            modelo.addElement(etiqueta);
        }
        lblEnFila.setText("En fila: " + state.cola.tam());
        lblAtendidos.setText("Atendidos: " + state.atendidos.size());
        lblSig.setText(state.cola.primero().map(c -> "Siguiente: " + c).orElse("Siguiente: —"));
        lblProm.setText("Prom. espera: " + promedioEspera(state).map(ColaClientesProGUI::formato).orElse("—"));
        lblEst.setText("Espera del siguiente: " + estimadoSiguiente(state, LocalDateTime.now()).map(ColaClientesProGUI::formato).orElse("—"));
        mensajes.setCaretPosition(mensajes.getDocument().getLength());
    }

    private void log(String s) { mensajes.append(s + "\n"); }

    /* ===== Renderer para lista: zebra + primero en negritas (detalle de UI) ===== */
    private static class ZebraRenderer extends DefaultListCellRenderer {
        @Override

        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel lbl) {
                // Color de fila alterno solo para estética (no afecta el modelo funcional)
                if (!isSelected) lbl.setBackground(index % 2 == 0 ? new Color(184, 255, 179) : Color.WHITE);
                lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
                lbl.setOpaque(true);
                lbl.setFont(lbl.getFont().deriveFont(index == 0 ? Font.BOLD : Font.PLAIN));
            }
            return c;
        }
    }

    /* ================================ main ================================ */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ColaClientesProGUI().setVisible(true));
    }
}
