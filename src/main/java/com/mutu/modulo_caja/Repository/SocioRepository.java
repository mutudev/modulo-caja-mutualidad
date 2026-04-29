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

  // OPCIÓN 1: Query más simple - buscar el texto completo en el nombre concatenado
  @Query(
          value =
                  "SELECT " +
                          "    (NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE, " +
                          "    NUM_SOCIO, " +
                          "    TIPO_SOCIO.DESCRIPCION, " +
                          "    EMPRESA.NOMBRE " +
                          "FROM SOCIO " +
                          "INNER JOIN TIPO_SOCIO ON TIPO_SOCIO.ID = SOCIO.CAT_TIPO_ID " +
                          "INNER JOIN EMPRESA ON EMPRESA.CODIGO = SOCIO.EMPRESA_COD " +
                          "WHERE STATUS = 1 " +
                          "AND (NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) LIKE '%' + :nombreCompleto + '%'",
          nativeQuery = true
  )
  List<Object[]> buscarPorNombreCompleto(@Param("nombreCompleto") String nombreCompleto);

  // OPCIÓN 2: Para búsqueda de 2 palabras específicas (más eficiente)
  @Query(
          value =
                  "SELECT " +
                          "    (NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE, " +
                          "    NUM_SOCIO, " +
                          "    TIPO_SOCIO.DESCRIPCION, " +
                          "    EMPRESA.NOMBRE " +
                          "FROM SOCIO " +
                          "INNER JOIN TIPO_SOCIO ON TIPO_SOCIO.ID = SOCIO.CAT_TIPO_ID " +
                          "INNER JOIN EMPRESA ON EMPRESA.CODIGO = SOCIO.EMPRESA_COD " +
                          "WHERE STATUS = 1 " +
                          "AND CHARINDEX(:palabra1, NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0 " +
                          "AND CHARINDEX(:palabra2, NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0",
          nativeQuery = true
  )
  List<Object[]> buscarPorDospalabras(@Param("palabra1") String palabra1, @Param("palabra2") String palabra2);

  // Query adicional para 3 palabras
  @Query(
          value =
                  "SELECT " +
                          "    (NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE, " +
                          "    NUM_SOCIO, " +
                          "    TIPO_SOCIO.DESCRIPCION, " +
                          "    EMPRESA.NOMBRE " +
                          "FROM SOCIO " +
                          "INNER JOIN TIPO_SOCIO ON TIPO_SOCIO.ID = SOCIO.CAT_TIPO_ID " +
                          "INNER JOIN EMPRESA ON EMPRESA.CODIGO = SOCIO.EMPRESA_COD " +
                          "WHERE STATUS = 1 " +
                          "AND CHARINDEX(:palabra1, NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0 " +
                          "AND CHARINDEX(:palabra2, NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0 " +
                          "AND CHARINDEX(:palabra3, NOMBRES + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0",
          nativeQuery = true
  )
  List<Object[]> buscarPorTresPalabras(@Param("palabra1") String palabra1, @Param("palabra2") String palabra2, @Param("palabra3") String palabra3);

}
