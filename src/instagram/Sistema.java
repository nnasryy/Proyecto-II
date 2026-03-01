/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author nasry
 */
public class Sistema {

    private final String RUTA_RAIZ = "INSTA_RAIZ";
    private final String RUTA_USERS = RUTA_RAIZ + "/users.ins";
    private Usuario usuarioActual;

    public Sistema() {
        verificarEstructura();
    }

    //Método para verificar mi ruta
    private void verificarEstructura() {
        File raiz = new File(RUTA_RAIZ);
        if (!raiz.exists()) {
            raiz.mkdir();
        }

        File users = new File(RUTA_USERS);
        try {
            if (!users.exists()) {
                users.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Método para registrar usuario
    public void registrarUsuario(String username, String password, TipoCuenta tipoCuenta) {
        if (existeUsername(username)) {
            System.out.println("El username ya existe");
            return;
        }
        EstadoCuenta estado = EstadoCuenta.ACTIVA;
        Usuario nuevo = new Usuario(username, password, tipoCuenta, estado);
        try (FileWriter writer = new FileWriter(RUTA_USERS, true)) {

            writer.write(
                    nuevo.getUsername() + "|"
                    + nuevo.getPassword() + "|"
                    + nuevo.getEstadoCuenta().name() + "|"
                    + nuevo.getTipoCuenta().name() + "\n"
            );
            crearCarpetaUsuario(username);
            System.out.println("Usuario registrado correctamente");
        } catch (IO Excpetion e
            
                ){
        e.printStackTrace();
            }

        }

    }
