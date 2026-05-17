package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelCaja;
import com.mutu.modulo_caja.Models.ModelCapitalSocial;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface CajaRepository extends JpaRepository<ModelCaja,Integer> {

    ModelCaja findByUsuarioIdAndTurnoAndEmpresaAndEstado(int usuarioId, String turno, String empresa, int estado);

    ModelCaja findById(int id);

    List<ModelCaja> findByUsuarioIdAndFrAndEstadoAndTurno(int usuarioId, LocalDate fr, int estado, String turno);

    ModelCaja findByUsuarioIdAndFrAndEstadoAndTurnoAndEmpresa(int usuarioId, LocalDate fr, int estado, String turno, String empresa);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    ModelCaja findByUsuarioIdAndFrAndEstadoAndEmpresa(int usuarioId, LocalDate fr, int estado, String empresa);

    ModelCaja findByUsuarioIdAndFrAndEstadoAndTurnoAndEmpresaAndAjuste(int usuarioId, LocalDate fr, int estado, String turno, String empresa, int ajuste);

    List<ModelCaja> findByUsuarioIdAndEstadoAndFrNot(int usuarioId, int estado, LocalDate fr);
}
