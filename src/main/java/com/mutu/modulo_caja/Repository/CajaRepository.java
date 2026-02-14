package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelCaja;
import com.mutu.modulo_caja.Models.ModelCapitalSocial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CajaRepository extends JpaRepository<ModelCaja,Integer> {

    ModelCaja findByUsuarioIdAndTurnoAndEmpresaAndEstado(int usuarioId, String turno, String empresa, int estado);

    ModelCaja findById(int id);

}
