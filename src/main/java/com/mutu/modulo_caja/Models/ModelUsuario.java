package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "USUARIO")
@NamedStoredProcedureQuery(
    name = "Usuario.pa_ValidarLogin",
    procedureName = "pa_ValidarLogin",
    resultClasses = ModelUsuario.class,
    parameters = {
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "Usuario", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "Pass", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Rol", type = Integer.class)
    })
@NamedStoredProcedureQuery(
    name = "Usuario.pa_ValidarCierre",
    procedureName = "pa_ValidarCierre",
    resultClasses = ModelUsuario.class,
    parameters = {
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "nombre_usuario",
          type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "turno", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "opcion", type = Integer.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
    })
public class ModelUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "USUARIO")
    private String usuario;

    @Column(name = "PASS")
    private String pass;

    @Column(name = "ROL_ID")
    private int rol;

}
