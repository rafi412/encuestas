package Controller;

import collections.Usuario;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.Dialogo;
import main.Session;
import org.bson.Document;
import main.MongoDBConnection;

import java.io.IOException;
import java.util.List;

public class LoginController {

    @FXML
    private Button logInButton;

    @FXML
    private BorderPane loginView;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button registrarButton;

    @FXML
    private TextField userTextfield;

    private MongoDBConnection dbConnection;
    private ObservableList<Usuario> usuariosList;

    public LoginController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la vista de login", e);
        }
    }

    @FXML
    public void initialize() {
        dbConnection = new MongoDBConnection();
        usuariosList = FXCollections.observableArrayList();
    }

    // src/main/java/Controller/LoginController.java
    @FXML
    void handleLogIn(ActionEvent event) {
        String username = userTextfield.getText().trim();
        String password = passwordTextField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Error", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        // Conexión a MongoDB y obtención de la colección
        MongoDatabase database = MongoDBConnection.getDatabase();
        MongoCollection<Document> usersCollection = database.getCollection("Usuario");

        // Buscar usuario en la BD
        Document userDoc = usersCollection.find(new Document("nombre", username)).first();

        if (userDoc != null) {
            String storedPassword = userDoc.getString("contraseña");

            if (password.equals(storedPassword)) {
                // Almacenar el usuario logeado
                Usuario usuarioLogeado = new Usuario(
                        userDoc.getLong("_id").intValue(),
                        userDoc.getString("nombre"),
                        userDoc.getString("apellidos"),
                        userDoc.getInteger("edad"),
                        userDoc.getString("correo")
                );
                System.out.println("Usuario logeado: " + usuarioLogeado.getNombre());

                // Guardar el usuario logeado en la sesión
                Session.getInstance().setUsuarioLogeado(usuarioLogeado);

                // Continuar con la lógica de redirección
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                    MainController mainController = new MainController();
                    loader.setController(mainController);

                    Parent mainView = loader.load();

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    double width = stage.getWidth();
                    double height = stage.getHeight();

                    stage.setTitle("Gestión de Encuestas");
                    Scene scene = new Scene(mainView);
                    stage.setScene(scene);
                    stage.setWidth(width);
                    stage.setHeight(height);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                mostrarAlerta("Error", "Contraseña incorrecta.");
            }
        } else {
            mostrarAlerta("Error", "Usuario no encontrado.");
        }
    }

    @FXML
    void onActionRegistrarButton(ActionEvent event) {
        boolean added = Dialogo.mostrarAgregarUsuarioDialog();
        if (added) {
            cargarUsuariosDesdeMongo();
        }
    }

    private void cargarVista(String fxmlPath, ActionEvent event, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(titulo);
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarUsuariosDesdeMongo() {
        List<Usuario> usuarios = dbConnection.obtenerUsuarios();
        usuariosList.clear(); // Limpiar la lista antes de agregar nuevos datos
        usuariosList.addAll(usuarios);

    }

    public BorderPane getView() {
        return loginView;
    }
}
