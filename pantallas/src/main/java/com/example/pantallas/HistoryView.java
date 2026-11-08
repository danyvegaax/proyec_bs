package com.example.pantallas;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Function;

public class HistoryView {

    // Una consulta (una fila de la tabla)
    public record Consulta(String fecha, String motivo, String veterinario, String iniciales,
                           String diagnostico, String estado) {
    }

    private final PetsView.Mascota mascota;
    private final boolean datosCompletos;
    private final ObservableList<Consulta> consultas = FXCollections.observableArrayList();
    private final FilteredList<Consulta> filtradas = new FilteredList<>(consultas, c -> true);
    private final TextField busquedaField = new TextField();
    private final ComboBox<String> estadoCombo = new ComboBox<>();
    private final Label cantidadLabel = new Label();
    private final Label paginaLabel = new Label();
    private final VBox contenido;

    public HistoryView(PetsView.Mascota mascota) {
        this.mascota = mascota;
        // El diseño solo trae datos completos para Max (M-0101)
        this.datosCompletos = mascota.id().equals("M-0101");
        if (datosCompletos) {
            consultas.addAll(
                    new Consulta("28 Oct 2024 - 10:30", "Control dermatológico post-tratamiento",
                            "Dra. Elena Ramos", "ER",
                            "Cita programada para evaluar remisión de dermatitis atópica.", "Pendiente"),
                    new Consulta("12 Oct 2024 - 16:15", "Prurito intenso y descamación en lomo",
                            "Dra. Elena Ramos", "ER",
                            "Dermatitis alérgica por picadura de pulga. Se indicó Apoquel 16mg y baño.", "Completada"),
                    new Consulta("15 Jun 2024 - 09:00", "Chequeo anual y profilaxis dental preventiva",
                            "Dr. Marcos Castro", "MC",
                            "Estado general óptimo. Grado leve de sarro dental, no requiere anestesia.", "Completada"),
                    new Consulta("02 Feb 2024 - 17:00", "Corte de uñas y limpieza ótica",
                            "Dr. Marcos Castro", "MC",
                            "Cancelada por el propietario 2 horas antes por motivos laborales.", "Cancelada")
            );
        }
        this.contenido = crearContenido();
        aplicarFiltro();
    }

    public Parent getRoot() {
        return new MainShell("mascotas", "Historial Clínico", contenido).getRoot();
    }

    // ---------- Estructura general ----------

    private VBox crearContenido() {
        VBox caja = new VBox(16);
        caja.setPadding(new Insets(24));
        caja.getChildren().addAll(crearBarraSuperior(), crearResumen(), crearZonaPestanas());
        return caja;
    }

