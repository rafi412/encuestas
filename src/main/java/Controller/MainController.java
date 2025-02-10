package Controller;

import collections.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.Session;

import java.io.IOException;

public class MainController {

    @FXML
    private Button buttonAdmin;

    @FXML
    private Button buttonUsuario;

    @FXML
    private Button buttonRespuestas;

    @FXML
    private BorderPane mainPane;

    public MainController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    public void initialize() {
        Usuario usuarioLogeado = Session.getInstance().getUsuarioLogeado();

        // Verificar si el usuario no es "root" y desactivar los botones
        if (usuarioLogeado != null && !"root".equals(usuarioLogeado.getNombre())) {
            buttonUsuario.setDisable(true);
            buttonRespuestas.setDisable(true);
        }
    }

    @FXML
    void onButtonAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/FXML/encuestasEmpleadosView.fxml"));

            EncuestasEmpleadosController encuestasEmpleadosController = new EncuestasEmpleadosController();
            loader.setController(encuestasEmpleadosController);

            Parent encuestasEmpleadosView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setTitle("Encuestas");
            Scene scene = new Scene(encuestasEmpleadosView);
            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onButtonUsuario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/FXML/usuariosView.fxml"));

            UsuariosController usuariosController = new UsuariosController();
            loader.setController(usuariosController);

            Parent usuariosView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setTitle("Usuarios");
            Scene scene = new Scene(usuariosView);
            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onButtonRespuestas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/FXML/respuestasEncuestasView.fxml"));

            RespuestasEncuestasController respuestasEncuestasController= new RespuestasEncuestasController();
            loader.setController(respuestasEncuestasController);

            Parent respuestasView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setTitle("Respuestas de Encuestas");
            Scene scene = new Scene(respuestasView);
            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BorderPane getView() {
        return mainPane;
    }

}