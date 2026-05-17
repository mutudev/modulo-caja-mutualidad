package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "TRANSACCION")
@Data
public class ModelTransaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "USUARIO_ID", nullable = false)
    private Integer usuarioId;

    @Column(name = "OPERACION_ID", nullable = false)
    private Integer operacionId;

    @Column(name = "SOCIO_ID", nullable = false)
    private Integer socioId;

    @Column(name = "SALDO", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    @Column(name = "STATUS", nullable = false)
    private Boolean status = true;

    @Column(name = "FR", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "HORA")
    private LocalTime hora;

    @Column(name = "EMPRESA", length = 4)
    private String empresa;

    @Column(name = "CAJA_ID")
    private Integer cajaId;

    @Column(name = "CAPITAL_CREDITO_PAGADO", precision = 12, scale = 2, nullable = true)
    private BigDecimal capitalCreditoPagado;

    @Column(name = "INTERESES_CREDITO_PAGADO", precision = 12, scale = 2, nullable = true)
    private BigDecimal interesesCreditoPagado;

    @Column(name = "MORA_CREDITO_PAGADO", precision = 12, scale = 2, nullable = true)
    private BigDecimal moraCreditoPagado;

    @Column(name = "IVA_CREDITO_PAGADO", precision = 12, scale = 2, nullable = true)
    private BigDecimal ivaCreditoPagado;

    @Column(name = "BONIF_CREDITO_PAGADO", precision = 12, scale = 2, nullable = true)
    private BigDecimal bonifCreditoPagado;

    @Column(name = "SALDO_CREDITO", precision = 12, scale = 2, nullable = true)
    private BigDecimal saldoCredito;

    @Column(name = "TIPO_CREDITO", length = 50, nullable = true)
    private String tipoCredito;

    @Column(name = "CUOTA_AFECTADA", nullable = true)
    private Integer cuotaAfectada;

    @Column(name = "CREDITO_AFECTADO", nullable = true)
    private Integer creditoAfectado;

    @Column(name = "AHORRO_ALMOMENTO", precision = 18, scale = 2)
    private BigDecimal ahorroAlMomento;

    @Column(name = "TOTAL_CUOTA_PAGADA", precision = 18, scale = 2, nullable = true)
    private BigDecimal totalCuotaPagada;
}
