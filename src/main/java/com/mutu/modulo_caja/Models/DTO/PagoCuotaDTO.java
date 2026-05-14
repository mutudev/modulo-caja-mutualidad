package com.mutu.modulo_caja.Models.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoCuotaDTO {

    private Integer creditoId;
    private Integer numCuota;
    private LocalDate fechaP;
    private BigDecimal capital;
    private BigDecimal intereses;
    private BigDecimal iva;
    private BigDecimal mora;
    private BigDecimal bonif;
    private BigDecimal total;
    private Integer status;
    private LocalDate fechaAnterior;
    private LocalDate fechaRealizada;
    private LocalDate fechaTerminoPago;
    private BigDecimal interesesAcumulados;
    private BigDecimal moraAcumulados;
}