/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;

/**
 *
 * @author nasry
 */
public class Usuario {
    private String username;
    private String password;
    private TipoCuenta tipoCuenta;
    private EstadoCuenta estadoCuenta;
    
    public Usuario(String username, String password, TipoCuenta tipoCuenta, EstadoCuenta estadoCuenta){
    this.username = username;
    this.password = password;
    this.tipoCuenta = tipoCuenta;
    this.estadoCuenta = estadoCuenta;
    }
    
}
