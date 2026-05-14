package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelTipoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoCreditoRepository extends JpaRepository <ModelTipoCredito, Integer>{

    ModelTipoCredito findById(int id);
}
