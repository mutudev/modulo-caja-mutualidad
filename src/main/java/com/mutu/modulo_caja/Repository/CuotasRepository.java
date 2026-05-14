package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.DTO.PagoCuotaDTO;
import com.mutu.modulo_caja.Models.ModelCuotas;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface CuotasRepository extends JpaRepository<ModelCuotas, Integer> {

    ModelCuotas
    findFirstByCreditoIdAndFechaPRealizadaIsNotNullOrderByNumCuotaDesc(Integer creditoId);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    ModelCuotas findByCreditoIdAndNumCuota(Integer creditoId, Integer numCuota);

    @Query(value = "EXEC pa_CalcularPagoDeCuotas ?1, ?2, ?3, ?4, ?5", nativeQuery = true)
    List<Object[]> pa_CalcularPagoDeCuotas(
            Integer creditoId,
            Double tasaInteres,
            Double tasaMora,
            Double tasaIva,
            LocalDate fechaDesembolso
    );




}
