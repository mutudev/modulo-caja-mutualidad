package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelPrevisionSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.Map;

public interface PrevisionSocialRepository extends JpaRepository<ModelPrevisionSocial, Integer> {

  ModelPrevisionSocial findByNumSocioAndEmpresaCod(int numSocio, String empresa_cod);

  @Procedure(name = "Prevision_Social.pa_AbonarPrevisionSocial")
  Map<String, Object> AbonarPrevisionSocial(
      int num_socio, String empresa, String hora, double monto_pagado, String usuario, String Resultado, double monto_asignado,
      double monto_ticket, int transaccion_id);
}
