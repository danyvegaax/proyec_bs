package com.example.pantallas;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Function;

public class PetsView {

    // Una mascota (una fila de la tabla)
    public record Mascota(String id, String nombre, String especie, String raza,
                          int edad, String dueno, String telefono, String estado) {
    }

    private static final String[] ESPECIES_FILTRO = {"todas", "Canino", "Felino", "Ave", "Conejo", "Otro"};
    private static final List<String> ESPECIES_CONOCIDAS = List.of("Canino", "Felino", "Ave", "Conejo");

    private final ObservableList<Mascota> mascotas = FXCollections.observableArrayList(
            new Mascota("M-0101", "Max", "Canino", "Golden Retriever", 3, "Carlos Méndez", "+51 987 654 321", "Activo"),
            new Mascota("M-0102", "Luna", "Felino", "Siamés", 1, "Lucía Flores", "+51 912 345 678", "Control"),
            new Mascota("M-0103", "Rocky", "Canino", "Bulldog Francés", 4, "Jorge Rivas", "+51 933 221 100", "Activo"),
            new Mascota("M-0104", "Milo", "Felino", "Europeo Común", 2, "Camila Duarte", "+51 945 778 899", "Activo"),
            new Mascota("M-0105", "Coco", "Ave", "Ninfa Carolinas", 2, "Andrés Silva", "+51 954 112 233", "Vacuna"),
            new Mascota("M-0106", "Tambor", "Conejo", "Holandés Enano", 1, "Valeria Paz", "+51 922 884 411", "Activo")
    );
    private final FilteredList<Mascota> filtradas = new FilteredList<>(mascotas, m -> true);

    private final TableView<Mascota> tabla = new TableView<>();
    private final TextField busquedaField = new TextField();
    private final ComboBox<String> filtroCombo = new ComboBox<>();
    private final Label seleccionLabel = new Label();
    private final Label codigoLabel = new Label();
    private final TextField nombreField = new TextField();
    private final ComboBox<String> especieCombo = new ComboBox<>();
    private final TextField razaField = new TextField();
    private final Spinner<Integer> edadSpinner = new Spinner<>();
    private final TextField duenoField = new TextField();
    private final TextField telefonoField = new TextField();
    private final HBox toast = new HBox(8);
    private final Label toastTexto = new Label();
    private PauseTransition temporizadorToast;

    private final VBox contenido;

    public PetsView() {
        contenido = crearContenido();
        tabla.getSelectionModel().selectFirst();
    }

    public Parent getRoot() {
        return new MainShell("mascotas", "Mascotas", contenido).getRoot();
    }

    // ---------- Estructura general ----------

    private VBox crearContenido() {
        VBox caja = new VBox(24);
        caja.setPadding(new Insets(24));
        caja.getChildren().addAll(crearTitulo(), crearDivision());
        return caja;
    }

