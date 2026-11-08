module com.example.pantallas {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.pantallas to javafx.fxml;
    exports com.example.pantallas;
}