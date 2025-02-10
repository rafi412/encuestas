package main;

import Controller.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {

    public static Stage primaryStage;
    private LoginController controller = new LoginController();

    @Override
    public void start(Stage primaryStage) throws Exception {
        App.primaryStage = primaryStage;

        primaryStage.setTitle("Iniciar Sesión");
        primaryStage.setScene(new Scene(controller.getView()));

        primaryStage.show();
    }

}