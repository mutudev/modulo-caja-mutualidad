package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransaccionRepository extends JpaRepository<ModelTransaccion, Integer> {

    ModelTransaccion findById(int transaccionId);

    List<ModelTransaccion> findByCreditoAfectadoAndStatus(int creditoAfectado, boolean status);

    List<ModelTransaccion> findByIdGreaterThanAndStatus(int transaccionId, boolean status);

    List<ModelTransaccion> findByIdLessThanAndStatus(int transaccionId, boolean status);


}