    private Region espacioFlexible() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        return espacio;
    }

    private HBox crearBarraSuperior() {
        Hyperlink volver = new Hyperlink("←  Volver a Mascotas");
        volver.getStyleClass().add("link-bold");
        volver.setOnAction(e -> Navigator.irAMascotas());

        Button pdf = new Button("Exportar Ficha Clínica en PDF");
        pdf.getStyleClass().add("light-button");
        Button nueva = new Button("+  Nueva Consulta");
        nueva.getStyleClass().add("dark-button");
        HBox botones = new HBox(8, pdf, nueva);

        HBox fila = new HBox(volver, espacioFlexible(), botones);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    // ---------- Tarjeta resumen (mascota + propietario) ----------

    private HBox crearResumen() {
        String sexo = datosCompletos ? "Macho" : "—";
        String peso = datosCompletos ? "28.5 kg" : "—";
        String microchip = datosCompletos ? "981098102381203" : "—";
        String alergias = datosCompletos ? "Ninguna registrada" : "—";
        String esterilizado = datosCompletos ? "Sí" : "—";
        String direccion = datosCompletos ? "Av. Las Palmeras 450, Lima" : "—";
        String ultimaVisita = datosCompletos ? "12 Octubre 2024" : "—";
        String edad = mascota.edad() + (mascota.edad() == 1 ? " año" : " años");

        // Foto (marcador de posición) y código
        Label pata = new Label("\uD83D\uDC3E");
        pata.getStyleClass().add("pet-paw-big");
        StackPane foto = new StackPane(pata);
        foto.getStyleClass().add("photo-box");
        foto.setMinSize(96, 96);
        foto.setPrefSize(96, 96);
        foto.setMaxSize(96, 96);
        Label codigo = new Label(mascota.id());
        codigo.getStyleClass().addAll("tag", "tag-lila");
        VBox columnaFoto = new VBox(8, foto, codigo);
        columnaFoto.setAlignment(Pos.TOP_CENTER);

        // Datos de la mascota
        Label nombre = new Label(mascota.nombre());
        nombre.getStyleClass().add("pet-title");
        Label especie = new Label(mascota.especie() + " (" + mascota.raza() + ")");
        especie.getStyleClass().add("app-subtitle");
        HBox lineaNombre = new HBox(8, nombre, especie);
        lineaNombre.setAlignment(Pos.BASELINE_LEFT);

        GridPane datos = new GridPane();
        datos.setHgap(12);
        double[] anchos = {20, 20, 20, 40};
        for (double ancho : anchos) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(ancho);
            datos.getColumnConstraints().add(columna);
        }
        datos.add(dato("Edad", edad), 0, 0);
        datos.add(dato("Sexo", sexo), 1, 0);
        datos.add(dato("Peso", peso), 2, 0);
        datos.add(dato("Microchip", microchip), 3, 0);

        Label etiquetaAlergias = new Label("Alergias:");
        etiquetaAlergias.getStyleClass().add("hint");
        Label valorAlergias = new Label(alergias);
        valorAlergias.getStyleClass().add("data-value");
        Label punto = new Label("•");
        punto.getStyleClass().add("hint");
        Label etiquetaEsteril = new Label("Esterilizado:");
        etiquetaEsteril.getStyleClass().add("hint");
        Label valorEsteril = new Label(esterilizado);
        valorEsteril.getStyleClass().add("data-value");
        HBox lineaSalud = new HBox(6, etiquetaAlergias, valorAlergias, punto, etiquetaEsteril, valorEsteril);
        lineaSalud.setAlignment(Pos.CENTER_LEFT);

        VBox columnaDatos = new VBox(12, lineaNombre, datos, lineaSalud);
        HBox.setHgrow(columnaDatos, Priority.ALWAYS);
        HBox izquierda = new HBox(16, columnaFoto, columnaDatos);
        HBox.setHgrow(izquierda, Priority.ALWAYS);

        // Divisor
        Region divisor = new Region();
        divisor.getStyleClass().add("v-divider");

        // Datos del propietario
        Label sobretitulo = new Label("DATOS DEL PROPIETARIO");
        sobretitulo.getStyleClass().add("overline");
        Label dueno = new Label(mascota.dueno());
        dueno.getStyleClass().add("card-title");
        Label telefono = new Label("Tel: " + mascota.telefono());
        telefono.getStyleClass().add("stat-note-muted");
        Label direccionLabel = new Label("Dir: " + direccion);
        direccionLabel.getStyleClass().add("stat-note-muted");
        direccionLabel.setWrapText(true);
        Label visita = new Label("Última Visita: " + ultimaVisita);
        visita.getStyleClass().add("hint");
        VBox.setMargin(visita, new Insets(8, 0, 0, 0));
        VBox propietario = new VBox(6, sobretitulo, dueno, telefono, direccionLabel, visita);
        propietario.setMinWidth(240);
        propietario.setPrefWidth(240);
        propietario.setMaxWidth(240);

        HBox resumen = new HBox(24, izquierda, divisor, propietario);
        resumen.setPadding(new Insets(16));
        resumen.getStyleClass().add("panel-card");
        return resumen;
    }

    private VBox dato(String etiqueta, String valor) {
        Label titulo = new Label(etiqueta);
        titulo.getStyleClass().add("hint");
        Label contenidoValor = new Label(valor);
        contenidoValor.getStyleClass().add("data-value");
        return new VBox(2, titulo, contenidoValor);
    }

    // ---------- Pestañas ----------

    private VBox crearZonaPestanas() {
        ToggleGroup grupo = new ToggleGroup();
        ToggleButton tabCitas = crearPestana("Citas (" + consultas.size() + ")", grupo);
        ToggleButton tabVacunas = crearPestana("Vacunas (" + (datosCompletos ? 3 : 0) + ")", grupo);
        ToggleButton tabTratamientos = crearPestana("Tratamientos (" + (datosCompletos ? 2 : 0) + ")", grupo);
        HBox pestanas = new HBox(4, tabCitas, tabVacunas, tabTratamientos);

        VBox panelCitas = crearPanelCitas();
        VBox panelVacunas = crearMarcador("Aquí irá el listado de vacunas de la mascota.");
        VBox panelTratamientos = crearMarcador("Aquí irá el listado de tratamientos de la mascota.");
        StackPane zona = new StackPane(panelCitas, panelVacunas, panelTratamientos);
        zona.getStyleClass().add("panel-card");

        ToggleButton[] botones = {tabCitas, tabVacunas, tabTratamientos};
        VBox[] paneles = {panelCitas, panelVacunas, panelTratamientos};
        grupo.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
                return;
            }
            for (int i = 0; i < botones.length; i++) {
                boolean activo = botones[i] == nuevo;
                paneles[i].setVisible(activo);
                paneles[i].setManaged(activo);
            }
        });
        tabCitas.setSelected(true);

        return new VBox(pestanas, zona);
    }

    private ToggleButton crearPestana(String texto, ToggleGroup grupo) {
        ToggleButton boton = new ToggleButton(texto);
        boton.setToggleGroup(grupo);
        boton.getStyleClass().add("tab-button");
        return boton;
    }

    private VBox crearMarcador(String mensaje) {
        Label texto = new Label(mensaje);
        texto.getStyleClass().add("hint");
        VBox caja = new VBox(texto);
        caja.setAlignment(Pos.CENTER);
        caja.setPrefHeight(200);
        return caja;
    }

    // ---------- Pestaña Citas ----------

    private VBox crearPanelCitas() {
        busquedaField.setPromptText("Filtrar por motivo, diagnóstico o veterinario...");
        busquedaField.getStyleClass().add("input-outline");
        HBox.setHgrow(busquedaField, Priority.ALWAYS);

        estadoCombo.getItems().addAll("Todos los Estados", "Completadas", "Pendientes", "Canceladas");
        estadoCombo.getSelectionModel().selectFirst();
        estadoCombo.getStyleClass().add("combo-outline");
        estadoCombo.setPrefWidth(180);

        busquedaField.textProperty().addListener((obs, anterior, nuevo) -> aplicarFiltro());
        estadoCombo.valueProperty().addListener((obs, anterior, nuevo) -> aplicarFiltro());

        cantidadLabel.getStyleClass().add("hint");

        HBox barra = new HBox(8, busquedaField, estadoCombo, cantidadLabel);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(16));

        paginaLabel.getStyleClass().add("hint");
        Button anterior = new Button("‹");
        anterior.getStyleClass().add("page-button");
        anterior.setDisable(true);
        Button numero = new Button("1");
        numero.getStyleClass().add("page-button-active");
        Button siguiente = new Button("›");
        siguiente.getStyleClass().add("page-button");
        siguiente.setDisable(true);
        HBox paginacion = new HBox(6, anterior, numero, siguiente);
        paginacion.setAlignment(Pos.CENTER);
        HBox pie = new HBox(paginaLabel, espacioFlexible(), paginacion);
        pie.setAlignment(Pos.CENTER_LEFT);
        pie.setPadding(new Insets(12, 16, 12, 16));

        return new VBox(barra, crearTabla(), pie);
    }

    private void aplicarFiltro() {
        String consulta = busquedaField.getText().trim().toLowerCase();
        int indice = Math.max(0, estadoCombo.getSelectionModel().getSelectedIndex());
        String[] estados = {"todos", "Completada", "Pendiente", "Cancelada"};
        String estado = estados[indice];

        filtradas.setPredicate(c -> {
            boolean coincideTexto = c.motivo().toLowerCase().contains(consulta)
                    || c.diagnostico().toLowerCase().contains(consulta)
                    || c.veterinario().toLowerCase().contains(consulta);
            boolean coincideEstado = estado.equals("todos") || c.estado().equals(estado);
            return coincideTexto && coincideEstado;
        });
        cantidadLabel.setText("Mostrando " + filtradas.size() + " registros clínicos");
        paginaLabel.setText("Página 1 de 1 (" + filtradas.size() + " elementos)");
    }

    private TableColumn<Consulta, String> columnaTexto(String titulo, double ancho,
                                                       Function<Consulta, String> valor) {
        TableColumn<Consulta, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        col.setCellFactory(c -> {
            TableCell<Consulta, String> celda = new TableCell<Consulta, String>() {
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

    private TableView<Consulta> crearTabla() {
        TableView<Consulta> tabla = new TableView<>(filtradas);
        tabla.getStyleClass().addAll("citas-table", "data-table");
        tabla.setFixedCellSize(56);
        tabla.setPrefHeight(38 + 56 * 4 + 2);
        tabla.setMinHeight(38 + 56 * 4 + 2);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Sin registros clínicos"));

        // Veterinario: círculo con iniciales + nombre
        TableColumn<Consulta, Consulta> colVet = new TableColumn<>("Veterinario Asignado");
        colVet.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        colVet.setCellFactory(col -> new TableCell<Consulta, Consulta>() {
            @Override
            protected void updateItem(Consulta c, boolean vacio) {
                super.updateItem(c, vacio);
                if (vacio || c == null) {
                    setGraphic(null);
                    return;
                }
                Label iniciales = new Label(c.iniciales());
                iniciales.getStyleClass().add("avatar-letter-light");
                StackPane circulo = new StackPane(iniciales);
                circulo.getStyleClass().add("avatar-dark");
                circulo.setMinSize(26, 26);
                circulo.setPrefSize(26, 26);
                circulo.setMaxSize(26, 26);
                Label nombre = new Label(c.veterinario());
                nombre.getStyleClass().add("stat-note-muted");
                HBox fila = new HBox(6, circulo, nombre);
                fila.setAlignment(Pos.CENTER_LEFT);
                setGraphic(fila);
            }
        });
        colVet.setPrefWidth(150);
        colVet.setSortable(false);

        // Estado: etiqueta de color
        TableColumn<Consulta, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        colEstado.setCellFactory(col -> new TableCell<Consulta, String>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(estado);
                chip.getStyleClass().add("status-chip");
                switch (estado) {
                    case "Completada" -> chip.getStyleClass().add("status-done");
                    case "Cancelada" -> chip.getStyleClass().add("status-cancel");
                    default -> chip.getStyleClass().add("status-wait");
                }
                setGraphic(chip);
            }
        });
        colEstado.setPrefWidth(96);
        colEstado.setSortable(false);

        // Acciones: botón Ver
        TableColumn<Consulta, Consulta> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        colAcciones.setCellFactory(col -> new TableCell<Consulta, Consulta>() {
            @Override
            protected void updateItem(Consulta c, boolean vacio) {
                super.updateItem(c, vacio);
                if (vacio || c == null) {
                    setGraphic(null);
                    return;
                }
                Button ver = new Button("Ver");
                ver.getStyleClass().add("soft-button");
                ver.setOnAction(e -> mostrarDetalle(c));
                setGraphic(ver);
            }
        });
        colAcciones.setStyle("-fx-alignment: CENTER;");
        colAcciones.setPrefWidth(78);
        colAcciones.setSortable(false);

        tabla.getColumns().add(columnaTexto("Fecha y Hora", 118, Consulta::fecha));
        tabla.getColumns().add(columnaTexto("Motivo de Consulta", 160, Consulta::motivo));
        tabla.getColumns().add(colVet);
        tabla.getColumns().add(columnaTexto("Diagnóstico / Notas", 230, Consulta::diagnostico));
        tabla.getColumns().add(colEstado);
        tabla.getColumns().add(colAcciones);
        return tabla;
    }

    private void mostrarDetalle(Consulta c) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Detalle de consulta");
        alerta.setHeaderText(c.motivo());
        alerta.setContentText(c.fecha() + "\n" + c.veterinario() + "\n\n" + c.diagnostico());
        alerta.showAndWait();
    }
}