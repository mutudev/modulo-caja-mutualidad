package com.mutu.modulo_caja.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "CAJA")
@Data
public class ModelCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "USUARIO_ID")
    private int usuarioId;

    @Column(name = "SAL_INICIAL")
    private double sal_inicial;

    @Column(name = "SAL_FINAL")
    private double sal_final;

    @Column(name = "FR")
    private LocalDate fr;

    @Column(name = "ESTADO")
    private int estado;

    @Column(name = "TURNO")
    private String turno;

    @Column(name = "EMPRESA")
    private String empresa;


    @Column(name = "AJUSTE")
    private int ajuste;


    @Column(name = "CIERRE")
    private int cierre;
}
