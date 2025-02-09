package Controller;

import collections.Pregunta;
import collections.Usuario;
import com.mongodb.client.MongoCollection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import collections.Encuesta;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.Dialogo;
import main.MongoDBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import main.Session;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EncuestasEmpleadosController {

    @FXML
    private Button addButton, backButton, deleteButton, modifiyButton;

    @FXML
    private TableView<Encuesta> empleadosTableView;

    @FXML
    private TableColumn<Encuesta, String> p1TableColumn, p2TableColumn, p3TableColumn, tituloTableColumn;

    @FXML
    private TextField busquedaEmpresaTextField;

    private MongoDBConnection dbConnection;
    private ObservableList<Encuesta> encuestasList;

    @FXML
    public void initialize() {
        dbConnection = new MongoDBConnection();

        tituloTableColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        p1TableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPreguntas().get(0).getPregunta()));

        p2TableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPreguntas().get(1).getPregunta()));

        p3TableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPreguntas().get(2).getPregunta()));

        cargarEncuestasDesdeMongo();

        busquedaEmpresaTextField.textProperty().addListener((observable, oldValue, newValue) -> BuscarEncuesta(newValue));

        mostrarRespuestasDeEncuesta(1);

        Usuario usuarioLogeado = Session.getInstance().getUsuarioLogeado();

        // Verificar si el usuario no es "root" y desactivar los botones
        if (usuarioLogeado != null && !"root".equals(usuarioLogeado.getNombre())) {
            addButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    @FXML
    private void onAddButtonAction(ActionEvent event) {
        boolean added = Dialogo.mostrarAgregarEncuestaDialog();
        if (added) {
            cargarEncuestasDesdeMongo();
        }
    }

    @FXML
    void onModifyAction(ActionEvent event) {
        Encuesta selectedEncuesta = empleadosTableView.getSelectionModel().getSelectedItem();
        if (selectedEncuesta == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor, selecciona una encuesta para modificar.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Dialog<Encuesta> dialog = new Dialog<>();
        dialog.setTitle("Modificar Encuesta");
        dialog.setHeaderText("Modifica los campos de la encuesta seleccionada");

        Label tituloLabel = new Label("Título:");
        TextField tituloField = new TextField(selectedEncuesta.getTitulo());

        Label descripcionLabel = new Label("Descripción:");
        TextField descripcionField = new TextField(selectedEncuesta.getDescripcion());

        Label pregunta1Label = new Label("Pregunta 1:");
        TextField pregunta1Field = new TextField(selectedEncuesta.getPreguntas().get(0).getPregunta());

        Label pregunta2Label = new Label("Pregunta 2:");
        TextField pregunta2Field = new TextField(selectedEncuesta.getPreguntas().get(1).getPregunta());

        Label pregunta3Label = new Label("Pregunta 3:");
        TextField pregunta3Field = new TextField(selectedEncuesta.getPreguntas().get(2).getPregunta());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(tituloLabel, 0, 0);
        grid.add(tituloField, 1, 0);
        grid.add(descripcionLabel, 0, 1);
        grid.add(descripcionField, 1, 1);
        grid.add(pregunta1Label, 0, 2);
        grid.add(pregunta1Field, 1, 2);
        grid.add(pregunta2Label, 0, 3);
        grid.add(pregunta2Field, 1, 3);
        grid.add(pregunta3Label, 0, 4);
        grid.add(pregunta3Field, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.addEventFilter(ActionEvent.ACTION, event1 -> {
            if (tituloField.getText().trim().isEmpty() || descripcionField.getText().trim().isEmpty() ||
                    pregunta1Field.getText().trim().isEmpty() || pregunta2Field.getText().trim().isEmpty() ||
                    pregunta3Field.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Todos los campos son obligatorios.", ButtonType.OK);
                alert.showAndWait();
                event1.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                selectedEncuesta.setTitulo(tituloField.getText());
                selectedEncuesta.setDescripcion(descripcionField.getText());
                selectedEncuesta.getPreguntas().get(0).setPregunta(pregunta1Field.getText());
                selectedEncuesta.getPreguntas().get(1).setPregunta(pregunta2Field.getText());
                selectedEncuesta.getPreguntas().get(2).setPregunta(pregunta3Field.getText());
                return selectedEncuesta;
            }
            return null;
        });

        Optional<Encuesta> result = dialog.showAndWait();
        result.ifPresent(encuesta -> {
            actualizarEncuestaEnBaseDeDatos(encuesta);
            cargarEncuestasDesdeMongo();
        });
    }

    private void actualizarEncuestaEnBaseDeDatos(Encuesta encuesta) {
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Encuesta");
        Document updatedDocument = new Document()
                .append("titulo", encuesta.getTitulo())
                .append("descripcion", encuesta.getDescripcion())
                .append("preguntas", List.of(
                        new Document("pregunta", encuesta.getPreguntas().get(0).getPregunta()).append("tipo", "texto"),
                        new Document("pregunta", encuesta.getPreguntas().get(1).getPregunta()).append("tipo", "texto"),
                        new Document("pregunta", encuesta.getPreguntas().get(2).getPregunta()).append("tipo", "texto")
                ))
                .append("fecha_creacion", encuesta.getFechaCreacion())
                .append("estado", encuesta.getEstado());

        collection.updateOne(new Document("_id", encuesta.getId()), new Document("$set", updatedDocument));
    }

    @FXML
    void onActionDeleteButton(ActionEvent event) {
        Encuesta selectedEncuesta = empleadosTableView.getSelectionModel().getSelectedItem();
        if (selectedEncuesta == null) {
            // Mostrar un mensaje de error si no se selecciona ninguna encuesta
            Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor, selecciona una encuesta para eliminar.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Mostrar un cuadro de confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar esta encuesta?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.YES) {
            return;
        }

        // Eliminar la encuesta de la base de datos
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Encuesta");
        collection.deleteOne(new Document("_id", selectedEncuesta.getId()));

        // Recargar la tabla para reflejar los cambios
        cargarEncuestasDesdeMongo();
    }

    @FXML
    void onResponderAction(ActionEvent actionEvent) {
        Encuesta encuestaSeleccionada = empleadosTableView.getSelectionModel().getSelectedItem();
        if (encuestaSeleccionada == null) {
            System.out.println("No se ha seleccionado ninguna encuesta.");
            return;
        }

        List<Pregunta> preguntas = encuestaSeleccionada.getPreguntas();
        if (preguntas.size() != 3) {
            System.out.println("Error: La encuesta no tiene exactamente 3 preguntas.");
            return;
        }

        // Crear el diálogo
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Responder Encuesta");
        dialog.setHeaderText("Responde las siguientes preguntas:");

        // Crear los labels y text fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        List<TextField> textFields = new ArrayList<>();
        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta pregunta = preguntas.get(i);
            Label label = new Label(pregunta.getPregunta() + ":");
            TextField textField = new TextField();
            grid.add(label, 0, i);
            grid.add(textField, 1, i);
            textFields.add(textField);
        }

        dialog.getDialogPane().setContent(grid);

        // Añadir botones de OK y Cancelar
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Convertir los resultados cuando se presiona OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                List<String> respuestas = new ArrayList<>();
                for (TextField textField : textFields) {
                    respuestas.add(textField.getText());
                }
                return respuestas;
            }
            return null;
        });

        boolean allAnswered;
        do {
            allAnswered = true;
            Optional<List<String>> result = dialog.showAndWait();
            if (result.isPresent()) {
                List<String> respuestas = result.get();
                if (respuestas.stream().anyMatch(String::isEmpty)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor, responde todas las preguntas.", ButtonType.OK);
                    alert.showAndWait();
                    allAnswered = false;
                } else {
                    guardarRespuestasEnBaseDeDatos(encuestaSeleccionada, respuestas);
                    System.out.println("Respuestas guardadas correctamente.");
                }
            } else {
                break; // Si el usuario cancela el diálogo, salir del bucle
            }
        } while (!allAnswered);
    }

    private void guardarRespuestasEnBaseDeDatos(Encuesta encuesta, List<String> respuestas) {
        MongoCollection<Document> respuestasCollection = MongoDBConnection.getDatabase().getCollection("respuestas");

        // Obtener el usuario logeado
        Usuario usuarioLogeado = Session.getInstance().getUsuarioLogeado();

        // Crear un array de respuestas donde cada una está asociada con su id_pregunta
        List<Document> respuestasDoc = new ArrayList<>();
        for (int i = 0; i < encuesta.getPreguntas().size(); i++) {
            Pregunta pregunta = encuesta.getPreguntas().get(i);
            String respuestaTexto = respuestas.get(i);

            Document respuestaItem = new Document()
                    .append("id_pregunta", pregunta.getIdPregunta())
                    .append("respuesta", respuestaTexto);

            respuestasDoc.add(respuestaItem);
        }

        // Generar un id_respuesta único para el documento principal
        long nuevoIdRespuesta = respuestasCollection.countDocuments() + 1;

        // Crear el documento final que tiene el id_encuesta, id_usuario y el array de respuestas
        Document respuestasFinal = new Document()
                .append("_id", nuevoIdRespuesta)  // El id_respuesta en el documento principal
                .append("id_encuesta", encuesta.getId())  // id_encuesta
                .append("id_usuario", usuarioLogeado.getId())  // id_usuario
                .append("respuestas", respuestasDoc);     // Array de respuestas

        // Insertar el documento con todas las respuestas en la colección "respuestas"
        respuestasCollection.insertOne(respuestasFinal);
    }

    private void mostrarRespuestasDeEncuesta(int idEncuesta) {
        // Obtener la colección de respuestas
        MongoCollection<Document> respuestasCollection = MongoDBConnection.getDatabase().getCollection("respuestas");

        // Buscar el documento de respuestas correspondiente a la encuesta seleccionada
        Document respuestasDoc = respuestasCollection.find(new Document("id_encuesta", idEncuesta)).first();

        if (respuestasDoc != null) {
            // Obtener el array de respuestas
            List<Document> respuestas = (List<Document>) respuestasDoc.get("respuestas");

            // Obtener la encuesta correspondiente para las preguntas
            Encuesta encuestaSeleccionada = obtenerEncuestaPorId(idEncuesta); // Método que deberías tener para obtener la encuesta
            List<Pregunta> preguntas = encuestaSeleccionada.getPreguntas();

            // Asegurarse de que el número de respuestas coincide con el número de preguntas
            if (respuestas.size() == preguntas.size()) {
                // Mostrar las preguntas y sus respuestas
                for (int i = 0; i < preguntas.size(); i++) {
                    Pregunta pregunta = preguntas.get(i);
                    String respuesta = respuestas.size() > i ? respuestas.get(i).getString("respuesta") : "No respondida";

                    System.out.println("Pregunta: " + pregunta.getPregunta());
                    System.out.println("Respuesta: " + respuesta);
                    System.out.println();
                }
            } else {
                System.out.println("Número de respuestas no coincide con el número de preguntas.");
            }
        } else {
            System.out.println("No se encontraron respuestas para la encuesta con ID: " + idEncuesta);
        }
    }

    private Encuesta obtenerEncuestaPorId(int idEncuesta) {
        // Para propósitos de este ejemplo, devolveremos una nueva encuesta con el ID
        return new Encuesta(idEncuesta, "Titulo de la encuesta", "Descripción",
                List.of(new Pregunta("Pregunta 1", "texto"),
                        new Pregunta("Pregunta 2", "texto"),
                        new Pregunta("Pregunta 3", "texto")),
                "2025-01-01", "activa");
    }



    private int generarIdEncuesta() {
        MongoCollection<Document> counterCollection = MongoDBConnection.getDatabase().getCollection("id_counter");

        // Intentar obtener el contador actual
        Document counterDoc = counterCollection.find(new Document("name", "encuesta")).first();

        int nextId;
        if (counterDoc == null) {
            // Si no existe el contador, inicializarlo en 1
            nextId = 1;
            counterCollection.insertOne(new Document("name", "encuesta").append("count", nextId));
        } else {
            // Si existe, incrementar el contador
            nextId = counterDoc.getInteger("count") + 1;
            counterCollection.updateOne(
                    new Document("name", "encuesta"),
                    new Document("$set", new Document("count", nextId))
            );
        }
        return nextId;
    }

    private void BuscarEncuesta(String busqueda) {
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Encuesta");
        List<Encuesta> filteredEncuesta = new ArrayList<>();

        // Crear un filtro para buscar encuestas cuyo título o descripción contenga el texto de búsqueda
        Document filter = new Document("$or", List.of(
                new Document("titulo", new Document("$regex", busqueda).append("$options", "i")),
                new Document("descripcion", new Document("$regex", busqueda).append("$options", "i"))
        ));

        // Consultar la base de datos con el filtro
        for (Document doc : collection.find(filter)) {

            List<Document> preguntasDoc = (List<Document>) doc.get("preguntas");
            List<Pregunta> preguntas = new ArrayList<>();
            for (Document p : preguntasDoc) {
                preguntas.add(new Pregunta(p.getString("pregunta"), p.getString("tipo")));
            }

            Encuesta encuesta = new Encuesta(
                    doc.getLong("_id").intValue(),
                    doc.getString("titulo"),
                    doc.getString("descripcion"),
                    preguntas,
                    doc.getString("fechaCreacion"),
                    doc.getString("estado")
            );
            filteredEncuesta.add(encuesta);
        }

        // Actualizar la lista de encuestas con los resultados filtrados
        encuestasList.setAll(filteredEncuesta);
    }

    @FXML
    void onBackButton(ActionEvent event) {
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

    private void cargarEncuestasDesdeMongo() {
        encuestasList = dbConnection.obtenerEncuestas();
        empleadosTableView.setItems(encuestasList);
    }
}