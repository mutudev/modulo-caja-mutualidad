package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelSocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface SocioRepository extends JpaRepository<ModelSocio, Integer> {

  @Procedure(name = "Socio.pa_BuscarSocioXNumero")
  HashMap pa_BuscarSocioXNumero(
      int NumSocio, String NombreFormateado, int NumSocioEncontrado, String TipoDeSocio);

  @Query(
      value =
          "SELECT (PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE, "
              + "NUM_SOCIO, TIPO_SOCIO.DESCRIPCION, EMPRESA.NOMBRE "
              + "FROM SOCIO "
              + "INNER JOIN TIPO_SOCIO ON TIPO_SOCIO.ID = SOCIO.CAT_TIPO_ID "
              + "INNER JOIN EMPRESA ON EMPRESA.CODIGO = SOCIO.EMPRESA_COD "
              + "WHERE (PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) LIKE %:nombreCompleto% "
              + "AND STATUS = 1",
      nativeQuery = true)
  List<Object[]> buscarPorNombreCompleto(@Param("nombreCompleto") String nombreCompleto);
}
