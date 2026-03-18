package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;
import java.time.LocalDate;

/**
 *
 * @author nasry
 */
public class Usuario {

    private String username;
    private String password;
    private String nombreCompleto;
    private char genero;
    private int edad;
    private LocalDate fechaRegistro;
    private String fotoPerfil;
    private TipoCuenta tipoCuenta;
    private EstadoCuenta estadoCuenta;

    public Usuario(String username, String password, String nombreCompleto,
            char genero, int edad, String fotoPerfil,
            LocalDate fechaRegistro, TipoCuenta tipoCuenta,
            EstadoCuenta estadoCuenta) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.edad = edad;
        this.fotoPerfil = fotoPerfil;
        this.fechaRegistro = fechaRegistro;
        this.tipoCuenta = tipoCuenta;
        this.estadoCuenta = estadoCuenta;
    }

    // GETTERS 
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public EstadoCuenta getEstadoCuenta() {
        return estadoCuenta;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public char getGenero() {
        return genero;
    }

    public int getEdad() {
        return edad;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }
    public void setEstadoCuenta(EstadoCuenta estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

public void setFotoPerfil(String fotoPerfil) {
    this.fotoPerfil = fotoPerfil;
}

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void setGenero(char genero) {
        this.genero = genero;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }


}
