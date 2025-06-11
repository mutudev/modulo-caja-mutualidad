package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelCapitalSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;
import java.util.Map;

public interface CapitalSocialRepository extends JpaRepository<ModelCapitalSocial, Integer> {

  List<ModelCapitalSocial> findByNumSocio(int numSocio);

  @Procedure(name = "Capital_Social.pa_AbonarCapitalSocial")
  Map<String, Object> pa_AbonarCapitalSocial(
      int socio, String empresa, double monto, String nombre_usuario, String Resultado, double socialNGU,
      double socialMUT, int transaccion_id);
}
