package com.example.pantallas;

import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class AppointmentsView {

    public record Paciente(String nombre, String codigo, String raza, String dueno) {
        @Override
        public String toString() {
            return nombre + " - " + codigo + " (" + raza + " - " + dueno + ")";
        }
    }

    public record Veterinario(String nombre, String especialidad) {
        /** "Dra. Elena Ramos" -> "Dra. Ramos" */
        public String corto() {
            String[] partes = nombre.split(" ");
            return partes[0] + " " + partes[partes.length - 1];
        }

        @Override
        public String toString() {
            return nombre + " (" + especialidad + ")";
        }
    }

    public record Cita(String id, LocalDate fecha, String hora, Paciente paciente,
                       Veterinario veterinario, String motivo, String detalle, String estado) {
    }

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern HORA_VALIDA = Pattern.compile("([01]?\\d|2[0-3]):[0-5]\\d");
    private static final String[] ESTADOS_FILTRO = {"todos", "Pendiente", "Completada", "Cancelada"};

    private static final Paciente MAX = new Paciente("Max", "M-0101", "Golden Retriever", "Carlos Gómez");
    private static final Paciente LUNA = new Paciente("Luna", "M-0082", "Siamés", "Patricia Rivas");
    private static final Paciente ROCKY = new Paciente("Rocky", "M-0145", "Bulldog Francés", "Luis Ortiz");
    private static final Paciente MILO = new Paciente("Milo", "M-0199", "Poodle Toy", "Andrea Castro");
    private static final Paciente KIRA = new Paciente("Kira", "M-0211", "Pastor Alemán", "Jorge Vega");
    private static final Paciente SIMBA = new Paciente("Simba", "M-0043", "Europeo Común", "Marta Silva");

    private static final Veterinario ELENA = new Veterinario("Dra. Elena Ramos", "Cirugía General y Tejidos Blandos");
    private static final Veterinario CARLOS = new Veterinario("Dr. Carlos Mendoza", "Medicina Interna y Felinos");
    private static final Veterinario SOFIA = new Veterinario("Dra. Sofía Valdivia", "Dermatología y Diagnóstico");

    private final ObservableList<Cita> citas = FXCollections.observableArrayList(
            new Cita("C-1042", LocalDate.of(2024, 10, 24), "10:30", MAX, ELENA,
                    "Control posquirúrgico", "Control posquirúrgico y revisión de sutura abdominal.", "Pendiente"),
            new Cita("C-1041", LocalDate.of(2024, 10, 24), "09:00", LUNA, CARLOS,
                    "Vacunación séxtuple", "Refuerzo anual séxtuple y desparasitación interna.", "Completada"),
            new Cita("C-1040", LocalDate.of(2024, 10, 23), "16:15", ROCKY, SOFIA,
                    "Ecografía abdominal",
                    "Paciente presentó cólico por ingesta de cuerpo extraño. Cancelada por derivación.", "Cancelada"),
            new Cita("C-1039", LocalDate.of(2024, 10, 23), "11:45", MILO, ELENA,
                    "Otitis alérgica", "Evaluación dermatológica por prurito en orejas.", "Completada"),
            new Cita("C-1038", LocalDate.of(2024, 10, 22), "15:00", KIRA, CARLOS,
                    "Profilaxis dental", "Limpieza dental profiláctica por ultrasonido.", "Pendiente"),
            new Cita("C-1037", LocalDate.of(2024, 10, 22), "08:30", SIMBA, SOFIA,
                    "Control geriátrico", "Chequeo geriátrico semestral y perfil renal.", "Completada")
    );
    private final FilteredList<Cita> filtradas = new FilteredList<>(citas, c -> true);

    private final TableView<Cita> tabla = new TableView<>();

    // Filtros
    private final ComboBox<String> filtroEstado = new ComboBox<>();
    private final DatePicker desdePicker = new DatePicker(LocalDate.of(2024, 10, 18));
    private final DatePicker hastaPicker = new DatePicker(LocalDate.of(2024, 10, 25));

    // Formulario
    private final ComboBox<Paciente> pacienteCombo = new ComboBox<>();
    private final ComboBox<Veterinario> vetCombo = new ComboBox<>();
    private final DatePicker fechaPicker = new DatePicker();
    private final TextField horaField = new TextField();
    private final TextArea motivoArea = new TextArea();
    private final ComboBox<String> estadoCombo = new ComboBox<>();
    private final Label badgeLabel = new Label();
    private final Label subtituloLabel = new Label();

    // Etiquetas dinámicas y aviso
    private final Label activasLabel = new Label();
    private final Label mostrandoLabel = new Label();
    private final HBox toast = new HBox(8);
    private final Label toastTexto = new Label();
    private PauseTransition temporizadorToast;

    private final VBox contenido;

    public AppointmentsView() {
        contenido = crearContenido();
        aplicarFiltro();
        tabla.getSelectionModel().selectFirst();
    }

    public Parent getRoot() {
        return new MainShell("citas", "Citas", contenido).getRoot();
    }

    // ---------- Estructura general ----------

    private VBox crearContenido() {
        VBox caja = new VBox(16);
        caja.setPadding(new Insets(24));
        caja.getChildren().addAll(crearTitulo(), crearFiltros(), crearDivision());
        return caja;
    }

    private Region espacioFlexible() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        return espacio;
    }

    private HBox crearTitulo() {
        Label titulo = new Label("Agenda y Gestión de Citas");
        titulo.getStyleClass().add("page-title");
        Label subtitulo = new Label("Control clínico diario, asignación de facultativos y turnos quirúrgicos.");
        subtitulo.getStyleClass().add("app-subtitle");
        VBox textos = new VBox(2, titulo, subtitulo);

        Region punto = new Region();
        punto.getStyleClass().add("dot-primary");
        Label turnos = new Label("Turnos hoy: 18 registrados");
        turnos.getStyleClass().add("hint");
        HBox estado = new HBox(6, punto, turnos);
        estado.setAlignment(Pos.CENTER_LEFT);

        Button imprimir = new Button("Imprimir Agenda");
        imprimir.getStyleClass().add("light-button");

        HBox derecha = new HBox(12, estado, imprimir);
        derecha.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(textos, espacioFlexible(), derecha);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private GridPane crearDivision() {
        GridPane grilla = new GridPane();
        grilla.setHgap(16);
        ColumnConstraints izquierda = new ColumnConstraints();
        izquierda.setPercentWidth(66);
        ColumnConstraints derecha = new ColumnConstraints();
        derecha.setPercentWidth(34);
        grilla.getColumnConstraints().addAll(izquierda, derecha);
        grilla.add(crearPanelIzquierdo(), 0, 0);
        grilla.add(crearFormulario(), 1, 0);
        return grilla;
    }

    // ---------- Filtros ----------

    private StringConverter<LocalDate> conversorFecha() {
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : fecha.format(FORMATO);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(texto.trim(), FORMATO);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        };
    }

    private VBox grupo(String titulo, javafx.scene.Node control) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("field-label");
        return new VBox(4, etiqueta, control);
    }

    private HBox crearFiltros() {
        filtroEstado.getItems().addAll("Todos los estados", "Pendiente", "Completada", "Cancelada");
        filtroEstado.getSelectionModel().selectFirst();
        filtroEstado.getStyleClass().add("combo-outline");
        filtroEstado.setPrefWidth(180);

        desdePicker.setConverter(conversorFecha());
        desdePicker.setPromptText("dd/mm/aaaa");
        desdePicker.getStyleClass().add("date-outline");
        desdePicker.setPrefWidth(140);
        hastaPicker.setConverter(conversorFecha());
        hastaPicker.setPromptText("dd/mm/aaaa");
        hastaPicker.getStyleClass().add("date-outline");
        hastaPicker.setPrefWidth(140);
        HBox fechas = new HBox(8, desdePicker, hastaPicker);

        Button filtrar = new Button("Filtrar");
        filtrar.getStyleClass().addAll("form-button", "save-button");
        filtrar.setOnAction(e -> aplicarFiltro());
        Button limpiar = new Button("Limpiar filtros");
        limpiar.getStyleClass().addAll("form-button", "edit-button");
        limpiar.setOnAction(e -> limpiarFiltros());
        HBox botones = new HBox(8, filtrar, limpiar);

        HBox barra = new HBox(16,
                grupo("Filtrar por Estado", filtroEstado),
                grupo("Rango de fechas", fechas),
                botones);
        barra.setAlignment(Pos.BOTTOM_LEFT);
        barra.setPadding(new Insets(16));
        barra.getStyleClass().add("panel-card");
        return barra;
    }

    private void aplicarFiltro() {
        int indice = Math.max(0, filtroEstado.getSelectionModel().getSelectedIndex());
        String estado = ESTADOS_FILTRO[indice];
        LocalDate desde = desdePicker.getValue();
        LocalDate hasta = hastaPicker.getValue();

        filtradas.setPredicate(c -> {
            boolean coincideEstado = estado.equals("todos") || c.estado().equals(estado);
            boolean despuesDeDesde = desde == null || !c.fecha().isBefore(desde);
            boolean antesDeHasta = hasta == null || !c.fecha().isAfter(hasta);
            return coincideEstado && despuesDeDesde && antesDeHasta;
        });
        actualizarContadores();
    }

    private void limpiarFiltros() {
        filtroEstado.getSelectionModel().selectFirst();
        desdePicker.setValue(LocalDate.of(2024, 10, 18));
        hastaPicker.setValue(LocalDate.of(2024, 10, 25));
        aplicarFiltro();
    }

    private void actualizarContadores() {
        activasLabel.setText(citas.size() + " activas");
        mostrandoLabel.setText("Mostrando " + filtradas.size() + " de " + citas.size() + " citas");
    }

    // ---------- Panel izquierdo: tabla y resúmenes ----------

    private VBox crearPanelIzquierdo() {
        Label titulo = new Label("Citas Registradas");
        titulo.getStyleClass().add("card-title");
        activasLabel.getStyleClass().addAll("tag", "tag-lila");
        Label ayuda = new Label("Seleccione una fila para editar");
        ayuda.getStyleClass().add("hint");
        HBox cabecera = new HBox(8, titulo, activasLabel, espacioFlexible(), ayuda);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setPadding(new Insets(16));

        mostrandoLabel.getStyleClass().add("hint");
        HBox paginacion = new HBox(4);
        paginacion.setAlignment(Pos.CENTER);
        for (String texto : new String[]{"«", "‹"}) {
            Button boton = new Button(texto);
            boton.getStyleClass().add("page-button");
            paginacion.getChildren().add(boton);
        }
        Button uno = new Button("1");
        uno.getStyleClass().add("page-button-active");
        paginacion.getChildren().add(uno);
        for (String texto : new String[]{"2", "3", "›", "»"}) {
            Button boton = new Button(texto);
            boton.getStyleClass().add("page-button");
            paginacion.getChildren().add(boton);
        }
        HBox pie = new HBox(mostrandoLabel, espacioFlexible(), paginacion);
        pie.setAlignment(Pos.CENTER_LEFT);
        pie.setPadding(new Insets(12, 16, 12, 16));

        VBox tarjeta = new VBox(cabecera, crearTabla(), pie);
        tarjeta.getStyleClass().add("panel-card");
        return new VBox(16, tarjeta, crearResumenes());
    }

    private TableColumn<Cita, String> columnaTexto(String titulo, double ancho, Function<Cita, String> valor) {
        TableColumn<Cita, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        col.setCellFactory(c -> {
            TableCell<Cita, String> celda = new TableCell<Cita, String>() {
                @Override
                protected void updateItem(String texto, boolean vacio) {
                    super.updateItem(texto, vacio);
                    setText(vacio ? null : texto);
                }
            };
            celda.setWrapText(true);
            return celda;
        });
        col.setPrefWidth(ancho);
        col.setSortable(false);
        return col;
    }

    /** Columna con dos líneas de texto (por ejemplo fecha y hora). */
    private TableColumn<Cita, Cita> columnaDoble(String titulo, double ancho, Function<Cita, String> arriba,
                                                 Function<Cita, String> abajo, String claseArriba) {
        TableColumn<Cita, Cita> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        col.setCellFactory(c -> new TableCell<Cita, Cita>() {
            @Override
            protected void updateItem(Cita cita, boolean vacio) {
                super.updateItem(cita, vacio);
                if (vacio || cita == null) {
                    setGraphic(null);
                    return;
                }
                Label primera = new Label(arriba.apply(cita));
                primera.getStyleClass().add(claseArriba);
                Label segunda = new Label(abajo.apply(cita));
                segunda.getStyleClass().add("pet-species");
                VBox caja = new VBox(1, primera, segunda);
                caja.setAlignment(Pos.CENTER_LEFT);
                setGraphic(caja);
            }
        });
        col.setPrefWidth(ancho);
        col.setSortable(false);
        return col;
    }

    private TableColumn<Cita, String> columnaEstado() {
        TableColumn<Cita, String> col = new TableColumn<>("Estado");
        col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        col.setCellFactory(c -> new TableCell<Cita, String>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                Region punto = new Region();
                punto.getStyleClass().add("chip-dot");
                Label chip = new Label(estado, punto);
                chip.setGraphicTextGap(5);
                String variante = switch (estado) {
                    case "Completada" -> "cita-completada";
                    case "Cancelada" -> "cita-cancelada";
                    default -> "cita-pendiente";
                };
                chip.getStyleClass().addAll("cita-chip", variante);
                setGraphic(chip);
            }
        });
        col.setPrefWidth(106);
        col.setSortable(false);
        return col;
    }

    private TableView<Cita> crearTabla() {
        tabla.setItems(filtradas);
        tabla.getStyleClass().addAll("citas-table", "data-table", "compact-table");
        tabla.setFixedCellSize(48);
        tabla.setPrefHeight(38 + 48 * 6 + 2);
        tabla.setMinHeight(38 + 48 * 6 + 2);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay citas para los filtros seleccionados"));

        tabla.getColumns().add(columnaTexto("ID", 52, Cita::id));
        tabla.getColumns().add(columnaDoble("Fecha y Hora", 82,
                c -> c.fecha().format(FORMATO), Cita::hora, "cell-main"));
        tabla.getColumns().add(columnaDoble("Mascota", 104,
                c -> c.paciente().nombre(), c -> c.paciente().raza(), "pet-name"));
        tabla.getColumns().add(columnaTexto("Veterinario", 92, c -> c.veterinario().corto()));
        tabla.getColumns().add(columnaTexto("Motivo", 104, Cita::motivo));
        tabla.getColumns().add(columnaEstado());

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                cargarFormulario(actual);
            }
        });
        return tabla;
    }

    private GridPane crearResumenes() {
        GridPane grilla = new GridPane();
        grilla.setHgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(33.33);
            grilla.getColumnConstraints().add(columna);
        }
        grilla.add(resumen("8", "Turnos Pendientes", "accent-amber"), 0, 0);
        grilla.add(resumen("14", "Completadas Hoy", "accent-green"), 1, 0);
        grilla.add(resumen("2", "Anuladas", "accent-red"), 2, 0);
        return grilla;
    }

    private HBox resumen(String valor, String titulo, String claseAcento) {
        Region barra = new Region();
        barra.getStyleClass().add(claseAcento);
        barra.setMinSize(4, 32);
        barra.setPrefSize(4, 32);
        barra.setMaxSize(4, 32);

        Label numero = new Label(valor);
        numero.getStyleClass().add("pill-big");
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("pill-title");

        HBox tarjeta = new HBox(10, barra, new VBox(1, numero, etiqueta));
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.getStyleClass().addAll("stat-card", "pill");
        return tarjeta;
    }

    // ---------- Formulario ----------

    private VBox crearFormulario() {
        Label titulo = new Label("Agendar / Editar Cita");
        titulo.getStyleClass().add("card-title");
        subtituloLabel.getStyleClass().add("hint");
        badgeLabel.getStyleClass().addAll("tag", "tag-lila");
        HBox cabecera = new HBox(new VBox(2, titulo, subtituloLabel), espacioFlexible(), badgeLabel);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        pacienteCombo.getItems().addAll(List.of(MAX, LUNA, ROCKY, MILO, KIRA, SIMBA));
        pacienteCombo.getStyleClass().add("combo-outline");
        pacienteCombo.setMaxWidth(Double.MAX_VALUE);

        vetCombo.getItems().addAll(List.of(ELENA, CARLOS, SOFIA));
        vetCombo.getStyleClass().add("combo-outline");
        vetCombo.setMaxWidth(Double.MAX_VALUE);

        fechaPicker.setConverter(conversorFecha());
        fechaPicker.setPromptText("dd/mm/aaaa");
        fechaPicker.getStyleClass().add("date-outline");
        fechaPicker.setMaxWidth(Double.MAX_VALUE);

        horaField.setPromptText("HH:mm");
        horaField.getStyleClass().add("input-outline");

        GridPane filaFechaHora = new GridPane();
        filaFechaHora.setHgap(12);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(55);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(45);
        filaFechaHora.getColumnConstraints().addAll(c1, c2);
        filaFechaHora.add(grupo("Fecha", fechaPicker), 0, 0);
        filaFechaHora.add(grupo("Hora (24 h)", horaField), 1, 0);

        motivoArea.setPromptText("Describa síntomas o motivo...");
        motivoArea.setPrefRowCount(3);
        motivoArea.setWrapText(true);
        motivoArea.getStyleClass().add("area-outline");

        estadoCombo.getItems().addAll("Pendiente", "Completada", "Cancelada");
        estadoCombo.getSelectionModel().selectFirst();
        estadoCombo.getStyleClass().add("combo-outline");
        estadoCombo.setMaxWidth(Double.MAX_VALUE);

        Label nota = new Label("Las modificaciones en la agenda notifican automáticamente al propietario "
                + "mediante SMS y actualizan la ficha clínica en JavaFX.");
        nota.getStyleClass().add("note-text");
        nota.setWrapText(true);
        HBox cajaNota = new HBox(nota);
        cajaNota.getStyleClass().add("note-box");

        VBox formulario = new VBox(12,
                cabecera,
                grupo("Mascota Paciente", pacienteCombo),
                grupo("Veterinario Responsable", vetCombo),
                filaFechaHora,
                grupo("Motivo de consulta", motivoArea),
                grupo("Estado del Turno", estadoCombo),
                crearBotones(),
                crearToast(),
                cajaNota
        );
        formulario.setPadding(new Insets(16));
        formulario.getStyleClass().add("panel-card");
        return formulario;
    }

    private GridPane crearBotones() {
        Button agendar = crearBoton("Agendar cita", "save-button");
        agendar.setOnAction(e -> agendar());
        Button actualizar = crearBoton("Actualizar", "edit-button");
        actualizar.setOnAction(e -> actualizar());
        Button cancelar = crearBoton("Cancelar cita", "delete-button");
        cancelar.setOnAction(e -> cancelarCita());

        GridPane grilla = new GridPane();
        grilla.setHgap(8);
        grilla.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        grilla.getColumnConstraints().addAll(c1, c2);
        grilla.add(agendar, 0, 0);
        grilla.add(actualizar, 1, 0);
        grilla.add(cancelar, 0, 1, 2, 1);
        return grilla;
    }

    private Button crearBoton(String texto, String variante) {
        Button boton = new Button(texto);
        boton.getStyleClass().addAll("form-button", variante);
        boton.setMaxWidth(Double.MAX_VALUE);
        return boton;
    }

    private HBox crearToast() {
        Region punto = new Region();
        punto.getStyleClass().add("toast-dot");

        toastTexto.setWrapText(true);
        toastTexto.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(toastTexto, Priority.ALWAYS);

        Button cerrar = new Button("×");
        cerrar.getStyleClass().add("toast-close");
        cerrar.setOnAction(e -> ocultarToast());

        toast.getStyleClass().add("toast");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getChildren().addAll(punto, toastTexto, cerrar);
        ocultarToast();
        return toast;
    }

    private void mostrarToast(String mensaje, boolean error) {
        toastTexto.setText(mensaje);
        toast.getStyleClass().removeAll("toast-ok", "toast-error");
        toast.getStyleClass().add(error ? "toast-error" : "toast-ok");
        toast.setVisible(true);
        toast.setManaged(true);
        if (temporizadorToast != null) {
            temporizadorToast.stop();
        }
        temporizadorToast = new PauseTransition(Duration.millis(3500));
        temporizadorToast.setOnFinished(e -> ocultarToast());
        temporizadorToast.play();
    }

    private void ocultarToast() {
        toast.setVisible(false);
        toast.setManaged(false);
    }

    // ---------- Acciones ----------

    private void cargarFormulario(Cita c) {
        badgeLabel.setText(c.id());
        subtituloLabel.setText("Editando turno: " + c.id());
        pacienteCombo.setValue(c.paciente());
        vetCombo.setValue(c.veterinario());
        fechaPicker.setValue(c.fecha());
        horaField.setText(c.hora());
        motivoArea.setText(c.detalle());
        estadoCombo.setValue(c.estado());
    }

    private String siguienteId() {
        int mayor = 0;
        for (Cita c : citas) {
            mayor = Math.max(mayor, Integer.parseInt(c.id().substring(2)));
        }
        return "C-" + (mayor + 1);
    }

    private String normalizarHora(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (!HORA_VALIDA.matcher(limpio).matches()) {
            return null;
        }
        return limpio.length() == 4 ? "0" + limpio : limpio;
    }

    private String resumir(String detalle) {
        String primera = detalle.split("[.,]")[0].trim();
        if (primera.isEmpty()) {
            primera = detalle;
        }
        return primera.length() > 28 ? primera.substring(0, 28).trim() + "…" : primera;
    }

    /** Lee el formulario y arma una cita; si hay un dato inválido avisa y devuelve null. */
    private Cita construirDesdeFormulario(String id) {
        Paciente paciente = pacienteCombo.getValue();
        Veterinario vet = vetCombo.getValue();
        LocalDate fecha = fechaPicker.getConverter().fromString(fechaPicker.getEditor().getText());
        String hora = normalizarHora(horaField.getText());
        String detalle = motivoArea.getText().trim();

        if (paciente == null || vet == null) {
            mostrarToast("Seleccione la mascota y el veterinario.", true);
            return null;
        }
        if (fecha == null) {
            mostrarToast("Ingrese una fecha válida (dd/mm/aaaa).", true);
            return null;
        }
        if (hora == null) {
            mostrarToast("Ingrese la hora en formato HH:mm (por ejemplo 10:30).", true);
            return null;
        }
        if (detalle.isEmpty()) {
            mostrarToast("Describa el motivo de la consulta.", true);
            return null;
        }
        return new Cita(id, fecha, hora, paciente, vet, resumir(detalle), detalle, estadoCombo.getValue());
    }

    private void agendar() {
        Cita nueva = construirDesdeFormulario(siguienteId());
        if (nueva == null) {
            return;
        }
        citas.add(0, nueva);
        tabla.getSelectionModel().select(nueva);
        subtituloLabel.setText("Nuevo turno agendado: " + nueva.id());
        actualizarContadores();
        mostrarToast("Cita " + nueva.id() + " agendada satisfactoriamente.", false);
    }

    private void actualizar() {
        Cita actual = tabla.getSelectionModel().getSelectedItem();
        if (actual == null) {
            mostrarToast("Seleccione una cita de la tabla para actualizarla.", true);
            return;
        }
        Cita editada = construirDesdeFormulario(actual.id());
        if (editada == null) {
            return;
        }
        citas.set(citas.indexOf(actual), editada);
        tabla.getSelectionModel().select(editada);
        actualizarContadores();
        mostrarToast("La cita " + editada.id() + " ha sido actualizada.", false);
    }

    private void cancelarCita() {
        Cita actual = tabla.getSelectionModel().getSelectedItem();
        if (actual == null) {
            mostrarToast("Seleccione una cita de la tabla para cancelarla.", true);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Confirma la cancelación de la cita " + actual.id() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Animalia");
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                Cita cancelada = new Cita(actual.id(), actual.fecha(), actual.hora(), actual.paciente(),
                        actual.veterinario(), actual.motivo(), actual.detalle(), "Cancelada");
                citas.set(citas.indexOf(actual), cancelada);
                tabla.getSelectionModel().select(cancelada);
                mostrarToast("Cita marcada como Cancelada.", false);
            }
        });
    }
}