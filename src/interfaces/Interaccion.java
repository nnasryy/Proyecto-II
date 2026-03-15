/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import java.util.ArrayList;

/**
 * Interfaz que define el contrato de interacciones sociales del sistema.
 * Implementada por Sistema.
 *
 * Temas: Interfaces
 */
public interface Interaccion {

    // ── Búsqueda ─────────────────────────────────────────────────
    /**
     * Busca usuarios cuyo username contenga el criterio dado.
     * @param criterio texto parcial o completo a buscar
     * @return lista de usuarios que coinciden
     */
    ArrayList buscar(String criterio);

    /**
     * Verifica si existe un usuario con ese username exacto.
     * @param username identificador único
     * @return true si existe
     */
    boolean existe(String username);

    // ── Seguimiento ──────────────────────────────────────────────
    /**
     * Sigue a un usuario (directo si es público, solicitud si es privado).
     * @param usernameObjetivo username a seguir
     * @return true si la operación fue exitosa
     */
    boolean seguirUsuario(String usernameObjetivo);

    /**
     * Deja de seguir a un usuario.
     * @param usernameObjetivo username a dejar de seguir
     * @return true si la operación fue exitosa
     */
    boolean dejarDeSeguir(String usernameObjetivo);
}