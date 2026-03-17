/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package red;

import java.io.Serializable;

public class EventoSocket implements Serializable {
    public enum Tipo {
        NUEVO_POST, NUEVO_MENSAJE, NUEVA_NOTIFICACION,
        CAMBIO_SEGUIDOR, ACTUALIZACION_PERFIL, PING
    }

    private Tipo tipo;
    private String usuarioOrigen;
    private String usuarioDestino; // null = broadcast a todos

    public EventoSocket(Tipo tipo, String usuarioOrigen, String usuarioDestino) {
        this.tipo = tipo;
        this.usuarioOrigen = usuarioOrigen;
        this.usuarioDestino = usuarioDestino;
    }

    public Tipo getTipo() { return tipo; }
    public String getUsuarioOrigen() { return usuarioOrigen; }
    public String getUsuarioDestino() { return usuarioDestino; }
}
