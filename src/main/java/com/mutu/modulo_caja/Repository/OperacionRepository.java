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

    @Procedure(name = "Operacion.pa_CancelarAbonoCredito")
    String pa_CancelarAbonoCredito(
            double capital_transaccion,
            double interes_transaccion,
            double mora_transaccion,
            double iva_transaccion,
            double bonif_transaccion,
            double saldo_credito_transaccion,
            double pago_total_transaccion,
            String tipo_credito_transaccion,
            int transaccion_id,
            String cod_empresa,
            String turno,
            String fecha_transaccion,
            String hora_transaccion,
            int cuota_id,
            String nom_usuario,
            String Resultado
    );
}
