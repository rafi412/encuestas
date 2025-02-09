package collections;

import java.util.List;

public class Respuesta {
    private int id;
    private int idEncuesta;
    private List<RespuestaItem> respuestas;

    // Constructor
    public Respuesta(int id, int idEncuesta, List<RespuestaItem> respuestas) {
        this.id = id;
        this.idEncuesta = idEncuesta;
        this.respuestas = respuestas;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEncuesta() {
        return idEncuesta;
    }

    public void setIdEncuesta(int idEncuesta) {
        this.idEncuesta = idEncuesta;
    }

    public List<RespuestaItem> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<RespuestaItem> respuestas) {
        this.respuestas = respuestas;
    }

    public static class RespuestaItem {
        private int idPregunta;
        private String respuesta;

        // Constructor
        public RespuestaItem(int idPregunta, String respuesta) {
            this.idPregunta = idPregunta;
            this.respuesta = respuesta;
        }

        // Getters y Setters
        public int getIdPregunta() {
            return idPregunta;
        }

        public void setIdPregunta(int idPregunta) {
            this.idPregunta = idPregunta;
        }

        public String getRespuesta() {
            return respuesta;
        }

        public void setRespuesta(String respuesta) {
            this.respuesta = respuesta;
        }
    }

}
