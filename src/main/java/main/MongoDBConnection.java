package main;

import com.mongodb.client.*;
import org.bson.Document;
import collections.Usuario;
import collections.Encuesta;
import collections.Pregunta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class MongoDBConnection {
    private static final String URI = "mongodb://localhost:27017"; // Cambia según tu configuración
    private static final String DATABASE_NAME = "miBaseDeDatos";
    private static final String ENCUESTA_COLLECTION = "Encuesta";
    private static final String USUARIOS_COLLECTION = "Usuario"; // Nueva colección para los usuarios

    private static final MongoClient mongoClient = MongoClients.create(URI);
    private static final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

    private final MongoCollection<Document> encuestaCollection;
    private final MongoCollection<Document> usuariosCollection;

    public MongoDBConnection() {
        encuestaCollection = database.getCollection(ENCUESTA_COLLECTION);
        usuariosCollection = database.getCollection(USUARIOS_COLLECTION);
    }



    // Métodos para Encuestas
    public ObservableList<Encuesta> obtenerEncuestas() {
        ObservableList<Encuesta> encuestasList = FXCollections.observableArrayList();

        FindIterable<Document> documentos = encuestaCollection.find();
        for (Document doc : documentos) {
            Object idObj = doc.get("_id");
            long idLong = 0;

            if (idObj instanceof Long) {
                idLong = (Long) idObj;
            } else if (idObj instanceof Integer) {
                idLong = ((Integer) idObj).longValue();
            }

            int id = (int) idLong;
            String titulo = doc.getString("titulo");
            String descripcion = doc.getString("descripcion");
            String fechaCreacion = doc.getString("fecha_creacion");
            String estado = doc.getString("estado");

            List<Pregunta> preguntas = new ArrayList<>();
            List<Document> preguntasDoc = (List<Document>) doc.get("preguntas");

            if (preguntasDoc != null) {
                for (Document p : preguntasDoc) {
                    preguntas.add(new Pregunta(p.getString("pregunta"), p.getString("tipo")));
                }
            }

            encuestasList.add(new Encuesta(id, titulo, descripcion, preguntas, fechaCreacion, estado));
        }

        return encuestasList;
    }

    // Métodos para Usuarios
    public ObservableList<Usuario> obtenerUsuarios() {
        ObservableList<Usuario> usuariosList = FXCollections.observableArrayList();

        FindIterable<Document> documentos = usuariosCollection.find();
        for (Document doc : documentos) {
            int id = doc.get("_id", Number.class).intValue(); // Convertir _id a int
            String nombre = doc.getString("nombre");
            String apellidos = doc.getString("apellidos");
            int edad = doc.getInteger("edad", 0); // Evitar valores null
            String correo = doc.getString("correo");

            Usuario usuario = new Usuario(id, nombre, apellidos, edad, correo);
            usuariosList.add(usuario);
        }
        return usuariosList;
    }

    public void agregarUsuario(Usuario usuario) {
        Document doc = new Document("_id", usuario.getId()) // Asegurar que tenga _id
                .append("nombre", usuario.getNombre())
                .append("apellidos", usuario.getApellidos())
                .append("edad", usuario.getEdad())
                .append("correo", usuario.getCorreoE());

        usuariosCollection.insertOne(doc);
    }

    public void actualizarUsuario(Usuario usuario) {
        Document updatedDoc = new Document("nombre", usuario.getNombre())
                .append("apellidos", usuario.getApellidos())
                .append("edad", usuario.getEdad())
                .append("correo", usuario.getCorreoE());

        usuariosCollection.updateOne(new Document("_id", usuario.getId()),
                new Document("$set", updatedDoc));
    }

    public Usuario obtenerUsuarioPorId(int idUsuario) {
        Document filtro = new Document("_id", idUsuario);
        Document doc = usuariosCollection.find(filtro).first();

        if (doc != null) {
            int id = doc.get("_id", Number.class).intValue();
            String nombre = doc.getString("nombre");
            String apellidos = doc.getString("apellidos");
            int edad = doc.getInteger("edad", 0);
            String correo = doc.getString("correo");

            return new Usuario(id, nombre, apellidos, edad, correo);
        }
        return null; // Usuario no encontrado
    }


    public void eliminarUsuario(Usuario usuario) {
        usuariosCollection.deleteOne(new Document("_id", usuario.getId()));
    }

    // Métodos para obtener la base de datos y colección
    public static MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public void cerrarConexion() {
        mongoClient.close();
    }
}
