package main;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Dialogo {

    public static boolean mostrarAgregarEncuestaDialog() {
        // Solicitar el título de la encuesta
        TextInputDialog tituloDialog = new TextInputDialog();
        tituloDialog.setTitle("Nueva Encuesta");
        tituloDialog.setHeaderText("Añadir Título de la Encuesta");
        tituloDialog.setContentText("Título:");

        Optional<String> titulo = tituloDialog.showAndWait();
        if (titulo.isEmpty() || titulo.get().trim().isEmpty()) return false;

        // Solicitar la descripción de la encuesta
        TextInputDialog descripcionDialog = new TextInputDialog();
        descripcionDialog.setTitle("Descripción de la Encuesta");
        descripcionDialog.setHeaderText("Añadir Descripción");
        descripcionDialog.setContentText("Descripción:");

        Optional<String> descripcion = descripcionDialog.showAndWait();
        if (descripcion.isEmpty() || descripcion.get().trim().isEmpty()) return false;

        // Crear lista para las preguntas
        List<String> preguntas = new ArrayList<>();

        // Añadir 3 preguntas obligatorias
        while (preguntas.size() < 3) {
            TextInputDialog preguntaDialog = new TextInputDialog();
            preguntaDialog.setTitle("Pregunta de la Encuesta");
            preguntaDialog.setHeaderText("Añadir Pregunta");
            preguntaDialog.setContentText("Pregunta " + (preguntas.size() + 1) + ":");

            Optional<String> pregunta = preguntaDialog.showAndWait();
            if (pregunta.isEmpty() || pregunta.get().trim().isEmpty()) {
                // Si la pregunta no es válida, mostrar mensaje y continuar con la misma pregunta
                Alert alert = new Alert(Alert.AlertType.ERROR, "La pregunta no puede estar vacía. Por favor, ingresa una pregunta válida.", ButtonType.OK);
                alert.showAndWait();
            } else {
                preguntas.add(pregunta.get().trim());
            }
        }

        // Confirmación antes de guardar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Deseas añadir esta encuesta?\n\nTítulo: " + titulo.get() +
                        "\nDescripción: " + descripcion.get() +
                        "\nPreguntas: " + String.join(", ", preguntas),
                ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.YES) return false;

        // Guardar en la base de datos
        agregarEncuestaABaseDeDatos(titulo.get(), descripcion.get(), preguntas);
        return true;
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

        // Crear el documento de la encuesta
        Document nuevaEncuesta = new Document()
                .append("_id", nuevoIdEncuesta)  // ID único para la encuesta
                .append("titulo", titulo)
                .append("descripcion", descripcion)
                .append("preguntas", preguntasDocs)
                .append("fecha_creacion", java.time.LocalDate.now().toString())
                .append("estado", "activa");

        // Insertar la nueva encuesta en la base de datos
        collection.insertOne(nuevaEncuesta);
    }

    public static boolean mostrarAgregarUsuarioDialog() {
        // Solicitar el nombre del usuario
        TextInputDialog nombreDialog = new TextInputDialog();
        nombreDialog.setTitle("Nuevo Usuario");
        nombreDialog.setHeaderText("Añadir Usuario");
        nombreDialog.setContentText("Nombre:");

        Optional<String> nombre = nombreDialog.showAndWait();
        if (nombre.isEmpty() || nombre.get().trim().isEmpty()) return false;

        // Solicitar los apellidos del usuario
        TextInputDialog apellidosDialog = new TextInputDialog();
        apellidosDialog.setTitle("Apellidos del Usuario");
        apellidosDialog.setHeaderText("Añadir Apellidos");
        apellidosDialog.setContentText("Apellidos:");

        Optional<String> apellidos = apellidosDialog.showAndWait();
        if (apellidos.isEmpty() || apellidos.get().trim().isEmpty()) return false;

        // Solicitar el correo electrónico del usuario
        TextInputDialog correoDialog = new TextInputDialog();
        correoDialog.setTitle("Correo del Usuario");
        correoDialog.setHeaderText("Añadir Correo");
        correoDialog.setContentText("Correo:");

        Optional<String> correo = correoDialog.showAndWait();
        if (correo.isEmpty() || correo.get().trim().isEmpty()) return false;

        // Solicitar la edad del usuario
        TextInputDialog edadDialog = new TextInputDialog();
        edadDialog.setTitle("Edad del Usuario");
        edadDialog.setHeaderText("Añadir Edad");
        edadDialog.setContentText("Edad:");

        Optional<String> edadStr = edadDialog.showAndWait();
        if (edadStr.isEmpty() || edadStr.get().trim().isEmpty()) return false;

        int edad;
        try {
            edad = Integer.parseInt(edadStr.get());
        } catch (NumberFormatException e) {
            // Si la edad no es un número válido, mostrar mensaje de error
            Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor, ingrese una edad válida.", ButtonType.OK);
            alert.showAndWait();
            return false;
        }

        // Confirmación antes de guardar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Deseas añadir este usuario?\n\nNombre: " + nombre.get() +
                        "\nApellidos: " + apellidos.get() +
                        "\nCorreo: " + correo.get() +
                        "\nEdad: " + edad,
                ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.YES) return false;

        // Guardar en la base de datos
        agregarUsuarioABaseDeDatos(nombre.get(), apellidos.get(), correo.get(), edad);
        return true;
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
