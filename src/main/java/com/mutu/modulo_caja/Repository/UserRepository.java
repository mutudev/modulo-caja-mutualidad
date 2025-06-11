package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<ModelUsuario, Integer> {

  @Procedure(name = "Usuario.pa_ValidarLogin")
  HashMap pa_validarLogin(String Usuario, String Pass, String Resultado, int Rol);

  @Query(
      value =
          "SELECT ROL_ID, MODULO_ID, MODULO.DESCRIPCION "
              + "FROM CONF_MODULO "
              + "INNER JOIN MODULO ON MODULO.ID = CONF_MODULO.MODULO_ID "
              + "WHERE ROL_ID = :RolUsuario",
      nativeQuery = true)
  List<Object[]> traerModulos(@Param("RolUsuario") int rolUsuario);

  @Query(
      value =
          "SELECT * FROM VW_VERIFICAR_APERTURAS WHERE USUARIO = :usuario AND FR = :fecha AND ESTADO = :estado AND TURNO = :turno",
      nativeQuery = true)
  List<Object[]> verificarAperturaCajero(
      @Param("usuario") String usuario,
      @Param("fecha") String fecha,
      @Param("estado") int estado,
      @Param("turno") String turno);

  @Query(
      value =
          "SELECT * FROM VW_HISTORIAL_CAJERO WHERE FECHA = :fecha AND ESTADO = :estado AND EMPRESA = :empresa AND TURNO = :turno AND USUARIO_ID = :usuario_id AND OPERACION_ID = operacion_id AND STATUS = :status ORDER BY ID",
      nativeQuery = true)
  List<Object[]> obtenerHistorial(
      @Param("fecha") String fecha,
      @Param("estado") int estado,
      @Param("empresa") String empresa,
      @Param("turno") String turno,
      @Param("usuario_id") int usuario_id,
      @Param("operacion_id") int operacion_id,
          @Param("status") int status);

  ModelUsuario findByUsuario(String usuario);

  @Query(
      value =
          "SELECT TOP 1 * FROM VW_VERIFICAR_APERTURAS WHERE USUARIO = :usuario AND FR = :fecha AND ESTADO = :estado AND TURNO = :turno AND EMPRESA = :empresa ORDER BY ID DESC",
      nativeQuery = true)
  List<Object[]> traerCuentaCajero(
      @Param("usuario") String usuario,
      @Param("fecha") String fecha,
      @Param("estado") int estado,
      @Param("turno") String turno,
      @Param("empresa") String empresa);

  @Procedure(name = "Usuario.pa_ValidarCierre")
  String pa_ValidarCierre(
      String nombre_usuario, String turno, String empresa, int opcion, String Resultado);

  @Query(
      value =
          "SELECT TOP 1 * FROM VW_VERIFICAR_APERTURAS WHERE USUARIO = :usuario AND FR = :fecha AND ESTADO = :estado AND TURNO = :turno AND EMPRESA = :empresa AND AJUSTE = :ajuste ORDER BY ID DESC",
      nativeQuery = true)
  List<Object[]> CuentasdeCierre(
      @Param("usuario") String usuario,
      @Param("fecha") String fecha,
      @Param("estado") int estado,
      @Param("turno") String turno,
      @Param("empresa") String empresa,
      @Param("ajuste") int ajuste);

  List<ModelUsuario> findByRol(int rol);

  @Query(value = "SELECT NOMBRE FROM VW_DATOS_USUARIO WHERE USUARIO = :usuario", nativeQuery = true)
  String traerCajero(
          @Param("usuario") String usuario
  );
}
