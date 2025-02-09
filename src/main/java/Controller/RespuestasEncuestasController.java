package Controller;

import com.mongodb.client.MongoCollection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.MongoDBConnection;
import org.bson.Document;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RespuestasEncuestasController {

    @FXML
    private Button backButton;

    @FXML
    private TextField buscarPorEncuestaTextField;

    @FXML
    private TextField buscarPorUsuarioTextField;

    @FXML
    private TableColumn<RespuestaView, String> encuestaTableColumn;

    @FXML
    private TableColumn<RespuestaView, String> usuarioTableColumn;

    @FXML
    private TableColumn<RespuestaView, String> respuesta1TableColumn;

    @FXML
    private TableColumn<RespuestaView, String> respuesta2TableColumn;

    @FXML
    private TableColumn<RespuestaView, String> respuesta3TableColumn1;

    @FXML
    private TableView<RespuestaView> statsTableView;

    private ObservableList<RespuestaView> respuestasList;

    @FXML
    public void initialize() {
        respuestasList = FXCollections.observableArrayList();

        encuestaTableColumn.setCellValueFactory(new PropertyValueFactory<>("tituloEncuesta"));
        usuarioTableColumn.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        respuesta1TableColumn.setCellValueFactory(new PropertyValueFactory<>("respuesta1"));
        respuesta2TableColumn.setCellValueFactory(new PropertyValueFactory<>("respuesta2"));
        respuesta3TableColumn1.setCellValueFactory(new PropertyValueFactory<>("respuesta3"));

        cargarRespuestasDesdeMongo();
        statsTableView.setItems(respuestasList);

        buscarPorUsuarioTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarRespuestasPorUsuario(newValue);
        });

        buscarPorEncuestaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarRespuestasPorEncuesta(newValue);
        });
    }

    private void cargarRespuestasDesdeMongo() {
        MongoCollection<Document> respuestasCollection = MongoDBConnection.getDatabase().getCollection("respuestas");
        MongoCollection<Document> encuestasCollection = MongoDBConnection.getDatabase().getCollection("Encuesta");
        MongoCollection<Document> usuariosCollection = MongoDBConnection.getDatabase().getCollection("Usuario");

        for (Document respuestaDoc : respuestasCollection.find()) {
            int idEncuesta = respuestaDoc.getInteger("id_encuesta");
            int idUsuario = respuestaDoc.getInteger("id_usuario");

            Document encuestaDoc = encuestasCollection.find(new Document("_id", idEncuesta)).first();
            Document usuarioDoc = usuariosCollection.find(new Document("_id", idUsuario)).first();

            if (encuestaDoc != null && usuarioDoc != null) {
                String tituloEncuesta = encuestaDoc.getString("titulo");
                String nombreUsuario = usuarioDoc.getString("nombre");

                List<Document> respuestas = (List<Document>) respuestaDoc.get("respuestas");
                String respuesta1 = respuestas.size() > 0 ? respuestas.get(0).getString("respuesta") : "";
                String respuesta2 = respuestas.size() > 1 ? respuestas.get(1).getString("respuesta") : "";
                String respuesta3 = respuestas.size() > 2 ? respuestas.get(2).getString("respuesta") : "";

                RespuestaView respuestaView = new RespuestaView(idEncuesta, tituloEncuesta, nombreUsuario, respuesta1, respuesta2, respuesta3);
                respuestasList.add(respuestaView);
            }
        }

        // Sort the list by the survey ID
        respuestasList.sort(Comparator.comparingInt(RespuestaView::getIdEncuesta));
    }

    private void filtrarRespuestasPorUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            statsTableView.setItems(respuestasList);
        } else {
            List<RespuestaView> filteredList = respuestasList.stream()
                    .filter(respuesta -> respuesta.getNombreUsuario().toLowerCase().contains(nombreUsuario.toLowerCase()))
                    .collect(Collectors.toList());
            statsTableView.setItems(FXCollections.observableArrayList(filteredList));
        }
    }

    private void filtrarRespuestasPorEncuesta(String tituloEncuesta) {
        if (tituloEncuesta == null || tituloEncuesta.isEmpty()) {
            statsTableView.setItems(respuestasList);
        } else {
            List<RespuestaView> filteredList = respuestasList.stream()
                    .filter(respuesta -> respuesta.getTituloEncuesta().toLowerCase().contains(tituloEncuesta.toLowerCase()))
                    .collect(Collectors.toList());
            statsTableView.setItems(FXCollections.observableArrayList(filteredList));
        }
    }

    @FXML
    void onBackAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            loader.setController(new MainController()); // Especificar el controlador
            Parent mainView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class RespuestaView {
        private int idEncuesta;
        private String tituloEncuesta;
        private String nombreUsuario;
        private String respuesta1;
        private String respuesta2;
        private String respuesta3;

        public RespuestaView(int idEncuesta, String tituloEncuesta, String nombreUsuario, String respuesta1, String respuesta2, String respuesta3) {
            this.idEncuesta = idEncuesta;
            this.tituloEncuesta = tituloEncuesta;
            this.nombreUsuario = nombreUsuario;
            this.respuesta1 = respuesta1;
            this.respuesta2 = respuesta2;
            this.respuesta3 = respuesta3;
        } //dasdas

        public int getIdEncuesta() {
            return idEncuesta;
        }

        public String getTituloEncuesta() {
            return tituloEncuesta;
        }

        public String getNombreUsuario() {
            return nombreUsuario;
        }

        public String getRespuesta1() {
            return respuesta1;
        }

        public String getRespuesta2() {
            return respuesta2;
        }

        public String getRespuesta3() {
            return respuesta3;
        }
    }
}