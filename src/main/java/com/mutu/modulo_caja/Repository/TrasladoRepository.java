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

@Repository
public interface TrasladoRepository extends JpaRepository<ModelTraslado, Integer> {

  @Procedure(name = "Traslado.pa_ProcesarTraslado")
  String pa_ProcesarTraslado(
      String nombre_usuario,
      double monto_trasladar,
      int tipo,
      String empresa,
      String turno,
      String Resultado);

  ModelTraslado findByCuentaOrigen(String cuenta);

  @Query(
      value =
          "SELECT * FROM VW_HISTORIAL_TRASLADOS WHERE USUARIO_ID = :usuario AND TIPO_TRASLADO = :tipo AND ESTADO = :estado AND FECHA = :fecha AND TURNO = :turno",
      nativeQuery = true)
  List<Object[]> historialTraslados(
      @Param("usuario") int usuario,
      @Param("tipo") int tipo,
      @Param("estado") int estado,
      @Param("fecha") String fecha,
      @Param("turno") String turno);


  @Procedure(name = "Operacion.pa_CancelarTraslados")
  String pa_CancelarTraslados (int opcion, int id_traslado, String cuenta_origen, String cuenta_destino,
                                double monto, String empresa, String Resultado);
}


