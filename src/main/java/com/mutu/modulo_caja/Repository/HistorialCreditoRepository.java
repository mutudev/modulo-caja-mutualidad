package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelHistorialCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCreditoRepository extends JpaRepository<ModelHistorialCredito, Integer> {

    List<ModelHistorialCredito> findByOperacionId(int operacionId);

    List<ModelHistorialCredito> findByCreditoIdAndNumCuotaAndEstado(int creditoId, int numCuota, boolean estado);
}
