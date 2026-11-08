package com.example.pantallas;

import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Animalia Management Suite");
        Navigator.init(stage);
        Navigator.irALogin();
        stage.show();
    }
}