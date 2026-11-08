package com.example.pantallas;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public final class Navigator {

    private static Stage stage;

    private Navigator() {
    }

    public static void init(Stage ventana) {
        stage = ventana;
    }

    private static void mostrar(Parent root) {
        Scene escena = stage.getScene();
        if (escena == null) {
            escena = new Scene(root, 1100, 700);
            escena.getStylesheets().add(Navigator.class.getResource("animalia.css").toExternalForm());
            stage.setScene(escena);
        } else {
            escena.setRoot(root);
        }
    }

    public static void irALogin() {
        mostrar(new LoginView().getRoot());
    }

    public static void irARegistro() {
        mostrar(new RegisterView().getRoot());
    }

    public static void irAPanel() {
        mostrar(new DashboardView().getRoot());
    }

    public static void irAMascotas() {
        mostrar(new PetsView().getRoot());
    }

    public static void irAHistorial(PetsView.Mascota mascota) {
        mostrar(new HistoryView(mascota).getRoot());
    }

    public static void irACitas() {
        mostrar(new AppointmentsView().getRoot());
    }

    /** Navegación del menú lateral. Las pantallas que faltan avisan por ahora. */
    public static void irASeccion(String clave) {
        switch (clave) {
            case "inicio" -> irAPanel();
            case "mascotas" -> irAMascotas();
            case "citas" -> irACitas();
            default -> {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Animalia");
                alerta.setHeaderText(null);
                alerta.setContentText("Esta pantalla la construiremos en un próximo paso.");
                alerta.showAndWait();
            }
        }
    }
}