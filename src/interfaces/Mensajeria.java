/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import instagram.Mensaje;

/**
 * Interfaz que define el contrato de mensajería y notificaciones.
 * Implementada por Sistema.
 *
 * Temas: Interfaces
 */
public interface Mensajeria {

    // ── Mensajes ─────────────────────────────────────────────────
    /**
     * Guarda un mensaje en el inbox del receptor.
     * @param m mensaje a guardar
     * @return true si se guardó correctamente
     */
    boolean enviarMensaje(Mensaje m);

    // ── Notificaciones ───────────────────────────────────────────
    /**
     * Envía una notificación al usuario destino.
     * @param usernameDestino usuario que recibirá la notificación
     * @param mensaje         contenido de la notificación
     */
    void notificar(String usernameDestino, String mensaje);

    /**
     * Retorna cuántas notificaciones pendientes tiene el usuario actual.
     * @return número de notificaciones sin ver
     */
    int getTotalNotificacionesPendientes();
}