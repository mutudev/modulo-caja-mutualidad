package com.mutu.modulo_caja.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "TIPO_CREDITO")
public class ModelTipoCredito {
    @Id
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "CODIGO_SISTEMA")
    private String Codigo_sistema;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "DESCRIPCION")
    private String Descripcion;

    @Column(name = "FC")
    private LocalDate Fc;

    @Column(name = "BONIF")
    private boolean Bonif;

    @Column(name = "MONTO_MAXIMO")
    private BigDecimal Monto_maximo;

    @Column(name = "IVA")
    private boolean Iva;
}
