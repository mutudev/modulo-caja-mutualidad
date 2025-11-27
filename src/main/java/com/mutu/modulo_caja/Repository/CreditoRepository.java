package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelCredito;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface CreditoRepository extends JpaRepository<ModelCredito, Integer> {

  @Query(
      value = "SELECT * FROM VW_CREDITOS_SOCIO WHERE NUM_SOCIO = :socio AND STATUS = :status",
      nativeQuery = true)
  List<Object[]> buscarCreditos(@Param("socio") int socio, @Param("status") int status);

  @Query(value = "SELECT * FROM VW_DESEMBOLSOS_PENDIENTES WHERE SOCIO = :socio", nativeQuery = true)
  List<Object[]> buscarDesembolsosPendientes(@Param("socio") int socio);

  @Procedure(name = "Cuenta_Credito.pa_ProcesarDesembolso")
  Map<String, Object> pa_ProcesarDesembolso(
      int CreditoID,
      String nombre_usuario,
      double monto_desembolso,
      String hora,
      String Resultado,
      int transaccion_id);

  @Procedure(name = "Cuenta_Credito.pa_PagarCredito")
  Map<String, Object>  pa_PagarCredito(
          int opcion,
          double monto_capital,
          double intereses,
          double mora,
          double iva,
          int cuota_id,
          int credito_id,
          double total_cuota_pendiente,
          int numero_cuota,
          double bonificacion,
          double total_pago,
          int numero_cuotas,
          String nombre_usuario,
          String empresa,
          String hora,
          int num_socio,
          String turno,
          double total_credito,
          String datos_cuotas,
          int transaccion_id,
          String Resultado,
          String saldo_ticket, String intereses_devueltos, String mora_devueltos, String iva_devueltos, String capital_devueltos,
          String bonif_devuelto
          );

  ModelCredito findById(int id);

  ModelCredito findBySocioAndStatusAndMontoAndEmpresa(
      int socio, int status, double monto, String empresa);

//  @Query(
//      value = "SELECT TOP 18 * FROM VW_CUOTAS_CREDITO WHERE CREDITO_ID = :credito_id AND STATUS = :status ORDER BY ID ASC",
//      nativeQuery = true)
//  List<Object[]> cuotasInmediatasXCredito(@Param("credito_id") int credito_id, @Param("status") int status);

  @Query(
          value = "SELECT * FROM VW_CUOTAS_CREDITO WHERE CREDITO_ID = :credito_id AND STATUS = :status ORDER BY ID ASC",
          nativeQuery = true)
  List<Object[]> cuotasInmediatasXCredito(
          @Param("credito_id") int credito_id,
          @Param("status") int status,
          Pageable pageable);

  @Query(value = "SELECT PLAZO FROM CAT_CREDITOS WHERE ID = :id", nativeQuery = true)
  int obtenerPlazo(@Param("id") int id);


  @Query(
          value = "SELECT TOP 1 * FROM VW_CUOTAS_CREDITO WHERE CREDITO_ID = :credito_id AND STATUS = :status ORDER BY ID DESC",
          nativeQuery = true)
  Object[] cuotaAnteriorXCredito(@Param("credito_id") int credito_id, @Param("status") int status);

  @Query(
          value = "SELECT TOP 1 * FROM VW_CUOTAS_CREDITO WHERE CREDITO_ID = :credito_id AND STATUS = :status ORDER BY ID ASC",
          nativeQuery = true)
  Object[] cuotaAnteriorNoPagada(@Param("credito_id") int credito_id, @Param("status") int status);

  @Query(
          value = "SELECT TOP 1 * FROM VW_CUOTAS_CREDITO WHERE CREDITO_ID = :credito_id AND STATUS = :status AND NUM_CUOTA = :num_cuota",
          nativeQuery = true)
  Object[] cuotaAnteriorConNum(@Param("credito_id") int credito_id, @Param("status") int status, @Param("num_cuota") int num_cuota);
}
