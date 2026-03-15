package instagram;

import java.util.ArrayList;

/**
 * Lista enlazada de usuarios con métodos recursivos.
 *
 * Temas demostrados:
 *   - Nodos (NodoUsuario)
 *   - Lista enlazada (estructura cabeza → siguiente)
 *   - Recursividad (todos los métodos de búsqueda son recursivos)
 */
public class ListaUsuarios {

    private NodoUsuario cabeza;
    private int tamanio;

    public ListaUsuarios() {
        this.cabeza  = null;
        this.tamanio = 0;
    }

    // ── INSERTAR ────────────────────────────────────────────────

    /** Agrega un usuario al final de la lista */
    public void agregar(Usuario u) {
        NodoUsuario nuevo = new NodoUsuario(u);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            agregarRecursivo(cabeza, nuevo);    // RECURSIVIDAD
        }
        tamanio++;
    }

    private void agregarRecursivo(NodoUsuario actual, NodoUsuario nuevo) {
        if (actual.siguiente == null) {
            actual.siguiente = nuevo;           // caso base: llegamos al final
        } else {
            agregarRecursivo(actual.siguiente, nuevo); // llamada recursiva
        }
    }

    // ── BUSCAR (RECURSIVO) ───────────────────────────────────────

    /**
     * Búsqueda recursiva por username exacto.
     * Ya existía en la clase original — se mantiene intacto.
     */
    public Usuario buscarRecursivo(String username) {
        return buscarRecursivoAux(cabeza, username);
    }

    private Usuario buscarRecursivoAux(NodoUsuario nodo, String username) {
        if (nodo == null)                                       return null; // caso base: no encontrado
        if (nodo.usuario.getUsername().equalsIgnoreCase(username)) return nodo.usuario; // caso base: encontrado
        return buscarRecursivoAux(nodo.siguiente, username);   // llamada recursiva
    }

    /**
     * Verifica si existe un usuario con ese username (recursivo).
     */
    public boolean contiene(String username) {
        return contieneRecursivo(cabeza, username);
    }

    private boolean contieneRecursivo(NodoUsuario nodo, String username) {
        if (nodo == null)                                         return false;
        if (nodo.usuario.getUsername().equals(username))          return true;
        return contieneRecursivo(nodo.siguiente, username);       // RECURSIVIDAD
    }

    /**
     * Busca todos los usuarios cuyo username contenga el criterio (parcial).
     * No incluye desactivados ni al usuario actual.
     */
    public ArrayList<Usuario> buscarPorCriterio(String criterio, String excluirUsername) {
        ArrayList<Usuario> resultados = new ArrayList<>();
        buscarPorCriterioRecursivo(cabeza, criterio.toLowerCase(), excluirUsername, resultados);
        return resultados;
    }

    private void buscarPorCriterioRecursivo(NodoUsuario nodo, String criterio,
                                             String excluir, ArrayList<Usuario> resultados) {
        if (nodo == null) return; // caso base
        Usuario u = nodo.usuario;
        boolean coincide  = u.getUsername().toLowerCase().contains(criterio);
        boolean noEsYo    = excluir == null || !u.getUsername().equals(excluir);
        boolean activo    = u.getEstadoCuenta() == enums.EstadoCuenta.ACTIVO;
        if (coincide && noEsYo && activo) resultados.add(u);
        buscarPorCriterioRecursivo(nodo.siguiente, criterio, excluir, resultados); // RECURSIVIDAD
    }

    // ── ELIMINAR ────────────────────────────────────────────────

    /** Elimina el usuario con ese username de la lista */
    public boolean eliminar(String username) {
        if (cabeza == null) return false;
        if (cabeza.usuario.getUsername().equals(username)) {
            cabeza = cabeza.siguiente;
            tamanio--;
            return true;
        }
        return eliminarRecursivo(cabeza, username);
    }

    private boolean eliminarRecursivo(NodoUsuario anterior, String username) {
        if (anterior.siguiente == null) return false; // caso base: no encontrado
        if (anterior.siguiente.usuario.getUsername().equals(username)) {
            anterior.siguiente = anterior.siguiente.siguiente;
            tamanio--;
            return true;
        }
        return eliminarRecursivo(anterior.siguiente, username); // RECURSIVIDAD
    }

    // ── UTILIDADES ──────────────────────────────────────────────

    /**
     * Convierte la lista enlazada a ArrayList (para compatibilidad con el resto del código).
     * Internamente usa recursividad.
     */
    public ArrayList<Usuario> toArrayList() {
        ArrayList<Usuario> lista = new ArrayList<>();
        toArrayListRecursivo(cabeza, lista);
        return lista;
    }

    private void toArrayListRecursivo(NodoUsuario nodo, ArrayList<Usuario> lista) {
        if (nodo == null) return;                   // caso base
        lista.add(nodo.usuario);
        toArrayListRecursivo(nodo.siguiente, lista); // RECURSIVIDAD
    }

    /** Vacía la lista */
    public void limpiar() {
        cabeza  = null;
        tamanio = 0;
    }

    public int getTamanio()     { return tamanio; }
    public boolean estaVacia()  { return cabeza == null; }
    public NodoUsuario getCabeza() { return cabeza; }
}