package com.example.pantallas;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RegisterView {

    private final StackPane root = new StackPane();
    private final TextField nombreField = new TextField();
    private final TextField usuarioField = new TextField();
    private final TextField correoField = new TextField();
    private final PasswordField claveField = new PasswordField();
    private final TextField claveVisibleField = new TextField();
    private final ComboBox<String> rolCombo = new ComboBox<>();
    private final Region[] barras = {new Region(), new Region(), new Region()};
    private final HBox toast = new HBox(8);
    private final Label toastTexto = new Label();
    private final Label pieSeguridad = new Label("Acceso protegido por registro médico colegiado");
    private Button botonRegistrar;

    public RegisterView() {
        construir();
    }

    public Parent getRoot() {
        return root;
    }

    private void construir() {
        root.getStyleClass().add("app-root");

        Label izquierda = new Label("ANIMALIA PMS V2.4");
        izquierda.getStyleClass().add("top-meta");
        Label derecha = new Label("Conectado: Sede Central");
        derecha.getStyleClass().add("top-meta");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barraSuperior = new HBox(izquierda, espacio, derecha);
        barraSuperior.setPadding(new Insets(0, 4, 0, 4));

        pieSeguridad.getStyleClass().add("footer-text");
        pieSeguridad.setMaxWidth(Double.MAX_VALUE);
        pieSeguridad.setAlignment(Pos.CENTER);

        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("card", "card-compact");
        tarjeta.getChildren().addAll(
                crearEncabezado(),
                crearCampo("Nombre completo", "* Requerido", true,
                        configurar(nombreField, "ej. Dra. Elena Ramos Morales")),
                crearCampo("Usuario", "Identificador interno", false,
                        configurar(usuarioField, "ej. eramos")),
                crearCampo("Correo electrónico", null, false,
                        configurar(correoField, "ej. eramos@animaliaclinica.com")),
                crearCampoClave(),
                crearCampo("Rol asignado", null, false, crearRolCombo()),
                crearBotones(),
                crearToast(),
                pieSeguridad
        );

        VBox contenido = new VBox(10, barraSuperior, tarjeta);
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
        insignia.setMinSize(48, 48);
        insignia.setPrefSize(48, 48);
        insignia.setMaxSize(48, 48);

        Label marca = new Label("ANIMALIA");
        marca.getStyleClass().add("brand-name");
        Label etiquetaGestion = new Label("GESTIÓN");
        etiquetaGestion.getStyleClass().add("badge-soft");
        HBox filaMarca = new HBox(6, marca, etiquetaGestion);
        filaMarca.setAlignment(Pos.CENTER);

        Label titulo = new Label("Crear nueva cuenta");
        titulo.getStyleClass().add("app-title");
        Label subtitulo = new Label("Complete los datos del personal veterinario");
        subtitulo.getStyleClass().add("app-subtitle");

        VBox encabezado = new VBox(4, insignia, filaMarca, titulo, subtitulo);
        encabezado.setAlignment(Pos.CENTER);
        return encabezado;
    }

    private TextField configurar(TextField campo, String sugerencia) {
        campo.setPromptText(sugerencia);
        campo.getStyleClass().add("input-outline");
        return campo;
    }

    private VBox crearCampo(String titulo, String pista, boolean resaltada, Node control) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("field-label");

        HBox fila = new HBox(etiqueta);
        fila.setAlignment(Pos.CENTER_LEFT);
        if (pista != null) {
            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);
            Label textoPista = new Label(pista);
            textoPista.getStyleClass().add(resaltada ? "hint-accent" : "hint");
            fila.getChildren().addAll(espacio, textoPista);
        }
        return new VBox(4, fila, control);
    }

    private VBox crearCampoClave() {
        claveField.setPromptText("Mínimo 8 caracteres");
        claveField.getStyleClass().addAll("input-outline", "input-with-button");

        // Campo de texto normal que se muestra cuando se pulsa "Ver"
        claveVisibleField.textProperty().bindBidirectional(claveField.textProperty());
        claveVisibleField.getStyleClass().addAll("input-outline", "input-with-button");
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

        // Tres barritas que indican la fortaleza de la contraseña
        HBox cajaBarras = new HBox(6);
        for (Region barra : barras) {
            barra.getStyleClass().add("strength-bar");
            barra.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(barra, Priority.ALWAYS);
            cajaBarras.getChildren().add(barra);
        }
        claveField.textProperty().addListener((obs, anterior, nuevo) -> actualizarFortaleza(nuevo));

        return crearCampo("Contraseña", "Mínimo 8 caracteres", false,
                new VBox(8, contenedor, cajaBarras));
    }

    private void actualizarFortaleza(String valor) {
        int cuantas = 0;
        String clase = "";
        if (!valor.isEmpty()) {
            cuantas = 1;
            clase = "bar-weak";
        }
        if (valor.length() >= 6) {
            cuantas = 2;
            clase = "bar-medium";
        }
        if (valor.length() >= 8 && valor.matches(".*[A-Z].*") && valor.matches(".*[0-9].*")) {
            cuantas = 3;
            clase = "bar-strong";
        }
        for (int i = 0; i < barras.length; i++) {
            barras[i].getStyleClass().setAll("strength-bar");
            if (i < cuantas) {
                barras[i].getStyleClass().add(clase);
            }
        }
    }

    private ComboBox<String> crearRolCombo() {
        rolCombo.getItems().addAll(
                "Médico Veterinario Titular",
                "Auxiliar de Clínica / Enfermería",
                "Recepción y Atención al Cliente",
                "Administrador de Sede"
        );
        rolCombo.getSelectionModel().selectFirst();
        rolCombo.setMaxWidth(Double.MAX_VALUE);
        rolCombo.getStyleClass().add("combo-outline");
        return rolCombo;
    }

    private VBox crearBotones() {
        botonRegistrar = new Button("Registrar");
        botonRegistrar.getStyleClass().addAll("primary-button", "compact");
        botonRegistrar.setMaxWidth(Double.MAX_VALUE);
        botonRegistrar.setDefaultButton(true);
        botonRegistrar.setOnAction(e -> registrar());

        Button volver = new Button("←  Volver al inicio de sesión");
        volver.getStyleClass().add("secondary-button");
        volver.setMaxWidth(Double.MAX_VALUE);
        volver.setOnAction(e -> Navigator.irALogin());

        VBox cajaBotones = new VBox(8, botonRegistrar, volver);
        cajaBotones.setPadding(new Insets(4, 0, 0, 0));
        return cajaBotones;
    }

    private HBox crearToast() {
        Region punto = new Region();
        punto.getStyleClass().add("toast-dot");

        toastTexto.setWrapText(true);
        toastTexto.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(toastTexto, Priority.ALWAYS);

        toast.getStyleClass().add("toast");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getChildren().addAll(punto, toastTexto);
        toast.setVisible(false);
        toast.setManaged(false);
        return toast;
    }

    private void mostrarToast(String mensaje, boolean error) {
        toastTexto.setText(mensaje);
        toast.getStyleClass().removeAll("toast-ok", "toast-error");
        toast.getStyleClass().add(error ? "toast-error" : "toast-ok");
        toast.setVisible(true);
        toast.setManaged(true);
        // Se oculta el pie para que todo siga cabiendo en la ventana
        pieSeguridad.setVisible(false);
        pieSeguridad.setManaged(false);
    }

    private void registrar() {
        String correo = correoField.getText().trim();
        if (nombreField.getText().isBlank() || usuarioField.getText().isBlank()) {
            mostrarToast("Complete el nombre completo y el usuario.", true);
            return;
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            mostrarToast("Ingrese un correo electrónico válido.", true);
            return;
        }
        if (claveField.getText().length() < 8) {
            mostrarToast("La contraseña debe tener al menos 8 caracteres.", true);
            return;
        }

        botonRegistrar.setDisable(true);
        botonRegistrar.setText("Guardando...");

        PauseTransition guardado = new PauseTransition(Duration.millis(700));
        guardado.setOnFinished(e -> {
            mostrarToast("Cuenta creada. Redirigiendo a pantalla de acceso...", false);
            PauseTransition espera = new PauseTransition(Duration.millis(1200));
            espera.setOnFinished(ev -> Navigator.irALogin());
            espera.play();
        });
        guardado.play();
    }
}