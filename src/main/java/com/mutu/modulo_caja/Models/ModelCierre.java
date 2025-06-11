package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "CIERRE_CAJERO")
@NamedStoredProcedureQuery(
    name = "Cierre_Cajero.pa_ProcesarAjuste",
    procedureName = "pa_ProcesarAjuste",
    resultClasses = ModelCierre.class,
    parameters = {
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "nombre_usuario",
          type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "faltante", type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "sobrante", type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "turno", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
    })
@NamedStoredProcedureQuery(
    name = "Cierre_Cajero.pa_CierreDeCajero",
    procedureName = "pa_CierreDeCajero",
    resultClasses = ModelCierre.class,
    parameters = {
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "cuenta_cajeroMut",
          type = Integer.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "cuenta_cajeroNgu",
          type = Integer.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "fecha", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
    })
public class ModelCierre {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "USUARIO_ID")
  private int usuarioId;

  @Column(name = "ABONO_AHORRO")
  private double abonoAhorro;

  @Column(name = "ABONO_CREDITO")
  private double abonoCredito;

  @Column(name = "RETIROS")
  private double retiros;

  @Column(name = "DESEMBOLSOS")
  private double desembolsos;

  @Column(name = "CAPITAL_SOCIAL")
  private double capitalSocial;

  @Column(name = "TRAS_APERTURA")
  private double trasApertura;

  @Column(name = "TRAS_CIERRE")
  private double trasCierre;

  @Column(name = "SOBRANTE")
  private double sobrante;

  @Column(name = "FALTANTE")
  private double faltante;

  @Column(name = "FC")
  private LocalDate fc;
}
