package com.mutu.modulo_caja.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "EMPRESA")
public class ModelEmpresa {

    @Id
    @Column(name = "CODIGO")
    private String codigo;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "RAZON_SOCIAL")
    private String razonSocial;

    @Column(name = "CALLE")
    private String calle;

    @Column(name = "CRUZAMIENTO")
    private String cruzamiento;

    @Column(name = "CAT_COLONIA_ID")
    private int catColoniaId;

    @Column(name = "RFC")
    private String rfc;
}
