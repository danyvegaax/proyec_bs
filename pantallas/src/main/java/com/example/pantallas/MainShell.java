package com.example.pantallas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Marco común de las pantallas 3 a 10: barra lateral + cabecera + contenido.
 */
public class MainShell {

    private static final String[][] SECCIONES = {
            {"inicio", "Inicio"},
            {"mascotas", "Mascotas"},
            {"citas", "Citas"},
            {"vacunas", "Vacunas"},
            {"medicamentos", "Medicamentos"},
            {"veterinarios", "Veterinarios"},
            {"reportes", "Reportes"}
    };

    private final BorderPane root = new BorderPane();

    public MainShell(String seccionActiva, String tituloActual, Node contenido) {
        root.getStyleClass().add("app-root");
        root.setLeft(crearBarraLateral(seccionActiva));

        ScrollPane desplazable = new ScrollPane(contenido);
        desplazable.setFitToWidth(true);
        desplazable.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazable.getStyleClass().add("page-scroll");

        BorderPane derecha = new BorderPane();
        derecha.setTop(crearCabecera(tituloActual));
        derecha.setCenter(desplazable);
        root.setCenter(derecha);
    }

    public Parent getRoot() {
        return root;
    }

    private VBox crearBarraLateral(String seccionActiva) {
        // Marca
        Label pata = new Label("\uD83D\uDC3E");
        pata.getStyleClass().add("sidebar-paw");
        StackPane insignia = new StackPane(pata);
        insignia.getStyleClass().add("sidebar-badge");
        insignia.setMinSize(36, 36);
        insignia.setPrefSize(36, 36);
        insignia.setMaxSize(36, 36);

        Label nombre = new Label("Animalia");
        nombre.getStyleClass().add("sidebar-title");
        Label lema = new Label("Gestión Veterinaria");
        lema.getStyleClass().add("sidebar-subtitle");

        HBox marca = new HBox(10, insignia, new VBox(2, nombre, lema));
        marca.setAlignment(Pos.CENTER_LEFT);
        marca.setPadding(new Insets(0, 16, 24, 16));

        // Menú
        VBox menu = new VBox(4);
        menu.setPadding(new Insets(0, 8, 0, 8));
        for (String[] seccion : SECCIONES) {
            Button item = new Button(seccion[1]);
            item.getStyleClass().add("nav-item");
            if (seccion[0].equals(seccionActiva)) {
                item.getStyleClass().add("nav-item-active");
            }
            item.setMaxWidth(Double.MAX_VALUE);
            item.setOnAction(e -> Navigator.irASeccion(seccion[0]));
            menu.getChildren().add(item);
        }

        // Usuario y cierre de sesión
        Label inicial = new Label("ER");
        inicial.getStyleClass().add("avatar-letter-dark");
        StackPane avatar = new StackPane(inicial);
        avatar.getStyleClass().add("avatar-light");
        avatar.setMinSize(32, 32);
        avatar.setPrefSize(32, 32);
        avatar.setMaxSize(32, 32);

        Label usuario = new Label("Dra. Elena Ramos");
        usuario.getStyleClass().add("user-name");
        Label rol = new Label("Veterinaria Principal");
        rol.getStyleClass().add("user-role");

        HBox cajaUsuario = new HBox(8, avatar, new VBox(2, usuario, rol));
        cajaUsuario.setAlignment(Pos.CENTER_LEFT);
        cajaUsuario.getStyleClass().add("user-box");

        Button salir = new Button("Cerrar sesión");
        salir.getStyleClass().add("logout-button");
        salir.setMaxWidth(Double.MAX_VALUE);
        salir.setOnAction(e -> Navigator.irALogin());

        VBox inferior = new VBox(8, cajaUsuario, salir);
        inferior.setPadding(new Insets(0, 8, 0, 8));

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);

        VBox barra = new VBox(marca, menu, espacio, inferior);
        barra.getStyleClass().add("sidebar");
        barra.setMinWidth(220);
        barra.setPrefWidth(220);
        barra.setMaxWidth(220);
        return barra;
    }

    private HBox crearCabecera(String tituloActual) {
        Label raiz = new Label("Animalia");
        raiz.getStyleClass().add("crumb");
        Label separador = new Label("›");
        separador.getStyleClass().add("crumb");
        Label actual = new Label(tituloActual);
        actual.getStyleClass().add("crumb-current");
        HBox migas = new HBox(8, raiz, separador, actual);
        migas.setAlignment(Pos.CENTER_LEFT);

        Region punto = new Region();
        punto.getStyleClass().add("dot-primary");
        Label estado = new Label("Clínica San Borja Activa");
        estado.getStyleClass().add("status-text");
        HBox filaEstado = new HBox(6, punto, estado);
        filaEstado.setAlignment(Pos.CENTER_LEFT);

        Label inicial = new Label("ER");
        inicial.getStyleClass().add("avatar-letter-light");
        StackPane avatar = new StackPane(inicial);
        avatar.getStyleClass().add("avatar-dark");
        avatar.setMinSize(32, 32);
        avatar.setPrefSize(32, 32);
        avatar.setMaxSize(32, 32);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox cabecera = new HBox(16, migas, espacio, filaEstado, avatar);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.getStyleClass().add("top-header");
        return cabecera;
    }
}