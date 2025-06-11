package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "PREVISION_SOCIAL")
@NamedStoredProcedureQuery(
    name = "Prevision_Social.pa_AbonarPrevisionSocial",
    procedureName = "pa_AbonarPrevisionSocial",
    resultClasses = ModelPrevisionSocial.class,
    parameters = {
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "num_socio", type = Integer.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "monto_pagado",
          type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "usuario", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.OUT,
          name = "monto_asignado",
          type = Double.class),
      @StoredProcedureParameter(
          mode = ParameterMode.OUT,
          name = "monto_ticket",
          type = Double.class),
      @StoredProcedureParameter(
          mode = ParameterMode.OUT,
          name = "transaccion_id",
          type = Integer.class)
    })
public class ModelPrevisionSocial {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "NUM_SOCIO")
  private int numSocio;

  @Column(name = "EMPRESA_COD")
  private String empresaCod;

  @Column(name = "PREVISION")
  private double prevision;

  @Column(name = "MONTO_ASIGNADO")
  private double montoAsignado;

  @Column(name = "FECHA_PAGO")
  private LocalDate fc;

  @Column(name = "UR")
  private int ur;
}
