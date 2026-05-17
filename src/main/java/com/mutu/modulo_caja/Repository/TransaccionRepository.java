package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<ModelTransaccion, Integer> {
}
