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
