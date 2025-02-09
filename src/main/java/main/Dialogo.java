package main;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.bson.Document;

import org.controlsfx.control.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dialogo {

    public static boolean mostrarAgregarEncuestaDialog() {
        // Crear el diálogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Encuesta");
        dialog.setHeaderText("Añadir Nueva Encuesta");

        // Crear los labels y text fields
        Label tituloLabel = new Label("Título:");
        TextField tituloField = new TextField();

        Label descripcionLabel = new Label("Descripción:");
        TextField descripcionField = new TextField();

        Label pregunta1Label = new Label("Pregunta 1:");
        TextField pregunta1Field = new TextField();

        Label pregunta2Label = new Label("Pregunta 2:");
        TextField pregunta2Field = new TextField();

        Label pregunta3Label = new Label("Pregunta 3:");
        TextField pregunta3Field = new TextField();

        // Crear el layout del diálogo
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

        // Añadir botones de OK y Cancelar
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Convertir los resultados cuando se presiona OK
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

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            // Guardar en la base de datos
            agregarEncuestaABaseDeDatos(tituloField.getText(), descripcionField.getText(),
                    List.of(pregunta1Field.getText(), pregunta2Field.getText(), pregunta3Field.getText()));
            return true;
        }
        return false;
    }


    private static void agregarEncuestaABaseDeDatos(String titulo, String descripcion, List<String> preguntas) {
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Encuesta");

        // Obtener el nuevo ID para la encuesta basado en el número de documentos existentes
        long nuevoIdEncuesta = collection.countDocuments() + 1;

        // Generar un ID autonumérico para cada pregunta de la encuesta
        List<Document> preguntasDocs = new ArrayList<>();
        for (int i = 0; i < preguntas.size(); i++) {
            // ID autonumérico para cada pregunta dentro de la encuesta
            int idPregunta = i + 1; // Asignar un ID basado en el índice de la lista (comienza en 1)

            preguntasDocs.add(new Document()
                    .append("_id", idPregunta) // ID único autonumérico para cada pregunta
                    .append("pregunta", preguntas.get(i))
                    .append("tipo", "texto"));
        }
        Document ultimaEncuesta = collection.find().sort(Sorts.descending("_id")).first();
        long nuevoId = (ultimaEncuesta != null) ? ultimaEncuesta.getLong("_id") + 1 : 1;
        // Crear el documento de la encuesta
        Document nuevaEncuesta = new Document()
                .append("_id", nuevoId)  // ID único para la encuesta
                .append("titulo", titulo)
                .append("descripcion", descripcion)
                .append("preguntas", preguntasDocs)
                .append("fecha_creacion", java.time.LocalDate.now().toString())
                .append("estado", "activa");

        // Insertar la nueva encuesta en la base de datos
        collection.insertOne(nuevaEncuesta);
    }

    public static boolean mostrarAgregarUsuarioDialog() {
        // Crear el diálogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Añadir Usuario");

        // Crear los labels y text fields
        Label nombreLabel = new Label("Nombre:");
        TextField nombreField = new TextField();

        Label apellidosLabel = new Label("Apellidos:");
        TextField apellidosField = new TextField();

        Label correoLabel = new Label("Correo:");
        TextField correoField = new TextField();

        Label edadLabel = new Label("Edad:");
        TextField edadField = new TextField();
        edadField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                edadField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Label contrasenaLabel = new Label("Contraseña:");
        PasswordField contrasenaField = new PasswordField();

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
        grid.add(contrasenaLabel, 0, 4);
        grid.add(contrasenaField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Añadir botones de OK y Cancelar
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Añadir filtro de evento al botón OK
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.addEventFilter(ActionEvent.ACTION, event1 -> {
            if (nombreField.getText().trim().isEmpty() || apellidosField.getText().trim().isEmpty() ||
                    correoField.getText().trim().isEmpty() || edadField.getText().trim().isEmpty() ||
                    contrasenaField.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Todos los campos son obligatorios.", ButtonType.OK);
                alert.showAndWait();
                event1.consume();
            } else if (!correoField.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "El correo no cumple con el esquema de un correo válido.", ButtonType.OK);
                alert.showAndWait();
                event1.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            agregarUsuarioABaseDeDatos(nombreField.getText(), apellidosField.getText(), correoField.getText(),
                    Integer.parseInt(edadField.getText()), contrasenaField.getText());
            return true;
        }

        return false;
    }

    private static void agregarUsuarioABaseDeDatos(String nombre, String apellidos, String correo, int edad, String contrasena) {

        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Usuario");

        // Obtener el último documento insertado y su ID
        Document ultimoUsuario = collection.find().sort(Sorts.descending("_id")).first();
        long nuevoId = (ultimoUsuario != null) ? ultimoUsuario.getLong("_id") + 1 : 1;

        Document nuevoUsuario = new Document()
                .append("_id", nuevoId)
                .append("nombre", nombre)
                .append("apellidos", apellidos)
                .append("correo", correo)
                .append("edad", edad)
                .append("contrasena", contrasena);

        collection.insertOne(nuevoUsuario);
    }

    private static void agregarUsuarioABaseDeDatos(String nombre, String apellidos, String correo, int edad) {
        MongoCollection<Document> collection = MongoDBConnection.getDatabase().getCollection("Usuario");

        Document nuevoUsuario = new Document()
                .append("_id", collection.countDocuments() + 1)
                .append("nombre", nombre)
                .append("apellidos", apellidos)
                .append("correo", correo)
                .append("edad", edad);

        collection.insertOne(nuevoUsuario);
    }
}
