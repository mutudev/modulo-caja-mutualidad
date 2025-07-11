package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelTraslado;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TrasladoRepository extends JpaRepository<ModelTraslado, Integer> {

  @Procedure(name = "Traslado.pa_ProcesarTraslado")
  Map<String, Object> pa_ProcesarTraslado(
      String nombre_usuario,
      double monto_trasladar,
      int tipo,
      String empresa,
      String hora,
      String turno,
      String Resultado,
      int transaccion_id);

  ModelTraslado findByCuentaOrigen(String cuenta);

  @Query(
      value =
          "SELECT * FROM VW_HISTORIAL_TRASLADOS WHERE USUARIO_ID = :usuario AND TIPO_TRASLADO = :tipo AND ESTADO = :estado AND FECHA = :fecha AND TURNO = :turno AND EMPRESA = :empresa",
      nativeQuery = true)
  List<Object[]> historialTraslados(
      @Param("usuario") int usuario,
      @Param("tipo") int tipo,
      @Param("estado") int estado,
      @Param("fecha") String fecha,
      @Param("turno") String turno,
      @Param("empresa") String empresa);

  @Procedure(name = "Operacion.pa_CancelarTraslados")
  String pa_CancelarTraslados(
      int opcion,
      int id_traslado,
      String cuenta_origen,
      String cuenta_destino,
      double monto,
      String empresa,
      String Resultado);
}
