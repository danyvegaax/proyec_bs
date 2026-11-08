package com.example.pantallas;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;

public class DashboardView {

    // Una fila de la tabla de citas
    public record Cita(String hora, String mascota, String especie, String motivo, String estado) {
    }

    public Parent getRoot() {
        return new MainShell("inicio", "Panel de Control", crearContenido()).getRoot();
    }

    private VBox crearContenido() {
        VBox contenido = new VBox(24);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().addAll(
                crearTitulo(),
                crearTarjetasResumen(),
                crearFilaCentral(),
                crearAccesos()
        );
        return contenido;
    }

    // ---------- Utilidades ----------

    private GridPane crearGrilla(double... porcentajes) {
        GridPane grilla = new GridPane();
        grilla.setHgap(16);
        for (double porcentaje : porcentajes) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(porcentaje);
            grilla.getColumnConstraints().add(columna);
        }
        return grilla;
    }

    private Region espacioFlexible() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        return espacio;
    }

    // ---------- Título y botones ----------

    private HBox crearTitulo() {
        Region punto = new Region();
        punto.getStyleClass().add("dot-primary");
        Label sobretitulo = new Label("SISTEMA DE GESTIÓN CLÍNICA");
        sobretitulo.getStyleClass().add("overline");
        HBox filaSobre = new HBox(6, punto, sobretitulo);
        filaSobre.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Panel Principal");
        titulo.getStyleClass().add("page-title");
        Label subtitulo = new Label("Bienvenida, Dra. Elena Ramos • Clínica Veterinaria San Miguel");
        subtitulo.getStyleClass().add("app-subtitle");
        VBox izquierda = new VBox(2, filaSobre, titulo, subtitulo);

        Button sincronizar = new Button("Sincronizar");
        sincronizar.getStyleClass().add("light-button");
        Button nuevaCita = new Button("+  Nueva Cita");
        nuevaCita.getStyleClass().add("dark-button");
        HBox botones = new HBox(8, sincronizar, nuevaCita);
        botones.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(izquierda, espacioFlexible(), botones);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    // ---------- Tarjetas de resumen ----------

    private GridPane crearTarjetasResumen() {
        GridPane grilla = crearGrilla(25, 25, 25, 25);

        grilla.add(tarjetaStat("Mascotas registradas",
                valor("1,428", null),
                texto("↑ +24 este mes en el padrón", "stat-note")), 0, 0);

        Label dieciocho = valor("18", null);
        Label programadas = new Label("programadas");
        programadas.getStyleClass().add("stat-unit");
        HBox valorCitas = new HBox(6, dieciocho, programadas);
        valorCitas.setAlignment(Pos.BASELINE_LEFT);
        HBox pieCitas = new HBox(texto("Atendidas: 4 de 18", "stat-note-muted"),
                espacioFlexible(), crearMiniBarra(0.22));
        pieCitas.setAlignment(Pos.CENTER_LEFT);
        grilla.add(tarjetaStat("Citas de hoy", valorCitas, pieCitas), 1, 0);

        grilla.add(tarjetaStat("Vacunas próximas a vencer",
                valor("7", "value-tertiary"),
                etiqueta("Próximos 15 días", "tag-lila")), 2, 0);

        grilla.add(tarjetaStat("Medicamentos con stock bajo",
                valor("5", "value-error"),
                etiqueta("Requiere reposición", "tag-error")), 3, 0);

        return grilla;
    }

    private VBox tarjetaStat(String titulo, Node valor, Node pie) {
        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.getStyleClass().add("stat-title");
        etiquetaTitulo.setWrapText(true);

        VBox superior = new VBox(4, etiquetaTitulo, valor);
        VBox.setVgrow(superior, Priority.ALWAYS);

        VBox tarjeta = new VBox(12, superior, pie);
        tarjeta.getStyleClass().add("stat-card");
        return tarjeta;
    }

    private Label valor(String texto, String claseExtra) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("stat-value");
        if (claseExtra != null) {
            etiqueta.getStyleClass().add(claseExtra);
        }
        return etiqueta;
    }

    private Label texto(String contenido, String clase) {
        Label etiqueta = new Label(contenido);
        etiqueta.getStyleClass().add(clase);
        return etiqueta;
    }

    private HBox etiqueta(String contenido, String variante) {
        Label chip = new Label(contenido);
        chip.getStyleClass().addAll("tag", variante);
        HBox caja = new HBox(chip);
        caja.setAlignment(Pos.CENTER_LEFT);
        return caja;
    }

    private StackPane crearMiniBarra(double fraccion) {
        double ancho = 80;
        double relleno = ancho * fraccion;

        Region fondo = new Region();
        fondo.getStyleClass().add("mini-track");
        fondo.setMinSize(ancho, 8);
        fondo.setPrefSize(ancho, 8);
        fondo.setMaxSize(ancho, 8);

        Region progreso = new Region();
        progreso.getStyleClass().add("mini-fill");
        progreso.setMinSize(relleno, 8);
        progreso.setPrefSize(relleno, 8);
        progreso.setMaxSize(relleno, 8);

        StackPane barra = new StackPane(fondo, progreso);
        StackPane.setAlignment(progreso, Pos.CENTER_LEFT);
        barra.setMinSize(ancho, 8);
        barra.setPrefSize(ancho, 8);
        barra.setMaxSize(ancho, 8);
        return barra;
    }

    // ---------- Fila central: citas + especies ----------

    private GridPane crearFilaCentral() {
        GridPane grilla = crearGrilla(58.33, 41.67);
        grilla.add(crearTarjetaCitas(), 0, 0);
        grilla.add(crearTarjetaEspecies(), 1, 0);
        return grilla;
    }

    private VBox crearTarjetaCitas() {
        Label titulo = new Label("Próximas citas de hoy");
        titulo.getStyleClass().add("card-title");
        Label vista = new Label("Vista JavaFX TableView");
        vista.getStyleClass().add("hint");
        Region punto = new Region();
        punto.getStyleClass().add("dot-secondary");
        HBox cabecera = new HBox(8, titulo, espacioFlexible(), vista, punto);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setPadding(new Insets(16));

        Label total = new Label("Mostrando 6 de 18 registros");
        total.getStyleClass().add("hint");
        Button anterior = new Button("‹");
        anterior.getStyleClass().add("page-button");
        Label pagina = new Label("1 / 3");
        pagina.getStyleClass().add("page-label");
        Button siguiente = new Button("›");
        siguiente.getStyleClass().add("page-button");
        HBox paginacion = new HBox(6, anterior, pagina, siguiente);
        paginacion.setAlignment(Pos.CENTER);
        HBox pie = new HBox(total, espacioFlexible(), paginacion);
        pie.setAlignment(Pos.CENTER_LEFT);
        pie.setPadding(new Insets(12, 16, 12, 16));

        VBox tarjeta = new VBox(cabecera, crearTablaCitas(), pie);
        tarjeta.getStyleClass().add("panel-card");
        return tarjeta;
    }

    private TableView<Cita> crearTablaCitas() {
        TableView<Cita> tabla = new TableView<>();
        tabla.getStyleClass().add("citas-table");
        tabla.setFixedCellSize(38);
        tabla.setPrefHeight(38 * 7 + 2);
        tabla.setMinHeight(38 * 7 + 2);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Sin citas para hoy"));

        TableColumn<Cita, String> colHora = new TableColumn<>("Fecha / Hora");
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().hora()));
        colHora.setPrefWidth(80);
        colHora.setSortable(false);

        TableColumn<Cita, Cita> colMascota = new TableColumn<>("Mascota y Especie");
        colMascota.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        colMascota.setCellFactory(col -> new TableCell<Cita, Cita>() {
            @Override
            protected void updateItem(Cita cita, boolean vacio) {
                super.updateItem(cita, vacio);
                if (vacio || cita == null) {
                    setGraphic(null);
                    return;
                }
                Label nombre = new Label(cita.mascota());
                nombre.getStyleClass().add("pet-name");
                Label especie = new Label("(" + cita.especie() + ")");
                especie.getStyleClass().add("pet-species");
                setGraphic(new HBox(6, nombre, especie));
            }
        });
        colMascota.setPrefWidth(170);
        colMascota.setSortable(false);

        TableColumn<Cita, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().motivo()));
        colMotivo.setPrefWidth(140);
        colMotivo.setSortable(false);

        TableColumn<Cita, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        colEstado.setCellFactory(col -> new TableCell<Cita, String>() {
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
        colEstado.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 16 0 0;");
        colEstado.setPrefWidth(95);
        colEstado.setSortable(false);

        tabla.getColumns().add(colHora);
        tabla.getColumns().add(colMascota);
        tabla.getColumns().add(colMotivo);
        tabla.getColumns().add(colEstado);

        tabla.setItems(FXCollections.observableArrayList(
                new Cita("09:00 AM", "Toby", "Canino - Labrador", "Control postquirúrgico", "Completada"),
                new Cita("09:45 AM", "Luna", "Felino - Siamés", "Vacuna Triple Felina", "Completada"),
                new Cita("10:30 AM", "Max", "Canino - Golden", "Chequeo anual preventivo", "En espera"),
                new Cita("11:15 AM", "Kiwi", "Ave - Ninfa", "Recorte de plumaje y pico", "Pendiente"),
                new Cita("12:00 PM", "Rocky", "Canino - Bulldog", "Dermatitis alérgica", "Cancelada"),
                new Cita("02:30 PM", "Thor", "Reptil - Pogona", "Desparasitación preventiva", "Pendiente")
        ));
        return tabla;
    }

    private VBox crearTarjetaEspecies() {
        Label titulo = new Label("Mascotas por especie");
        titulo.getStyleClass().add("card-title");
        Label base = new Label("Base activa");
        base.getStyleClass().add("hint");
        HBox cabecera = new HBox(8, titulo, espacioFlexible(), base);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Label actualizacion = new Label("Última actualización: Hoy, 09:15");
        actualizacion.getStyleClass().add("hint");
        Hyperlink desglose = new Hyperlink("Ver desglose →");
        desglose.getStyleClass().add("link-bold");
        desglose.setOnAction(e -> Navigator.irASeccion("reportes"));
        HBox pie = new HBox(actualizacion, espacioFlexible(), desglose);
        pie.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(16, cabecera, crearDona(), crearLeyenda(), pie);
        tarjeta.setPadding(new Insets(16));
        tarjeta.getStyleClass().add("panel-card");
        return tarjeta;
    }

    private StackPane crearDona() {
        double tam = 180;
        double centro = tam / 2;
        double radio = tam * 0.38;
        double grosor = tam * 0.18;

        Pane lienzo = new Pane();
        lienzo.setMinSize(tam, tam);
        lienzo.setPrefSize(tam, tam);
        lienzo.setMaxSize(tam, tam);

        Circle fondo = new Circle(centro, centro, radio);
        fondo.setFill(Color.TRANSPARENT);
        fondo.setStroke(Color.web("#ebddff"));
        fondo.setStrokeWidth(grosor);
        lienzo.getChildren().add(fondo);

        double[] porcentajes = {58, 32, 6, 4};
        String[] colores = {"#522587", "#69519b", "#c6abfd", "#3f3d46"};
        double acumulado = 0;
        for (int i = 0; i < porcentajes.length; i++) {
            double inicio = 90 - acumulado * 3.6;   // 90° = arriba
            double largo = -porcentajes[i] * 3.6;   // negativo = sentido del reloj
            Arc arco = new Arc(centro, centro, radio, radio, inicio, largo);
            arco.setType(ArcType.OPEN);
            arco.setFill(Color.TRANSPARENT);
            arco.setStroke(Color.web(colores[i]));
            arco.setStrokeWidth(grosor);
            arco.setStrokeLineCap(StrokeLineCap.BUTT);
            lienzo.getChildren().add(arco);
            acumulado += porcentajes[i];
        }

        Label numero = new Label("1,428");
        numero.getStyleClass().add("donut-number");
        Label leyenda = new Label("PACIENTES");
        leyenda.getStyleClass().add("donut-caption");
        VBox textoCentral = new VBox(2, numero, leyenda);
        textoCentral.setAlignment(Pos.CENTER);
        textoCentral.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        return new StackPane(lienzo, textoCentral);
    }

    private GridPane crearLeyenda() {
        GridPane grilla = new GridPane();
        grilla.setHgap(12);
        grilla.setVgap(8);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(50);
            grilla.getColumnConstraints().add(columna);
        }
        grilla.add(itemLeyenda("Caninos", "58%", "#522587", "#522587"), 0, 0);
        grilla.add(itemLeyenda("Felinos", "32%", "#69519b", "#69519b"), 1, 0);
        grilla.add(itemLeyenda("Aves", "6%", "#c6abfd", "#543b84"), 0, 1);
        grilla.add(itemLeyenda("Exóticos", "4%", "#3f3d46", "#3f3d46"), 1, 1);
        return grilla;
    }

    private HBox itemLeyenda(String nombre, String porcentaje, String colorPunto, String colorTexto) {
        Region punto = new Region();
        punto.setMinSize(10, 10);
        punto.setPrefSize(10, 10);
        punto.setMaxSize(10, 10);
        punto.setStyle("-fx-background-color: " + colorPunto + "; -fx-background-radius: 999;");

        Label etiquetaNombre = new Label(nombre);
        etiquetaNombre.getStyleClass().add("legend-name");
        Label etiquetaPorcentaje = new Label(porcentaje);
        etiquetaPorcentaje.getStyleClass().add("legend-percent");
        etiquetaPorcentaje.setStyle("-fx-text-fill: " + colorTexto + ";");

        HBox item = new HBox(8, punto, etiquetaNombre, espacioFlexible(), etiquetaPorcentaje);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("legend-item");
        return item;
    }

    // ---------- Accesos rápidos ----------

    private GridPane crearAccesos() {
        GridPane grilla = crearGrilla(33.33, 33.33, 33.34);
        grilla.add(acceso("Historiales Médicos", "Actualizar diagnósticos y tratamientos",
                "Abrir", "mascotas"), 0, 0);
        grilla.add(acceso("Farmacia e Insumos", "Revisar 5 alertas de inventario",
                "Revisar", "medicamentos"), 1, 0);
        grilla.add(acceso("Cierre de Caja", "Reporte financiero del turno",
                "Generar", "reportes"), 2, 0);
        return grilla;
    }

    private HBox acceso(String titulo, String descripcion, String textoBoton, String seccion) {
        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.getStyleClass().add("shortcut-title");
        Label etiquetaDescripcion = new Label(descripcion);
        etiquetaDescripcion.getStyleClass().add("stat-note-muted");
        etiquetaDescripcion.setWrapText(true);
        VBox textos = new VBox(2, etiquetaTitulo, etiquetaDescripcion);
        HBox.setHgrow(textos, Priority.ALWAYS);

        Button boton = new Button(textoBoton);
        boton.getStyleClass().add("soft-button");
        boton.setOnAction(e -> Navigator.irASeccion(seccion));

        HBox tarjeta = new HBox(12, textos, boton);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.getStyleClass().add("stat-card");
        return tarjeta;
    }
}