// src/main/java/main/Session.java
package main;

import collections.Usuario;

public class Session {
    private static Session instance;
    private Usuario usuarioLogeado;

    private Session() {
        // Constructor privado para evitar instanciación
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public Usuario getUsuarioLogeado() {
        return usuarioLogeado;
    }

    public void setUsuarioLogeado(Usuario usuarioLogeado) {
        this.usuarioLogeado = usuarioLogeado;
    }
}