package com.mutu.modulo_caja.Models;


public class CreditoVisual {
    private int idCredito;
    private String tipoCredito;
    private String fechaDesembolso;
    private double monto;
    private String empresa;

    public CreditoVisual(int idCredito, String tipoCredito, String fechaDesembolso, double monto, String empresa) {
        this.idCredito = idCredito;
        this.tipoCredito = tipoCredito;
        this.fechaDesembolso = fechaDesembolso;
        this.monto = monto;
        this.empresa = empresa;
    }

    public int getIdCredito() {
        return idCredito;
    }

    public String getTipoCredito() {
        return tipoCredito;
    }

    public String getFechaDesembolso() {
        return fechaDesembolso;
    }

    public double getMonto() {
        return monto;
    }

    public String getEmpresa() {
        return empresa;
    }
}

