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
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "hora", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "transaccion_id", type = Integer.class)
    })

@NamedStoredProcedureQuery(
        name = "Cuenta_Credito.pa_PagarCredito",
        procedureName = "pa_PagarCredito",
        resultClasses = ModelCredito.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "opcion", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "monto_capital", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "intereses", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "mora", type = Double.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "iva", type = Double.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "cuota_id", type = Integer.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "credito_id", type = Integer.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "total_cuota_pendiente", type = Double.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "numero_cuota", type = Integer.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "bonificacion", type = Double.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "total_pago", type = Double.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "numero_cuotas", type = Integer.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "nombre_usuario", type = String.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "hora", type = String.class ),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "num_socio", type = Integer.class ),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "transaccion_id", type = Integer.class ),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class ),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "saldo_ticket", type = String.class )
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
