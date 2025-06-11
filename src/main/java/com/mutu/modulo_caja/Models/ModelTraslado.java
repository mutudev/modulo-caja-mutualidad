package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "TRASLADO")
@NamedStoredProcedureQuery(
    name = "Traslado.pa_ProcesarTraslado",
    procedureName = "pa_ProcesarTraslado",
    resultClasses = ModelTraslado.class,
    parameters = {
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "nombre_usuario",
          type = String.class),
      @StoredProcedureParameter(
          mode = ParameterMode.IN,
          name = "monto_trasladar",
          type = Double.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "tipo", type = Integer.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.IN, name = "turno", type = String.class),
      @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
    })

@NamedStoredProcedureQuery(
        name = "Operacion.pa_CancelarTraslados",
        procedureName = "pa_CancelarTraslados",
        resultClasses = ModelTraslado.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "opcion", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "id_traslado", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "cuenta_origen", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "cuenta_destino", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "monto", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)

        })

public class ModelTraslado {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "USUARIO_ID")
  private int usuarioId;

  @Column(name = "CUENTA_ORIGEN")
  private String cuentaOrigen;

  @Column(name = "CUENTA_DESTINO")
  private String cuentaDestino;

  @Column(name = "MONTO")
  private double monto;

  @Column(name = "TIPO_TRASLADO")
  private int tipoTraslado;

  @Column(name = "FR")
  private LocalDateTime fr;

  @Column(name = "HORA_REGISTRO")
  private LocalDateTime horaRegistro;

  @Column(name = "ESTADO")
  private int estado;
}
