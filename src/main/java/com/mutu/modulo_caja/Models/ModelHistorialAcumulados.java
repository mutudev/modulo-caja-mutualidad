package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HISTORIAL_ACUMULADOS")
public class ModelHistorialAcumulados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CREDITO_ID", nullable = false)
    private Long creditoId;

    @Column(name = "NUM_CUOTA", nullable = false)
    private Integer numCuota;

    @Column(name = "OPERACION_ID", nullable = false)
    private Long operacionId;

    @Column(name = "INTERES_ACUMULADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal interesAcumulado;

    @Column(name = "MORA_ACUMULADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal moraAcumulado;

    @Column(name = "ESTADO", nullable = false)
    private Boolean estado;

    @Column(name = "FR", nullable = false)
    private LocalDate fr;
}
