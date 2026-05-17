package com.mutu.modulo_caja.Models;

import com.mutu.modulo_caja.Models.DTO.PagoCuotaDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "CUOTAS_EJEMPLO")
@NamedStoredProcedureQuery(
        name = "Cuotas_Credito.pa_CalcularPagoDeCuotas",
        procedureName = "pa_CalcularPagoDeCuotas",
        resultClasses = PagoCuotaDTO.class,
        parameters = {

                @StoredProcedureParameter(
                        mode = ParameterMode.IN,
                        name = "credito_id",
                        type = Integer.class
                ),

                @StoredProcedureParameter(
                        mode = ParameterMode.IN,
                        name = "tasa_interes",
                        type = Double.class
                ),

                @StoredProcedureParameter(
                        mode = ParameterMode.IN,
                        name = "tasa_mora",
                        type = Double.class
                ),

                @StoredProcedureParameter(
                        mode = ParameterMode.IN,
                        name = "tasa_iva",
                        type = Double.class
                ),

                @StoredProcedureParameter(
                        mode = ParameterMode.IN,
                        name = "fecha_desembolso",
                        type = LocalDate.class
                )
        }
)
public class ModelCuotas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CREDITO_ID", nullable = false)
    private Integer creditoId;

    @Column(name = "NUM_CUOTA", nullable = false)
    private Integer numCuota;

    @Column(name = "FECHA_P", nullable = false)
    private LocalDate fechaP;

    @Column(name = "CAPITAL", nullable = false, precision = 18, scale = 2)
    private BigDecimal capital;

    @Column(name = "INTERESES_ORD", nullable = false, precision = 18, scale = 2)
    private BigDecimal intereses;

    @Column(name = "IVA", nullable = false, precision = 18, scale = 2)
    private BigDecimal iva;

    @Column(name = "MORA", nullable = false, precision = 18, scale = 2)
    private BigDecimal mora;

    @Column(name = "BONIF", nullable = false, precision = 18, scale = 2)
    private BigDecimal bonif;

    @Column(name = "TOTAL", nullable = false, precision = 18, scale = 2)
    private BigDecimal total;

    @Column(name = "SALDO_CREDITO", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoCredito;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "FECHA_ANTERIOR")
    private LocalDate fechaAnterior;

    @Column(name = "FECHA_P_REALIZADA")
    private LocalDate fechaPRealizada;

    @Column(name = "FECHA_TERMINO_PAGO")
    private LocalDate fechaTerminoPago;

    @Column(name = "VIGENCIA")
    private Boolean vigencia;

    @Column(name = "INTERES_ACUMULADO", precision = 18, scale = 2)
    private BigDecimal interesAcumulado;

    @Column(name = "MORA_ACUMULADO", precision = 18, scale = 2)
    private BigDecimal moraAcumulado;

    @Column(name = "IVA_ACUMULADO", precision = 18, scale = 2)
    private BigDecimal ivaAcumulado;

    @Column(name = "BONIF_PAGO", precision = 18, scale = 2)
    private BigDecimal bonifPago;

    @Column(name = "IS_CONDONADO")
    private Boolean isCondonado;
}
