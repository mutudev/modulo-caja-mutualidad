package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelCierre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface CierreRepository extends JpaRepository<ModelCierre, Integer> {

  @Procedure(name = "Cierre_Cajero.pa_ProcesarAjuste")
  String procesarAjuste(
      String nombre_usuario,
      double faltante,
      double sobrante,
      String turno,
      String empresa,
      double monto_fis,
      String Resultado);

  @Procedure(name = "Cierre_Cajero.pa_CierreDeCajero")
  Map<String, Object> procesarCierre(int cuenta_cajeroMut, int cuenta_cajeroNgu, String fecha, double saldo_fisico, String usuario, String Resultado,
                                     int Cierre_id_mut, int Cierre_id_ngu);

  @Query (value = "SELECT * FROM CIERRE_CAJERO WHERE ID = :id", nativeQuery = true)
  Object[] traerCierreCajero(@Param("id") int id);

}
