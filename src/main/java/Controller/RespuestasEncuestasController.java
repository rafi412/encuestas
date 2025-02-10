package Controller;

import com.mongodb.client.MongoCollection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.Stage;
import main.MongoDBConnection;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

public class RespuestasEncuestasController {

    @FXML
    private Button backButton;

    @FXML
    private TextField buscarPorEncuestaTextField;

    @FXML
    private TextField buscarPorUsuarioTextField;

    @FXML
    private TreeTableColumn<RespuestaView, String> usuarioTreeTableColumn;

    @FXML
    private TreeTableColumn<RespuestaView, String> encuestaTreeTableColumn;

    @FXML
    private TreeTableColumn<RespuestaView, String> preguntaTreeTableColumn;

    @FXML
    private TreeTableColumn<RespuestaView, String> respuestaTreeTableColumn;

    @FXML
    private TreeTableView<RespuestaView> statsTreeTableView;

    private TreeItem<RespuestaView> respuestasRaiz;

    @FXML
    public void initialize() {
        usuarioTreeTableColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("usuario"));
        encuestaTreeTableColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("encuesta"));
        preguntaTreeTableColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pregunta"));
        respuestaTreeTableColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("respuesta"));

        cargarRespuestasDesdeMongo();

        buscarPorEncuestaTextField.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        buscarPorUsuarioTextField.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
    }

    private void cargarRespuestasDesdeMongo() {
        MongoCollection<Document> respuestasCollection = MongoDBConnection.getDatabase().getCollection("respuestas");
        MongoCollection<Document> encuestasCollection = MongoDBConnection.getDatabase().getCollection("Encuesta");
        MongoCollection<Document> usuariosCollection = MongoDBConnection.getDatabase().getCollection("Usuario");

        TreeItem<RespuestaView> root = new TreeItem<>(new RespuestaView("Usuario", "Encuesta", "Pregunta", "Respuesta"));

        for (Document respuestaDoc : respuestasCollection.find()) {
            Integer idEncuesta = respuestaDoc.getInteger("id_encuesta");
            Integer idUsuario = respuestaDoc.getInteger("id_usuario");

            if (idEncuesta == null || idUsuario == null) continue;

            Document encuestaDoc = encuestasCollection.find(new Document("_id", idEncuesta)).first();
            Document usuarioDoc = usuariosCollection.find(new Document("_id", idUsuario)).first();

            if (encuestaDoc != null && usuarioDoc != null) {
                String usuario = usuarioDoc.getString("nombre");
                String encuesta = encuestaDoc.getString("titulo");

                List<Document> preguntas = (List<Document>) encuestaDoc.get("preguntas");
                List<Document> respuestas = (List<Document>) respuestaDoc.get("respuestas");

                TreeItem<RespuestaView> encuestaItem = new TreeItem<>(new RespuestaView(usuario, encuesta, "", ""));

                for (int i = 0; i < preguntas.size(); i++) {
                    Document pregunta = preguntas.get(i);
                    Document respuesta = respuestas.get(i);

                    String preguntaTexto = pregunta.getString("pregunta");
                    String respuestaTexto = respuesta.getString("respuesta");

                    TreeItem<RespuestaView> preguntaItem = new TreeItem<>(new RespuestaView("", "", preguntaTexto, respuestaTexto));
                    encuestaItem.getChildren().add(preguntaItem);
                }

                root.getChildren().add(encuestaItem);
            }
        }

        respuestasRaiz = root;
        statsTreeTableView.setRoot(root);
        statsTreeTableView.setShowRoot(false);
    }

    private void aplicarFiltros() {
        String filtroEncuesta = buscarPorEncuestaTextField.getText().toLowerCase();
        String filtroUsuario = buscarPorUsuarioTextField.getText().toLowerCase();

        TreeItem<RespuestaView> root = new TreeItem<>(new RespuestaView("Usuario", "Encuesta", "Pregunta", "Respuesta"));

        for (TreeItem<RespuestaView> encuestaItem : respuestasRaiz.getChildren()) {
            boolean coincideEncuesta = filtroEncuesta.isEmpty() || encuestaItem.getValue().getEncuesta().toLowerCase().contains(filtroEncuesta);
            boolean coincideUsuario = filtroUsuario.isEmpty() || encuestaItem.getValue().getUsuario().toLowerCase().contains(filtroUsuario);

            if (coincideEncuesta && coincideUsuario) {
                root.getChildren().add(encuestaItem);
            }
        }

        statsTreeTableView.setRoot(root);
    }

    @FXML
    void onBackAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainView.fxml"));
            loader.setController(new MainController());
            Parent mainView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.setTitle("Gestión de Encuestas");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class RespuestaView {
        private String usuario;
        private String encuesta;
        private String pregunta;
        private String respuesta;

        public RespuestaView(String usuario, String encuesta, String pregunta, String respuesta) {
            this.usuario = usuario;
            this.encuesta = encuesta;
            this.pregunta = pregunta;
            this.respuesta = respuesta;
        }

        public String getUsuario() {
            return usuario;
        }

        public String getEncuesta() {
            return encuesta;
        }

        public String getPregunta() {
            return pregunta;
        }

        public String getRespuesta() {
            return respuesta;
        }
    }
}