package collections;

import java.util.Objects;

public class Pregunta {
    private static int contadorId = 1; // ID autoincremental
    private int idPregunta;
    private String pregunta;
    private String tipo;
    private String respuesta;

    public Pregunta(String pregunta, String tipo) {
        this.idPregunta = contadorId++; // Asigna un ID único y lo incrementa
        this.pregunta = pregunta;
        this.tipo = tipo;
        this.respuesta = null;
    }

    // Resto de la clase...



    // Constructor con respuesta
    public Pregunta(int idPregunta, String pregunta, String tipo, String respuesta) {
        this.idPregunta = idPregunta;
        this.pregunta = pregunta;
        this.tipo = tipo;
        this.respuesta = respuesta;
    }

    // Getters y Setters
    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    // Representación en texto para depuración
    @Override
    public String toString() {
        return "Pregunta{" +
                "idPregunta=" + idPregunta +
                ", pregunta='" + pregunta + '\'' +
                ", tipo='" + tipo + '\'' +
                ", respuesta='" + (respuesta != null ? respuesta : "Sin respuesta") + '\'' +
                '}';
    }

    // Métodos equals y hashCode para comparación en listas
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pregunta pregunta1 = (Pregunta) obj;
        return idPregunta == pregunta1.idPregunta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPregunta);
    }
}
