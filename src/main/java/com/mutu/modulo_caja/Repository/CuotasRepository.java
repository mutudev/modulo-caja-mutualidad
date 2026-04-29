package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelCuotas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CuotasRepository extends JpaRepository<ModelCuotas, Integer> {

    ModelCuotas
    findFirstByCreditoIdAndFechaPRealizadaIsNotNullOrderByNumCuotaDesc(Integer creditoId);

    ModelCuotas
    findByCreditoIdAndNumCuota(Integer creditoId, Integer numCuota);




}
