package collections;

import java.util.Collections;
import java.util.List;

public class Encuesta {
    private int id;
    private String titulo;
    private String descripcion;
    private List<Pregunta> preguntas;
    private String fechaCreacion;
    private String estado;
    private String respuestas;

    public Encuesta(int id, String titulo, String descripcion, List<Pregunta> preguntas, String fechaCreacion, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.preguntas = preguntas;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.respuestas = String.valueOf(List.of("", "", ""));
    }

    public Encuesta(String titulo, String pregunta1, String pregunta2, String pregunta3) {
        this.titulo = titulo;
        this.preguntas = List.of(
                new Pregunta(pregunta1, "texto"),
                new Pregunta(pregunta2, "texto"),
                new Pregunta(pregunta3, "texto")
        );
    }


    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public List<String> getRespuestas() {
        return Collections.singletonList(respuestas);
    }

    public String getEstado() {
        return estado;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setRespuestas(List<String> respuestas) {
        if (respuestas.size() == 3) {
            this.respuestas = String.valueOf(respuestas);
        } else {
            System.out.println("Error: Deben ser exactamente 3 respuestas.");
        }
    }
}
