package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "CUENTA_AHORRO")
@NamedStoredProcedureQuery(
    name = "Cuenta_Ahorro.pa_AbonarAhorro",
    procedureName = "pa_AbonarAhorro",
    resultClasses = ModelAhorro.class,
    parameters = {
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "cant_ahorro", type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "cuenta", type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "nombre_usuario",
          type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "hora", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "resultado", type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.OUT,
          name = "ahorro_total",
          type = Double.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "transaccion_id", type = Integer.class)
    })
@NamedStoredProcedureQuery(
    name = "Cuenta_Ahorro.pa_ProcesarRetiro",
    procedureName = "pa_ProcesarRetiro",
    resultClasses = ModelAhorro.class,
    parameters = {
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "ID", type = Integer.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "num_socio", type = Integer.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "nombre_usuario",
          type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "monto_retiro",
          type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "turno", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
    })
public class ModelAhorro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "SOCIO")
  private int socio;

  @Column(name = "NUM_CUENTA")
  private String num_cuenta;

  @Column(name = "SALDO")
  private double saldo;

  @Column(name = "STATUS")
  private int status;

  @Column(name = "FC")
  private Date fc;
}
