package com.mutu.modulo_caja.Repository;

import com.mutu.modulo_caja.Models.ModelEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<ModelEmpresa, String> {

    List<ModelEmpresa> findAll();

    ModelEmpresa findByCodigo(String codigo);

    ModelEmpresa findByRazonSocial(String razonSocial);

}
