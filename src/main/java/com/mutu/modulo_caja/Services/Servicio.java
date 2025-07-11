package com.mutu.modulo_caja.Services;

import com.mutu.modulo_caja.Models.*;
import com.mutu.modulo_caja.Repository.TrasladoRepository;
import com.mutu.modulo_caja.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Servicio {

  @Autowired private UserRepository repoUsuario;

  @Autowired private SocioRepository repoSocio;

  @Autowired private AhorroRepository repoAhorro;

  @Autowired private CreditoRepository repoCredito;

  @Autowired private CapitalSocialRepository repoCS;

  @Autowired private TrasladoRepository repoTraslado;

  @Autowired private PrevisionSocialRepository repoPS;

  @Autowired private CierreRepository repoCierre;

  @Autowired private OperacionRepository repoOperaciones;

  @Autowired private EmpresaRepository repoEmpresa;

  @Autowired private CajaRepository repoCaja;

  @Transactional
  public HashMap validarLogin(String usuario, String password, String resultado, int rol) {
    return repoUsuario.pa_validarLogin(usuario, password, resultado, rol);
  }

  @Transactional
  public HashMap buscarSocios(
      int NumSocio, String NombreFomateado, int NumSocioEncontrado, String TipoDeSocio) {
    return repoSocio.pa_BuscarSocioXNumero(
        NumSocio, NombreFomateado, NumSocioEncontrado, TipoDeSocio);
  }

  public List<Object[]> traerModulos(int rolUsuario) {
    return repoUsuario.traerModulos(rolUsuario);
  }

  public List<Object[]> traerCoincidencias(String nombreCompleto) {
    return repoSocio.buscarPorNombreCompleto(nombreCompleto);
  }

  public ModelAhorro traerCuentaAhorroPorSocio(int socio) {
    return repoAhorro.findBySocio(socio);
  }

  @Transactional
  public Map<String, Object> AbonarAhorro(
      double cant_ahorro, String cuenta, String nombre_usuario, String empresa, String hora, String resultado, double ahorro_total,
      int transaccion_id) {
    return repoAhorro.pa_AbonarAhorro(cant_ahorro, cuenta, nombre_usuario, empresa, hora, resultado,ahorro_total,transaccion_id);
  }

  public List<Object[]> consultarCreditos(int socio, int status) {
    return repoCredito.buscarCreditos(socio, status);
  }

  public List<Object[]> desembolsosPendientes(int socio) {
    return repoCredito.buscarDesembolsosPendientes(socio);
  }

  @Transactional
  public Map<String, Object> ProcesarDesembolso(
      int CreditoID, String nombre_usuario, double monto_desembolso, String hora, String Resultado, int transaccion_id) {
    return repoCredito.pa_ProcesarDesembolso(
        CreditoID, nombre_usuario, monto_desembolso,hora, Resultado, transaccion_id);
  }

  public List<Object[]> retirosPendientes(int socio) {
    return repoAhorro.buscarRetirosPendientes(socio);
  }

  @Transactional
  public Map<String, Object> ProcesarRetiro(
          int ID, int num_socio, String nombre_usuario, double monto_retiro, String empresa,String hora, String turno,
          int transaccion_id, String Resultado) {
    return repoAhorro.pa_ProcesarRetiro(
        ID, num_socio, nombre_usuario, monto_retiro, empresa,hora, turno, transaccion_id, Resultado);
  }

  public List<ModelCapitalSocial> traerCuentasCS(int socio) {
    return repoCS.findByNumSocio(socio);
  }

  @Transactional
  public Map<String, Object>  AbonarCapitalSocial(
      int socio, String empresa, String hora, double monto, String nombre_usuario, String Resultado,
      double socialNGU,
      double socialMUT, int transaccion_id) {
    return repoCS.pa_AbonarCapitalSocial(socio, empresa,hora, monto, nombre_usuario, Resultado,  socialNGU,
    socialMUT,  transaccion_id);
  }

  public List<Object[]> verificarAperturaCajero(
      String usuario, String fecha, int estado, String turno) {
    return repoUsuario.verificarAperturaCajero(usuario, fecha, estado, turno);
  }

  @Transactional
  public Map<String, Object> procesarTraslado(
      String nombre_usuario,
      double monto_trasladar,
      int tipo,
      String empresa,
      String hora,
      String turno,
      String Resultado,
      int transaccion_id) {
    return repoTraslado.pa_ProcesarTraslado(
        nombre_usuario, monto_trasladar, tipo, empresa, hora, turno, Resultado,transaccion_id);
  }

  public List<Object[]> traerHistorial(
      String fecha,
      int estado,
      String empresa,
      String turno,
      int usuario_id,
      int operacion_id,
      int status) {
    return repoUsuario.obtenerHistorial(
        fecha, estado, empresa, turno, usuario_id, operacion_id, status);
  }

  public ModelUsuario traerDatosUsuario(String usuario) {
    return repoUsuario.findByUsuario(usuario);
  }

  public ModelPrevisionSocial traerCuentaPS(int numSocio, String empresa_cod) {
    return repoPS.findByNumSocioAndEmpresaCod(numSocio, empresa_cod);
  }

  @Transactional
  public Map<String, Object> AbonarPrevisionSocial(
      int num_socio, String empresa, String hora, double monto_pagado, String usuario, String Resultado, double monto_asignado,
      double monto_ticket, int transaccion_id) {
    return  repoPS.AbonarPrevisionSocial(num_socio, empresa,hora, monto_pagado, usuario, Resultado, monto_asignado,
    monto_ticket, transaccion_id);
  }

  public List<Object[]> traerCuentaCajero(
      String usuario, String fecha, int estado, String turno, String empresa) {
    return repoUsuario.traerCuentaCajero(usuario, fecha, estado, turno, empresa);
  }

  @Transactional
  public String procesarAjuste(
      String nombre_usuario,
      double faltante,
      double sobrante,
      String turno,
      String empresa,
      String Resultado) {
    return repoCierre.procesarAjuste(nombre_usuario, faltante, sobrante, turno, empresa, Resultado);
  }

  @Transactional
  public String pa_VerificarCierre(
      String nombre_usuario, String turno, String empresa, int opcion, String Resultado) {
    return repoUsuario.pa_ValidarCierre(nombre_usuario, turno, empresa, opcion, Resultado);
  }

  public List<Object[]> CuentasdeCierre(
      String usuario, String fecha, int estado, String turno, String empresa, int ajuste) {
    return repoUsuario.CuentasdeCierre(usuario, fecha, estado, turno, empresa, ajuste);
  }

  public ModelTraslado traerTrasladoCajero(String cuenta) {
    return repoTraslado.findByCuentaOrigen(cuenta);
  }

  @Transactional
  public String pa_procesarCierre(
      int cuenta_cajeroMut, int cuenta_cajeroNgu, String fecha, String Resultado) {
    return repoCierre.procesarCierre(cuenta_cajeroMut, cuenta_cajeroNgu, fecha, Resultado);
  }

  public List<ModelUsuario> traerCajero(int rol) {
    return repoUsuario.findByRol(rol);
  }

  public List<ModelOperaciones> traerOperacions() {
    return repoOperaciones.findAll();
  }

  public List<ModelEmpresa> traerEmpresas() {
    return repoEmpresa.findAll();
  }

  public List<Object[]> historialTraslados(
      int usuario, int tipo, int estado, String fecha, String turno, String empresa) {
    return repoTraslado.historialTraslados(usuario, tipo, estado, fecha, turno, empresa);
  }

  @Transactional
  public String pa_CancelarOperacion(
      int opcion,
      String nombre_usuario,
      int numero_socio,
      int id_transaccion,
      double monto,
      String empresa,
      String turno,
      String Resultado) {
    return repoOperaciones.pa_CancelarOperacion(
        opcion, nombre_usuario, numero_socio, id_transaccion, monto, empresa, turno, Resultado);
  }

  @Transactional
  public String pa_CancelarTraslados(
      int opcion,
      int id_traslado,
      String cuenta_origen,
      String cuenta_destino,
      double monto,
      String empresa,
      String Resultado) {
    return repoTraslado.pa_CancelarTraslados(
        opcion, id_traslado, cuenta_origen, cuenta_destino, monto, empresa, Resultado);
  }

  public ModelEmpresa traerEmpresa(String codigo){
    return repoEmpresa.findByCodigo(codigo);
  }

  public String traerCajeroPorUsuario (String  usuario){return repoUsuario.traerCajero(usuario);}

  public ModelCredito traerDatosCredito(int id){return repoCredito.findById(id);}

  public  ModelCaja traerDatosCaja(int usuario_id,String turno, String empresa, int estado
  ){return repoCaja.findByUsuarioIdAndTurnoAndEmpresaAndEstado(usuario_id, turno,empresa,estado);}

  public  Object[] traerCierreCajero(int id){
    return repoCierre.traerCierreCajero(id);
  }
  public ModelCredito traerDatosDesembolsoCancelado(int socio, int status, double monto, String empresa){
    return repoCredito.findBySocioAndStatusAndMontoAndEmpresa(socio, status, monto, empresa);
  }

  public List<Object[]> traerCuotasxCredito(int credito_id, int status) {
    return repoCredito.cuotasInmediatasXCredito(credito_id, status);
  }

  public Object[] traerUltimaCuotaPagada(int credito_id, int status) {
    return repoCredito.cuotaAnteriorXCredito(credito_id, status);
  }


}
