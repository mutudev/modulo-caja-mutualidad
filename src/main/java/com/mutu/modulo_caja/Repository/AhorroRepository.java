package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelAhorro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

public interface AhorroRepository extends JpaRepository<ModelAhorro, Integer> {

  ModelAhorro findBySocio(int socio);

  @Procedure(name = "Cuenta_Ahorro.pa_AbonarAhorro")
  Map<String, Object> pa_AbonarAhorro(
      double cant_ahorro, String cuenta, String nombre_usuario, String empresa, String resultado, double ahorro_total,
      int transaccion_id);

  @Query(value = "SELECT * FROM VW_RETIROS_PENDIENTES WHERE SOCIO = :socio", nativeQuery = true)
  List<Object[]> buscarRetirosPendientes(@Param("socio") int socio);

  @Procedure(name = "Cuenta_Ahorro.pa_ProcesarRetiro")
  String pa_ProcesarRetiro(
      int ID, int num_socio, String nombre_usuario, double monto_retiro, String empresa, String turno, String Resultado);
}
