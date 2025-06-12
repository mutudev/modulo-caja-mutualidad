package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "CAPITAL_SOCIAL")
@NamedStoredProcedureQuery(
    name = "Capital_Social.pa_AbonarCapitalSocial",
    procedureName = "pa_AbonarCapitalSocial",
    resultClasses = ModelCapitalSocial.class,
    parameters = {
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "socio", type = Integer.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "hora", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "monto", type = Double.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "nombre_usuario",
          type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "socialNGU", type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "socialMUT", type = Double.class),
      @StoredProcedureParameter(
          mode = ParameterMode.OUT,
          name = "transaccion_id",
          type = Integer.class),
    })
public class ModelCapitalSocial {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "NUM_SOCIO")
  private int numSocio;

  @Column(name = "EMPRESA_COD")
  private String empresa_cod;

  @Column(name = "MONTO_CUBIERTO")
  private double monto_cubierto;

  @Column(name = "FC")
  private Date fc;

  @Column(name = "FP")
  private Date fp;
}
