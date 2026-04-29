package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "VW_CUOTAS_CREDITO")
public class ModelCuotas {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CREDITO_ID")
    private Integer creditoId;

    @Column(name = "NUM_CUOTA")
    private Integer numCuota;

    @Column(name = "FECHA_P")
    private String fechaP;  // CAST a VARCHAR en la vista

    @Column(name = "CAPITAL")
    private String capital; // FORMAT -> moneda

    @Column(name = "INTERESES")
    private String intereses;

    @Column(name = "IVA")
    private String iva;

    @Column(name = "MORA")
    private String mora;

    @Column(name = "BONIF")
    private String bonif;

    @Column(name = "TOTAL")
    private String total;

    @Column(name = "SALDO_CREDITO")
    private String saldoCredito;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "FECHA_P_REALIZADA")
    private String fechaPRealizada;

    @Column(name = "FECHA_TERMINO_PAGO")
    private String fechaTerminoPago;

    @Column(name = "VIGENCIA")
    private Integer vigencia;

    @Column(name = "INTERES_ACUMULADO")
    private String interesAcumulado;

    @Column(name = "MORA_ACUMULADO")
    private String moraAcumulado;

    @Column(name = "IVA_ACUMULADO")
    private String ivaAcumulado;

    @Column(name = "IS_CONDONADO")
    private Boolean isCondonado;

    @Column(name = "ESTADO_CUOTA")
    private String estadoCuota;
}