    private Region espacioFlexible() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        return espacio;
    }

    private HBox crearTitulo() {
        Label titulo = new Label("Gestión de Mascotas");
        titulo.getStyleClass().add("page-title");
        Label subtitulo = new Label("Directorio centralizado de pacientes y fichas clínicas registradas");
        subtitulo.getStyleClass().add("app-subtitle");
        VBox textos = new VBox(2, titulo, subtitulo);

        Region punto = new Region();
        punto.getStyleClass().add("dot-primary");
        Label sincronizado = new Label("Sincronizado con Base Central");
        sincronizado.getStyleClass().add("hint");
        HBox estado = new HBox(6, punto, sincronizado);
        estado.setAlignment(Pos.CENTER_LEFT);

        Button exportar = new Button("Exportar CSV");
        exportar.getStyleClass().add("light-button");

        HBox derecha = new HBox(12, estado, exportar);
        derecha.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(textos, espacioFlexible(), derecha);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private GridPane crearDivision() {
        GridPane grilla = new GridPane();
        grilla.setHgap(16);
        ColumnConstraints izquierda = new ColumnConstraints();
        izquierda.setPercentWidth(62);
        ColumnConstraints derecha = new ColumnConstraints();
        derecha.setPercentWidth(38);
        grilla.getColumnConstraints().addAll(izquierda, derecha);
        grilla.add(crearPanelIzquierdo(), 0, 0);
        grilla.add(crearFicha(), 1, 0);
        return grilla;
    }

    // ---------- Panel izquierdo: búsqueda, tabla y resúmenes ----------

    private VBox crearPanelIzquierdo() {
        VBox tarjeta = new VBox(crearBarraBusqueda(), crearTabla(), crearPieTabla());
        tarjeta.getStyleClass().add("panel-card");
        return new VBox(16, tarjeta, crearResumenes());
    }

    private HBox crearBarraBusqueda() {
        busquedaField.setPromptText("Buscar por nombre o dueño...");
        busquedaField.getStyleClass().add("input-outline");
        HBox.setHgrow(busquedaField, Priority.ALWAYS);

        filtroCombo.getItems().addAll("Filtrar: Todas", "Perro (Canino)", "Gato (Felino)",
                "Ave", "Conejo", "Otro");
        filtroCombo.getSelectionModel().selectFirst();
        filtroCombo.getStyleClass().add("combo-outline");
        filtroCombo.setPrefWidth(150);

        Button buscar = new Button("Buscar");
        buscar.getStyleClass().add("dark-button");
        buscar.setOnAction(e -> aplicarFiltro());

        busquedaField.textProperty().addListener((obs, anterior, nuevo) -> aplicarFiltro());
        filtroCombo.valueProperty().addListener((obs, anterior, nuevo) -> aplicarFiltro());

        HBox barra = new HBox(8, busquedaField, filtroCombo, buscar);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(16));
        return barra;
    }

    private void aplicarFiltro() {
        String consulta = busquedaField.getText().trim().toLowerCase();
        int indice = Math.max(0, filtroCombo.getSelectionModel().getSelectedIndex());
        String especieFiltro = ESPECIES_FILTRO[indice];

        filtradas.setPredicate(m -> {
            boolean coincideTexto = m.nombre().toLowerCase().contains(consulta)
                    || m.dueno().toLowerCase().contains(consulta);
            boolean coincideEspecie;
            if (especieFiltro.equals("todas")) {
                coincideEspecie = true;
            } else if (especieFiltro.equals("Otro")) {
                coincideEspecie = !ESPECIES_CONOCIDAS.contains(m.especie());
            } else {
                coincideEspecie = m.especie().equals(especieFiltro);
            }
            return coincideTexto && coincideEspecie;
        });
    }

    private TableColumn<Mascota, String> columna(String titulo, double ancho, Function<Mascota, String> valor) {
        TableColumn<Mascota, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        col.setPrefWidth(ancho);
        col.setSortable(false);
        return col;
    }

    private TableView<Mascota> crearTabla() {
        tabla.setItems(filtradas);
        tabla.getStyleClass().addAll("citas-table", "data-table");
        tabla.setFixedCellSize(38);
        tabla.setPrefHeight(38 * 7 + 2);
        tabla.setMinHeight(38 * 7 + 2);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay mascotas que coincidan"));

        TableColumn<Mascota, String> colEstado = columna("Estado", 78, Mascota::estado);
        colEstado.setCellFactory(col -> new TableCell<Mascota, String>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(estado);
                chip.getStyleClass().addAll("status-chip",
                        estado.equals("Activo") ? "pet-active" : "pet-other");
                setGraphic(chip);
            }
        });

        tabla.getColumns().add(columna("ID", 64, Mascota::id));
        tabla.getColumns().add(columna("Nombre", 70, Mascota::nombre));
        tabla.getColumns().add(columna("Especie", 66, Mascota::especie));
        tabla.getColumns().add(columna("Raza", 112, Mascota::raza));
        tabla.getColumns().add(columna("Edad", 56,
                m -> m.edad() + (m.edad() == 1 ? " año" : " años")));
        tabla.getColumns().add(columna("Dueño", 100, Mascota::dueno));
        tabla.getColumns().add(colEstado);

        // Al elegir una fila se llena la ficha de la derecha
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                cargarFicha(actual);
            }
        });
        return tabla;
    }

    private VBox crearPieTabla() {
        Button historial = new Button("Ver historial clínico");
        historial.getStyleClass().add("soft-button");
        historial.setOnAction(e -> {
            Mascota actual = tabla.getSelectionModel().getSelectedItem();
            if (actual == null) {
                mostrarToast("Seleccione una mascota de la tabla.");
            } else {
                Navigator.irAHistorial(actual);
            }
        });
        Label etiquetaSeleccion = new Label("Mascota seleccionada:");
        etiquetaSeleccion.getStyleClass().add("hint");
        seleccionLabel.getStyleClass().add("selected-name");
        HBox fila1 = new HBox(8, historial, etiquetaSeleccion, seleccionLabel);
        fila1.setAlignment(Pos.CENTER_LEFT);

        Label total = new Label("Total: 1,428 mascotas registradas");
        total.getStyleClass().add("hint");
        Button anterior = new Button("‹");
        anterior.getStyleClass().add("page-button");
        Label pagina = new Label("1 / 143");
        pagina.getStyleClass().add("page-label");
        Button siguiente = new Button("›");
        siguiente.getStyleClass().add("page-button");
        HBox paginacion = new HBox(6, anterior, pagina, siguiente);
        paginacion.setAlignment(Pos.CENTER);
        HBox fila2 = new HBox(total, espacioFlexible(), paginacion);
        fila2.setAlignment(Pos.CENTER_LEFT);

        VBox pie = new VBox(8, fila1, fila2);
        pie.setPadding(new Insets(12, 16, 12, 16));
        return pie;
    }

    private GridPane crearResumenes() {
        GridPane grilla = new GridPane();
        grilla.setHgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(33.33);
            grilla.getColumnConstraints().add(columna);
        }
        grilla.add(resumen("Vacunas Pendientes", "18 citas hoy"), 0, 0);
        grilla.add(resumen("En Hospitalización", "4 pacientes"), 1, 0);
        grilla.add(resumen("Cumpleaños de Mes", "62 mascotas"), 2, 0);
        return grilla;
    }

    private VBox resumen(String titulo, String valor) {
        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.getStyleClass().add("pill-title");
        Label etiquetaValor = new Label(valor);
        etiquetaValor.getStyleClass().add("pill-value");
        VBox tarjeta = new VBox(2, etiquetaTitulo, etiquetaValor);
        tarjeta.getStyleClass().addAll("stat-card", "pill");
        return tarjeta;
    }

    // ---------- Panel derecho: ficha de la mascota ----------

    private VBox crearFicha() {
        Label pata = new Label("\uD83D\uDC3E");
        pata.getStyleClass().add("pet-paw");
        StackPane avatar = new StackPane(pata);
        avatar.getStyleClass().add("avatar-pet");
        avatar.setMinSize(56, 56);
        avatar.setPrefSize(56, 56);
        avatar.setMaxSize(56, 56);

        Label sobretitulo = new Label("FICHA MÉDICA");
        sobretitulo.getStyleClass().add("overline");
        Label titulo = new Label("Ficha de Mascota");
        titulo.getStyleClass().add("card-title");
        codigoLabel.getStyleClass().add("hint");
        HBox cabecera = new HBox(12, avatar, new VBox(2, sobretitulo, titulo, codigoLabel));
        cabecera.setAlignment(Pos.CENTER_LEFT);

        nombreField.setPromptText("Ej. Max, Toby, Pelusa");
        nombreField.getStyleClass().add("input-outline");

        especieCombo.getItems().addAll("Canino", "Felino", "Ave", "Conejo", "Roedor", "Otro");
        especieCombo.getSelectionModel().selectFirst();
        especieCombo.getStyleClass().add("combo-outline");
        especieCombo.setMaxWidth(Double.MAX_VALUE);

        razaField.setPromptText("Ej. Poodle, Mestizo");
        razaField.getStyleClass().add("input-outline");

        edadSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 35, 1));
        edadSpinner.setEditable(true);
        edadSpinner.setMaxWidth(Double.MAX_VALUE);

        GridPane filaRazaEdad = new GridPane();
        filaRazaEdad.setHgap(12);
        ColumnConstraints colRaza = new ColumnConstraints();
        colRaza.setPercentWidth(58);
        ColumnConstraints colEdad = new ColumnConstraints();
        colEdad.setPercentWidth(42);
        filaRazaEdad.getColumnConstraints().addAll(colRaza, colEdad);
        filaRazaEdad.add(crearCampo("Raza", null, razaField), 0, 0);
        filaRazaEdad.add(crearCampo("Edad (años)", null, edadSpinner), 1, 0);

        duenoField.setPromptText("Nombre completo del cliente");
        duenoField.getStyleClass().add("input-outline");
        telefonoField.setPromptText("+51 900 000 000");
        telefonoField.getStyleClass().add("input-outline");

        Label nota = new Label("Microchip ID: 981098102381203. Última desparasitación hace 2 meses.");
        nota.getStyleClass().add("note-text");
        nota.setWrapText(true);
        HBox cajaNota = new HBox(nota);
        cajaNota.getStyleClass().add("note-box");

        VBox ficha = new VBox(12,
                cabecera,
                crearCampo("Nombre de la Mascota", "*Requerido", nombreField),
                crearCampo("Especie", null, especieCombo),
                filaRazaEdad,
                crearCampo("Dueño / Propietario", null, duenoField),
                crearCampo("Teléfono de contacto", null, telefonoField),
                cajaNota,
                crearBotones(),
                crearToast()
        );
        ficha.setPadding(new Insets(16));
        ficha.getStyleClass().add("panel-card");
        return ficha;
    }

    private VBox crearCampo(String titulo, String pista, Node control) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("field-label");
        HBox fila = new HBox(etiqueta);
        fila.setAlignment(Pos.CENTER_LEFT);
        if (pista != null) {
            Label textoPista = new Label(pista);
            textoPista.getStyleClass().add("hint-accent");
            fila.getChildren().addAll(espacioFlexible(), textoPista);
        }
        return new VBox(4, fila, control);
    }

    private GridPane crearBotones() {
        Button botonGuardar = crearBoton("Guardar", "save-button");
        botonGuardar.setOnAction(e -> guardar());
        Button botonEditar = crearBoton("Editar", "edit-button");
        botonEditar.setOnAction(e -> {
            nombreField.requestFocus();
            nombreField.selectAll();
            mostrarToast("Modo edición habilitado para " + nombreField.getText());
        });
        Button botonEliminar = crearBoton("Eliminar", "delete-button");
        botonEliminar.setOnAction(e -> eliminar());
        Button botonLimpiar = crearBoton("Limpiar", "clear-button");
        botonLimpiar.setOnAction(e -> {
            prepararNuevo();
            mostrarToast("Formulario restablecido para nuevo registro.");
        });

        GridPane grilla = new GridPane();
        grilla.setHgap(8);
        grilla.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        grilla.getColumnConstraints().addAll(c1, c2);
        grilla.add(botonGuardar, 0, 0);
        grilla.add(botonEditar, 1, 0);
        grilla.add(botonEliminar, 0, 1);
        grilla.add(botonLimpiar, 1, 1);
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

        toast.getStyleClass().addAll("toast", "toast-ok");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getChildren().addAll(punto, toastTexto, cerrar);
        ocultarToast();
        return toast;
    }

    private void mostrarToast(String mensaje) {
        toastTexto.setText(mensaje);
        toast.setVisible(true);
        toast.setManaged(true);
        if (temporizadorToast != null) {
            temporizadorToast.stop();
        }
        temporizadorToast = new PauseTransition(Duration.millis(3200));
        temporizadorToast.setOnFinished(e -> ocultarToast());
        temporizadorToast.play();
    }

    private void ocultarToast() {
        toast.setVisible(false);
        toast.setManaged(false);
    }

    // ---------- Acciones ----------

    private void cargarFicha(Mascota m) {
        codigoLabel.setText("Código: " + m.id());
        seleccionLabel.setText(m.nombre() + " (" + m.id() + ")");
        nombreField.setText(m.nombre());
        especieCombo.setValue(m.especie());
        razaField.setText(m.raza());
        edadSpinner.getValueFactory().setValue(m.edad());
        duenoField.setText(m.dueno());
        telefonoField.setText(m.telefono());
    }

    private void prepararNuevo() {
        tabla.getSelectionModel().clearSelection();
        codigoLabel.setText("Código: M-NUEVO");
        seleccionLabel.setText("ninguna");
        nombreField.clear();
        especieCombo.getSelectionModel().selectFirst();
        razaField.clear();
        edadSpinner.getValueFactory().setValue(1);
        duenoField.clear();
        telefonoField.clear();
        nombreField.requestFocus();
    }

    private int leerEdad() {
        try {
            int valor = Integer.parseInt(edadSpinner.getEditor().getText().trim());
            return Math.max(0, Math.min(35, valor));
        } catch (NumberFormatException e) {
            return edadSpinner.getValue();
        }
    }

    private String siguienteId() {
        int mayor = 0;
        for (Mascota m : mascotas) {
            mayor = Math.max(mayor, Integer.parseInt(m.id().substring(2)));
        }
        return String.format("M-%04d", mayor + 1);
    }

    private void guardar() {
        String nombre = nombreField.getText().trim();
        if (nombre.isEmpty()) {
            mostrarToast("El nombre de la mascota es obligatorio.");
            return;
        }
        Mascota actual = tabla.getSelectionModel().getSelectedItem();
        if (actual != null) {
            Mascota actualizada = new Mascota(actual.id(), nombre, especieCombo.getValue(),
                    razaField.getText().trim(), leerEdad(), duenoField.getText().trim(),
                    telefonoField.getText().trim(), actual.estado());
            mascotas.set(mascotas.indexOf(actual), actualizada);
            tabla.getSelectionModel().select(actualizada);
            mostrarToast("Ficha actualizada exitosamente.");
        } else {
            Mascota nueva = new Mascota(siguienteId(), nombre, especieCombo.getValue(),
                    razaField.getText().trim(), leerEdad(), duenoField.getText().trim(),
                    telefonoField.getText().trim(), "Activo");
            mascotas.add(nueva);
            tabla.getSelectionModel().select(nueva);
            mostrarToast("Mascota registrada con el código " + nueva.id() + ".");
        }
    }

    private void eliminar() {
        Mascota actual = tabla.getSelectionModel().getSelectedItem();
        if (actual == null) {
            mostrarToast("Seleccione una mascota de la tabla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Confirma eliminar el registro de " + actual.nombre() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Animalia");
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                mascotas.remove(actual);
                prepararNuevo();
                mostrarToast("Registro de " + actual.nombre() + " eliminado.");
            }
        });
    }
}