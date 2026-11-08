package com.example.pantallas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView {

    // Credenciales de prueba (más adelante se reemplazan por una validación real)
    private static final String USUARIO_DEMO = "elena.ramos";
    private static final String CLAVE_DEMO = "12345678";

    private final StackPane root = new StackPane();
    private final TextField usuarioField = new TextField();
    private final PasswordField claveField = new PasswordField();
    private final TextField claveVisibleField = new TextField();
    private final HBox bannerError = new HBox(8);

    public LoginView() {
        construir();
    }

    public Parent getRoot() {
        return root;
    }

    private void construir() {
        root.getStyleClass().add("app-root");

        VBox tarjeta = new VBox(16);
        tarjeta.getStyleClass().add("card");
        tarjeta.getChildren().addAll(
                crearEncabezado(),
                crearCampoUsuario(),
                crearCampoClave(),
                crearBannerError(),
                crearBotonIngresar(),
                crearEnlaceCrearCuenta(),
                crearPuestoActivo()
        );

        Label pie = new Label("Animalia v1.2.0 • JavaFX Desktop Edition");
        pie.getStyleClass().add("footer-text");

        VBox contenido = new VBox(16, tarjeta, pie);
        contenido.setAlignment(Pos.CENTER);
        contenido.setMaxWidth(420);
        contenido.setMaxHeight(Region.USE_PREF_SIZE);

        root.getChildren().add(contenido);
    }

    private VBox crearEncabezado() {
        // "\uD83D\uDC3E" es el emoji de la huella de pata
        Label iconoPata = new Label("\uD83D\uDC3E");
        iconoPata.getStyleClass().add("logo-icon");

        StackPane insignia = new StackPane(iconoPata);
        insignia.getStyleClass().add("logo-badge");
        insignia.setMinSize(56, 56);
        insignia.setPrefSize(56, 56);
        insignia.setMaxSize(56, 56);

        Label titulo = new Label("Animalia");
        titulo.getStyleClass().add("app-title");

        Label subtitulo = new Label("Sistema de Gestión Veterinaria");
        subtitulo.getStyleClass().add("app-subtitle");

        VBox encabezado = new VBox(4, insignia, titulo, subtitulo);
        encabezado.setAlignment(Pos.CENTER);
        encabezado.setPadding(new Insets(0, 0, 8, 0));
        return encabezado;
    }

    private VBox crearCampoUsuario() {
        Label etiqueta = new Label("Usuario");
        etiqueta.getStyleClass().add("field-label");

        usuarioField.setPromptText("ej. elena.ramos");
        usuarioField.setText("elena.ramos");
        usuarioField.getStyleClass().add("input");

        return new VBox(4, etiqueta, usuarioField);
    }

    private VBox crearCampoClave() {
        Label etiqueta = new Label("Contraseña");
        etiqueta.getStyleClass().add("field-label");

        Hyperlink olvido = new Hyperlink("¿Olvidó su clave?");
        olvido.getStyleClass().add("link-small");
        olvido.setOnAction(e -> mostrarAviso("Aquí irá la recuperación de clave."));

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox filaEtiqueta = new HBox(etiqueta, espacio, olvido);
        filaEtiqueta.setAlignment(Pos.CENTER_LEFT);

        claveField.setPromptText("••••••••");
        claveField.getStyleClass().addAll("input", "input-with-button");

        // Campo de texto normal que se muestra cuando se pulsa "Ver"
        claveVisibleField.textProperty().bindBidirectional(claveField.textProperty());
        claveVisibleField.getStyleClass().addAll("input", "input-with-button");
        claveVisibleField.setVisible(false);
        claveVisibleField.setManaged(false);

        Button ojo = new Button("Ver");
        ojo.getStyleClass().add("eye-button");
        ojo.setFocusTraversable(false);
        StackPane.setAlignment(ojo, Pos.CENTER_RIGHT);
        StackPane.setMargin(ojo, new Insets(0, 6, 0, 0));
        ojo.setOnAction(e -> {
            boolean mostrar = !claveVisibleField.isVisible();
            claveVisibleField.setVisible(mostrar);
            claveVisibleField.setManaged(mostrar);
            claveField.setVisible(!mostrar);
            claveField.setManaged(!mostrar);
            ojo.setText(mostrar ? "Ocultar" : "Ver");
        });

        StackPane contenedor = new StackPane(claveField, claveVisibleField, ojo);
        return new VBox(4, filaEtiqueta, contenedor);
    }

    private HBox crearBannerError() {
        Label icono = new Label("!");
        icono.getStyleClass().add("banner-icon");

        Label mensaje = new Label("Credenciales incorrectas. Verifique su usuario o contraseña.");
        mensaje.getStyleClass().add("banner-text");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(mensaje, Priority.ALWAYS);

        bannerError.getStyleClass().add("error-banner");
        bannerError.setAlignment(Pos.CENTER_LEFT);
        bannerError.getChildren().addAll(icono, mensaje);
        mostrarError(false);
        return bannerError;
    }

    private Button crearBotonIngresar() {
        Button boton = new Button("Iniciar sesión  →");
        boton.getStyleClass().add("primary-button");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setDefaultButton(true);
        boton.setOnAction(e -> iniciarSesion());
        return boton;
    }

    private HBox crearEnlaceCrearCuenta() {
        Label texto = new Label("¿No tienes cuenta?");
        texto.getStyleClass().add("muted-text");

        Hyperlink enlace = new Hyperlink("Crear cuenta");
        enlace.getStyleClass().add("link-bold");
        enlace.setOnAction(e -> Navigator.irARegistro());

        HBox fila = new HBox(4, texto, enlace);
        fila.setAlignment(Pos.CENTER);
        fila.setPadding(new Insets(8, 0, 0, 0));
        return fila;
    }

    private HBox crearPuestoActivo() {
        Label etiqueta = new Label("Puesto activo:");
        etiqueta.getStyleClass().add("role-label");

        Label chip = new Label("Recepción / Consultas");
        chip.getStyleClass().add("chip");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox caja = new HBox(etiqueta, espacio, chip);
        caja.setAlignment(Pos.CENTER_LEFT);
        caja.getStyleClass().add("role-box");
        return caja;
    }

    private void iniciarSesion() {
        boolean correcto = usuarioField.getText().trim().equals(USUARIO_DEMO)
                && claveField.getText().equals(CLAVE_DEMO);
        mostrarError(!correcto);
        if (correcto) {
            Navigator.irAPanel();
        }
    }

    private void mostrarError(boolean mostrar) {
        bannerError.setVisible(mostrar);
        bannerError.setManaged(mostrar);
    }

    private void mostrarAviso(String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Animalia");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}