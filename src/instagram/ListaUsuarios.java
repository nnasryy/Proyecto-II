/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

/**
 *
 * @author nasry
 */
public class ListaUsuarios {
    private NodoUsuario cabeza;

    public ListaUsuarios() {
        this.cabeza = null;
    }

    public void agregar(Usuario u) {
        NodoUsuario nuevo = new NodoUsuario(u);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            NodoUsuario actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
        }
    }

    // MÉTODO RECURSIVO: Buscar usuario
    public Usuario buscarRecursivo(String username) {
        return buscarRecursivoAux(cabeza, username);
    }

    private Usuario buscarRecursivoAux(NodoUsuario nodo, String username) {
        if (nodo == null) return null; // Caso base: no encontrado
        if (nodo.usuario.getUsername().equalsIgnoreCase(username)) {
            return nodo.usuario; // Caso base: encontrado
        }
        return buscarRecursivoAux(nodo.siguiente, username); // Llamada recursiva
    }
    
    public NodoUsuario getCabeza() {
        return cabeza;
    }
}