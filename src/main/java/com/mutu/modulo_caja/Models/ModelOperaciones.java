package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "OPERACION")
@NamedStoredProcedureQuery(
        name = "Operacion.pa_CancelarOperacion",
        procedureName = "pa_CancelarOperacion",
        resultClasses = ModelOperaciones.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "opcion", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "nombre_usuario", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "numero_socio", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "id_transaccion", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "monto", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "turno", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
        })
@NamedStoredProcedureQuery(
        name = "Operacion.pa_CancelarAbonoCredito",
        procedureName = "pa_CancelarAbonoCredito",
        resultClasses = ModelOperaciones.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "capital_transaccion", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "interes_transaccion", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "mora_transaccion", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "iva_transaccion", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "bonif_transaccion", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "saldo_credito_transaccion", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "pago_total_transaccion", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "tipo_credito_transaccion", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "transaccion_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "cod_empresa", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "turno", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "fecha_transaccion", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "hora_transaccion", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "cuota_id", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "nom_usuario", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
        })


public class ModelOperaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "OPERACION")
    private String operacion;

    @Column(name = "FC")
    private LocalDate fc;
}
