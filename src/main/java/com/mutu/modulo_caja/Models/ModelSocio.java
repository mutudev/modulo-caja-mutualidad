package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "SOCIO")
@NamedStoredProcedureQuery(
    name = "Socio.pa_BuscarSocioXNumero",
    procedureName = "pa_BuscarSocioXNumero",
    resultClasses = ModelUsuario.class,
    parameters = {
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "NumSocio", type = Integer.class),
      @StoredProcedureParameter(
          mode = ParameterMode.OUT,
          name = "NombreFormateado",
          type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.OUT,
          name = "NumSocioEncontrado",
          type = Integer.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "TipoDeSocio", type = String.class)
    })
public class ModelSocio {

  @Id
  @Column(name = "NUM_SOCIO")
  private Integer NumSocio;

  @Column(name = "EMPRESA_COD")
  private String Empresa;

  @Column(name = "PRIMER_NOM")
  private String PrimerNom;

  @Column(name = "SEGUNDO_NOM")
  private String SegundoNom;

  @Column(name = "APELLIDO_P")
  private String ApellidoP;

  @Column(name = "APELLIDO_M")
  private String ApellidoM;
}
