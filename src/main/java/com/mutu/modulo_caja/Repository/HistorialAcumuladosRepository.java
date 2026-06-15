package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelHistorialAcumulados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialAcumuladosRepository extends JpaRepository<ModelHistorialAcumulados, Long> {

    List<ModelHistorialAcumulados> findByOperacionIdAndEstado(int operacionId, boolean estado);


}
