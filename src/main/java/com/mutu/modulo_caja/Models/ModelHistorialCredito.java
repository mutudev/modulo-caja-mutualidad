package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "HISTORIAL_CREDITO_DETALLE")
@Data
public class ModelHistorialCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "FECHA_P", nullable = false)
    private LocalDate fechaP;

    @Column(name = "FECHA_V", nullable = false)
    private LocalDate fechaV;

    @Column(name = "NUM_CUOTA", nullable = false)
    private Integer numCuota;

    @Column(name = "CAPITAL_PAGADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal capitalPagado;

    @Column(name = "INTERES_PAGADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal interesPagado;

    @Column(name = "MORA_PAGADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal moraPagado;

    @Column(name = "IVA_PAGADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal ivaPagado;

    @Column(name = "TOTAL_PAGADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPagado;

    @Column(name = "SALDO_CREDITO", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoCredito;

    @Column(name = "OPERACION_ID", nullable = false)
    private Integer operacionId;

    @Column(name = "ESTADO", nullable = false)
    private Boolean estado;

    @Column(name = "FECHA_P_ANTERIOR")
    private LocalDate fechaPagoAnterior;

    @Column(name = "FECHA_P_FINALIZADO")
    private LocalDate fechaPagoFinalizado;

    @Column(name = "CREDITO_ID", nullable = false)
    private Integer creditoId;

    @Column(name = "BONIF_PAGADO", nullable = false)
    private BigDecimal bonifPagada;

}
