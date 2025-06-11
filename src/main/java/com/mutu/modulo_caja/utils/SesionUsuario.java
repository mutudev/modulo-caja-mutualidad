package com.mutu.modulo_caja.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Clase de sesión compartida para almacenar temporalmente
 * el usuario y empresa logueados, accesible en todos los controladores
 * mediante inyección de dependencias.
 */
@Getter
@Setter
@Component

public class SesionUsuario {
    private static SesionUsuario instancia;
    private String usuario;
    private String empresa;

    private SesionUsuario() {}

    public static SesionUsuario getInstance() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getUsuario() { return usuario; }
    public String getEmpresa() { return empresa; }
}


