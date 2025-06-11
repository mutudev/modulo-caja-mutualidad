package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelOperaciones;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperacionRepository extends JpaRepository<ModelOperaciones, Integer> {

    List<ModelOperaciones> findAll();

    @Procedure(name = "Operacion.pa_CancelarOperacion")
    String pa_CancelarOperacion (int opcion, String nombre_usuario, int numero_socio,
                                 int id_transaccion, double monto, String empresa, String turno ,
                                 String Resultado);

}
