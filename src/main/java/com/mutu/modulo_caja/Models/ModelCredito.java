package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "CUENTA_CREDITO")
@NamedStoredProcedureQuery(
    name = "Cuenta_Credito.pa_ProcesarDesembolso",
    procedureName = "pa_ProcesarDesembolso",
    resultClasses = ModelCredito.class,
    parameters = {
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "CreditoID", type = Integer.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "nombre_usuario",
          type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "monto_desembolso",
          type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
    })
public class ModelCredito {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "ASESOR")
  private int asesor;

  @Column(name = "SOCIO")
  private int socio;

  @Column(name = "TIPO_CREDITO")
  private int tipo_credito;

  @Column(name = "EMPRESA")
  private String empresa;

  @Column(name = "MONTO")
  private double monto;

  @Column(name = "PLAZO")
  private int plazo;

  @Column(name = "FC")
  private Date fc;

  @Column(name = "FECHA_VENCI")
  private Date fecha_venci;

  @Column(name = "ORDINARIOS")
  private float ordinarios;

  @Column(name = "MORATORIOS")
  private float moratorios;

  @Column(name = "BONIFICACION")
  private float bonificacion;

  @Column(name = "IVA")
  private float iva;

  @Column(name = "STATUS")
  private int status;
}
