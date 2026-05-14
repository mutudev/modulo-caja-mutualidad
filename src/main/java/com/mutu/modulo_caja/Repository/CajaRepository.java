package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelCaja;
import com.mutu.modulo_caja.Models.ModelCapitalSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CajaRepository extends JpaRepository<ModelCaja,Integer> {

    ModelCaja findByUsuarioIdAndTurnoAndEmpresaAndEstado(int usuarioId, String turno, String empresa, int estado);

    ModelCaja findById(int id);

    List<ModelCaja> findByUsuarioIdAndFrAndEstadoAndTurno(int usuarioId, LocalDate fr, int estado, String turno);

    ModelCaja findByUsuarioIdAndFrAndEstadoAndTurnoAndEmpresa(int usuarioId, LocalDate fr, int estado, String turno, String empresa);

    ModelCaja findByUsuarioIdAndFrAndEstadoAndTurnoAndEmpresaAndAjuste(int usuarioId, LocalDate fr, int estado, String turno, String empresa, int ajuste);

    List<ModelCaja> findByUsuarioIdAndEstadoAndFrNot(int usuarioId, int estado, LocalDate fr);
}
