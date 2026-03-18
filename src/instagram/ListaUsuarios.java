package instagram;

import java.util.ArrayList;


public class ListaUsuarios {

    private NodoUsuario cabeza;
    private int tamanio;

    public ListaUsuarios() {
        this.cabeza  = null;
        this.tamanio = 0;
    }

    public void agregar(Usuario u) {
        NodoUsuario nuevo = new NodoUsuario(u);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            agregarRecursivo(cabeza, nuevo);   
        }
        tamanio++;
    }

    private void agregarRecursivo(NodoUsuario actual, NodoUsuario nuevo) {
        if (actual.siguiente == null) {
            actual.siguiente = nuevo;          
        } else {
            agregarRecursivo(actual.siguiente, nuevo); 
        }
    }


    public Usuario buscarRecursivo(String username) {
        return buscarRecursivoAux(cabeza, username);
    }

    private Usuario buscarRecursivoAux(NodoUsuario nodo, String username) {
        if (nodo == null)                                       return null; 
        if (nodo.usuario.getUsername().equalsIgnoreCase(username)) return nodo.usuario; 
        return buscarRecursivoAux(nodo.siguiente, username);   
    }

  
    public boolean contiene(String username) {
        return contieneRecursivo(cabeza, username);
    }

    private boolean contieneRecursivo(NodoUsuario nodo, String username) {
        if (nodo == null)                                         return false;
        if (nodo.usuario.getUsername().equals(username))          return true;
        return contieneRecursivo(nodo.siguiente, username);     
    }

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
        buscarPorCriterioRecursivo(nodo.siguiente, criterio, excluir, resultados); 
    }


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


    public ArrayList<Usuario> toArrayList() {
        ArrayList<Usuario> lista = new ArrayList<>();
        toArrayListRecursivo(cabeza, lista);
        return lista;
    }

    private void toArrayListRecursivo(NodoUsuario nodo, ArrayList<Usuario> lista) {
        if (nodo == null) return;               
        lista.add(nodo.usuario);
        toArrayListRecursivo(nodo.siguiente, lista); 
    }


    public void limpiar() {
        cabeza  = null;
        tamanio = 0;
    }

    public int getTamanio()     { return tamanio; }
    public boolean estaVacia()  { return cabeza == null; }
    public NodoUsuario getCabeza() { return cabeza; }
}