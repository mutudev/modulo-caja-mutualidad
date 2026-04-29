package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionRepository extends JpaRepository<ModelConfiguracion, Integer> {

}
