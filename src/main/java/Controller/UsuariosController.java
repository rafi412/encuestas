package Controller;

import collections.Usuario;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.Dialogo;
import main.MongoDBConnection;
import org.bson.Document;
import com.mongodb.client.MongoCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuariosController {

    @FXML
    private Button addButton, backButton, deleteButton, modifyButton;

    @FXML
    private TableView<Usuario> usuariosTableView;

    @FXML
    private TableColumn<Usuario, String> nombreTableColumn, apellidosTableColumn, correoTableColumn;

    @FXML
    private TableColumn<Usuario, Integer> edadTableColumn;

    @FXML
    private TextField buscarUsuarioTextField;

    @FXML
    private BorderPane root;

    private MongoDBConnection dbConnection;
    private ObservableList<Usuario> usuariosList;

    @FXML
    public void initialize() {
        dbConnection = new MongoDBConnection();
        usuariosList = FXCollections.observableArrayList(); // Inicializar la lista

        nombreTableColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidosTableColumn.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        correoTableColumn.setCellValueFactory(new PropertyValueFactory<>("correoE"));
        edadTableColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getEdad()).asObject());

        usuariosTableView.setItems(usuariosList); // Asignar la lista al TableView
        cargarUsuariosDesdeMongo();

        buscarUsuarioTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarUsuarios(newValue);
        });

        // Limpiar selección si se hace clic fuera de la tabla
        root.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!usuariosTableView.isHover()) {
                usuariosTableView.getSelectionModel().clearSelection();
            }
        });

        // Limpiar selección si se pulsa ESC
        root.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    usuariosTableView.getSelectionModel().clearSelection();
                    break;
                default:
                    break;
            }
        });

    }


    @FXML
    private void onAddButtonAction(ActionEvent event) {
        boolean added = Dialogo.mostrarAgregarUsuarioDialog();
        if (added) {
            cargarUsuariosDesdeMongo();
        }
    }

    @FXML
    void onModifyAction(ActionEvent event) {
        Usuario selectedUsuario = usuariosTableView.getSelectionModel().getSelectedItem();
        if (selectedUsuario == null) {
            // Mostrar mensaje de error si no se selecciona un usuario
            Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor, selecciona un usuario para modificar.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Crear el diálogo de modificación
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Modificar Usuario");
        dialog.setHeaderText("Modifica los campos del usuario seleccionado");

        // Crear los labels y text fields
        Label nombreLabel = new Label("Nombre:");
        TextField nombreField = new TextField(selectedUsuario.getNombre());

        Label apellidosLabel = new Label("Apellidos:");
        TextField apellidosField = new TextField(selectedUsuario.getApellidos());

        Label correoLabel = new Label("Correo:");
        TextField correoField = new TextField(selectedUsuario.getCorreoE());

        Label edadLabel = new Label("Edad:");
        TextField edadField = new TextField(String.valueOf(selectedUsuario.getEdad()));
        edadField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                edadField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Crear el layout del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(nombreLabel, 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(apellidosLabel, 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(correoLabel, 0, 2);
        grid.add(correoField, 1, 2);
        grid.add(edadLabel, 0, 3);
        grid.add(edadField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Añadir filtro de evento al botón OK
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.addEventFilter(ActionEvent.ACTION, event1 -> {
            if (nombreField.getText().trim().isEmpty() || apellidosField.getText().trim().isEmpty() ||
                    correoField.getText().trim().isEmpty() || edadField.getText().trim().isEmpty()) {
                // Mostrar alerta si algún campo está vacío
                Alert alert = new Alert(Alert.AlertType.ERROR, "Todos los campos son obligatorios.", ButtonType.OK);
                alert.showAndWait();
                event1.consume();
            } else if (!correoField.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "El correo no cumple con el esquema de un correo válido.", ButtonType.OK);
                alert.showAndWait();
                event1.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                selectedUsuario.setNombre(nombreField.getText());
                selectedUsuario.setApellidos(apellidosField.getText());
                selectedUsuario.setCorreoE(correoField.getText());
                selectedUsuario.setEdad(Integer.parseInt(edadField.getText()));
                return selectedUsuario;
            }
            return null;
        });

        Optional<Usuario> result;
        do {
            result = dialog.showAndWait();
        } while (result.isPresent() && result.get() == null);

        result.ifPresent(usuario -> {
            actualizarUsuarioEnBaseDeDatos(usuario);
            cargarUsuariosDesdeMongo(); // Recargar la tabla para reflejar los cambios
        });
    }

    @FXML
    void onActionDeleteButton(ActionEvent event) {
        Usuario selectedUsuario = usuariosTableView.getSelectionModel().getSelectedItem();
        if (selectedUsuario == null) {
            // Mostrar mensaje de error si no se selecciona un usuario
            Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor, selecciona un usuario para eliminar.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Mostrar cuadro de confirmación para eliminar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar este usuario?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.YES) {
            return;
        }

        // Eliminar el usuario de la base de datos
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Usuario");
        collection.deleteOne(new Document("_id", selectedUsuario.getId()));

        // Recargar la tabla para reflejar los cambios
        cargarUsuariosDesdeMongo();
    }

    @FXML
    void onBackAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/FXML/MainView.fxml"));

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
    }

    private void actualizarUsuarioEnBaseDeDatos(Usuario usuario) {
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Usuario");
        Document updatedDocument = new Document()
                .append("nombre", usuario.getNombre())
                .append("apellidos", usuario.getApellidos())
                .append("correo", usuario.getCorreoE())
                .append("edad", usuario.getEdad());

        collection.updateOne(new Document("_id", usuario.getId()), new Document("$set", updatedDocument));
    }

    private void cargarUsuariosDesdeMongo() {
        List<Usuario> usuarios = dbConnection.obtenerUsuarios();
        usuariosList.clear(); // Limpiar la lista antes de agregar nuevos datos
        usuariosList.addAll(usuarios);

    }

    private void filtrarUsuarios(String searchText) {
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Usuario");
        List<Usuario> filteredUsuarios = new ArrayList<>();

        // Crear un filtro de búsqueda
        Document filter = new Document("$or", List.of(
                new Document("nombre", new Document("$regex", searchText).append("$options", "i")),
                new Document("apellidos", new Document("$regex", searchText).append("$options", "i"))
        ));
        for (Document doc : collection.find(filter)) {
            Usuario usuario = new Usuario(
                    doc.getLong("_id").intValue(),
                    doc.getString("nombre"),
                    doc.getString("apellidos"),
                    doc.getInteger("edad"),
                    doc.getString("correo")

            );
            filteredUsuarios.add(usuario);
        }
        usuariosList.setAll(filteredUsuarios);
    }

}
