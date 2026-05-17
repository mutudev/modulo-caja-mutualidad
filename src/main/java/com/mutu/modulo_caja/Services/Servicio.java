package com.mutu.modulo_caja.Services;

import com.mutu.modulo_caja.Controllers.LoginController;
import com.mutu.modulo_caja.Models.*;
import com.mutu.modulo_caja.Models.DTO.PagoCuotaDTO;
import com.mutu.modulo_caja.Repository.TrasladoRepository;
import com.mutu.modulo_caja.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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

  @Autowired private CuotasRepository repoCuotas;

  @Autowired private ConfiguracionRepository repoConfiguracion;

  @Autowired private TipoCreditoRepository repoTipoCredito;

  @Autowired private TransaccionRepository repoTransaccion;

  @Autowired private HistorialCreditoRepository repoHistorialCredito;


  @Transactional
  public HashMap validarLogin(String usuario, String password, String resultado, int rol, int cajero) {
    return repoUsuario.pa_validarLogin(usuario, password, resultado, rol, cajero);
  }

  @Transactional
  public HashMap buscarSocios(
      int NumSocio, String NombreFomateado, int NumSocioEncontrado, String TipoDeSocio) {
    return repoSocio.pa_BuscarSocioXNumero(
        NumSocio, NombreFomateado, NumSocioEncontrado, TipoDeSocio);
  }

  @Transactional
  public List<PagoCuotaDTO> calcularPagoDeCuotas(
          Integer creditoId,
          Double tasaInteres,
          Double tasaMora,
          Double tasaIva,
          LocalDate fechaDesembolso) {

    List<Object[]> resultados = repoCuotas.pa_CalcularPagoDeCuotas(
            creditoId,
            tasaInteres,
            tasaMora,
            tasaIva,
            fechaDesembolso
    );

    List<PagoCuotaDTO> lista = new ArrayList<>();

    for (Object[] row : resultados) {
      PagoCuotaDTO dto = new PagoCuotaDTO(
              row[0] != null ? ((Number) row[0]).intValue()                        : null,
              row[1] != null ? ((Number) row[1]).intValue()                        : null,
              row[2] != null ? ((java.sql.Date) row[2]).toLocalDate()              : null,
              row[3] != null ? new BigDecimal(row[3].toString())                   : BigDecimal.ZERO,
              row[4] != null ? new BigDecimal(row[4].toString())                   : BigDecimal.ZERO,
              row[5] != null ? new BigDecimal(row[5].toString())                   : BigDecimal.ZERO,
              row[6] != null ? new BigDecimal(row[6].toString())                   : BigDecimal.ZERO,
              row[7] != null ? new BigDecimal(row[7].toString())                   : BigDecimal.ZERO,
              row[8] != null ? new BigDecimal(row[8].toString())                   : BigDecimal.ZERO,
              row[9] != null ? ((Number) row[9]).intValue()                        : null,
              row[10] != null ? ((java.sql.Date) row[10]).toLocalDate()              : null,
              row[11] != null ? ((java.sql.Date) row[11]).toLocalDate()              : null,
              row[12] != null ? ((java.sql.Date) row[12]).toLocalDate()              : null,
              row[13] != null ? new BigDecimal(row[13].toString())                   : BigDecimal.ZERO,
              row[14] != null ? new BigDecimal(row[14].toString())                   : BigDecimal.ZERO
      );
      lista.add(dto);
    }

    return lista;
  }

  public List<Object[]> traerModulos(int usuarioID) {
    return repoUsuario.traerModulos(usuarioID);
  }

  public ModelAhorro traerCuentaAhorroPorSocio(int socio) {
    return repoAhorro.findBySocio(socio);
  }



  public ModelSocio traerSocioXNumero(int numSocio){return  repoSocio.findByNumSocio(numSocio);}
  @Transactional
  public Map<String, Object> AbonarAhorro(
      double cant_ahorro,
      String cuenta,
      String nombre_usuario,
      String empresa,
      String hora,
      String resultado,
      double ahorro_total,
      int transaccion_id) {
    return repoAhorro.pa_AbonarAhorro(
        cant_ahorro,
        cuenta,
        nombre_usuario,
        empresa,
        hora,
        resultado,
        ahorro_total,
        transaccion_id);
  }

  public List<Object[]> consultarCreditos(int socio, int status) {
    return repoCredito.buscarCreditos(socio, status);
  }

  public List<Object[]> desembolsosPendientes(int socio) {
    return repoCredito.buscarDesembolsosPendientes(socio);
  }

  @Transactional
  public Map<String, Object> ProcesarDesembolso(
      int CreditoID,
      String nombre_usuario,
      double monto_desembolso,
      String hora,
      String Resultado,
      int transaccion_id) {
    return repoCredito.pa_ProcesarDesembolso(
        CreditoID, nombre_usuario, monto_desembolso, hora, Resultado, transaccion_id);
  }

  public List<Object[]> retirosPendientes(int socio) {
    return repoAhorro.buscarRetirosPendientes(socio);
  }

  @Transactional
  public Map<String, Object> ProcesarRetiro(
      int ID,
      int num_socio,
      String nombre_usuario,
      double monto_retiro,
      String empresa,
      String hora,
      String turno,
      int transaccion_id,
      String Resultado) {
    return repoAhorro.pa_ProcesarRetiro(
        ID,
        num_socio,
        nombre_usuario,
        monto_retiro,
        empresa,
        hora,
        turno,
        transaccion_id,
        Resultado);
  }

  public List<ModelCapitalSocial> traerCuentasCS(int socio) {
    return repoCS.findByNumSocio(socio);
  }

  public ModelTipoCredito traerTipoCredito(int id){return repoTipoCredito.findById(id);}

  @Transactional
  public Map<String, Object> AbonarCapitalSocial(
      int socio,
      String empresa,
      String hora,
      double monto,
      String nombre_usuario,
      String Resultado,
      double socialNGU,
      double socialMUT,
      int transaccion_id) {
    return repoCS.pa_AbonarCapitalSocial(
        socio,
        empresa,
        hora,
        monto,
        nombre_usuario,
        Resultado,
        socialNGU,
        socialMUT,
        transaccion_id);
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
      int validar,
      String Resultado,
      int transaccion_id) {
    return repoTraslado.pa_ProcesarTraslado(
        nombre_usuario, monto_trasladar, tipo, empresa, hora, turno, validar, Resultado, transaccion_id);
  }

  public List<Object[]> buscarSocioPorNombre(String nombreCompleto) {
    String[] palabras = nombreCompleto.trim().split("\\s+");

    if (palabras.length == 1) {
      return repoSocio.buscarPorNombreCompleto(nombreCompleto);
    } else if (palabras.length == 2) {
      return repoSocio.buscarPorDospalabras(palabras[0], palabras[1]);
    } else if (palabras.length >= 3) {
      return repoSocio.buscarPorTresPalabras(palabras[0], palabras[1], palabras[2]);
    }

    return new ArrayList<>();
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
      int num_socio,
      String empresa,
      String hora,
      double monto_pagado,
      String usuario,
      String Resultado,
      double monto_asignado,
      double monto_ticket,
      int transaccion_id) {
    return repoPS.AbonarPrevisionSocial(
        num_socio,
        empresa,
        hora,
        monto_pagado,
        usuario,
        Resultado,
        monto_asignado,
        monto_ticket,
        transaccion_id);
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
      double monto_fis,
      String Resultado) {
    return repoCierre.procesarAjuste(nombre_usuario, faltante, sobrante, turno, empresa, monto_fis, Resultado);
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
  public Map<String, Object> pa_procesarCierre(
      int cuenta_cajeroMut, int cuenta_cajeroNgu, String fecha, double saldo_fisico, String usuario, String Resultado, int Cierre_id_mut, int Cierre_id_ngu) {
    return repoCierre.procesarCierre(cuenta_cajeroMut, cuenta_cajeroNgu, fecha, saldo_fisico, usuario, Resultado, Cierre_id_mut, Cierre_id_ngu);
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

  public ModelEmpresa traerEmpresaConRS(String rs) {
    return repoEmpresa.findByRazonSocial(rs);
  }

  public ModelTraslado traerTrasladoApertura(String cuentaDestino){return repoTraslado.findByCuentaDestino(cuentaDestino);}

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

  public ModelEmpresa traerEmpresa(String codigo) {
    return repoEmpresa.findByCodigo(codigo);
  }

  public String traerCajeroPorUsuario(String usuario) {
    return repoUsuario.traerCajero(usuario);
  }

  public ModelCredito traerDatosCredito(int id) {
    return repoCredito.findById(id);
  }

  public ModelCaja traerDatosCaja(int usuario_id, String turno, String empresa, int estado) {
    return repoCaja.findByUsuarioIdAndTurnoAndEmpresaAndEstado(usuario_id, turno, empresa, estado);
  }

  public ModelCaja traerCajaConId(int id) {
    return repoCaja.findById(id);
  }

  public Object[] traerCierreCajero(int id) {
    return repoCierre.traerCierreCajero(id);
  }

  public ModelCredito traerDatosDesembolsoCancelado(
      int socio, int status, double monto, String empresa) {
    return repoCredito.findBySocioAndStatusAndMontoAndEmpresa(socio, status, monto, empresa);
  }

  public Object[] traerUltimaCuotaPagada(int credito_id, int status) {
    return repoCredito.cuotaAnteriorXCredito(credito_id, status);
  }

  public Object[] traerUltimaCuotaPagadaChecar(int credito_id) {
    return repoCredito.cuotaUltimaSaldada(credito_id);
  }

  public Object[] cuotaUltimaAfectada(int credito_id) {
    return repoCredito.cuotaUltimaAfectada(credito_id);
  }


  public Object[] cuotaAnterior(int credito_id, int num_cuota) {
    return repoCredito.cuotaAnterior(credito_id, num_cuota);
  }

  public Object[] traerUltimaCuotaConNum(int credito_id, int status, int num_cuota) {
    return repoCredito.cuotaAnteriorConNum(credito_id, status, num_cuota);
  }

  public List<Object[]> traerCuotasPrueba(int credito_id, int status, Pageable limit) {
    return repoCredito.cuotasInmediatasXCredito(credito_id, status, limit);
  }

  public int obtenerPlazoCredito(int id) {
    return repoCredito.obtenerPlazo(id);
  }

  @Transactional
  public String pa_CancelarAbonoCredito(
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
  ) {
    return repoOperaciones.pa_CancelarAbonoCredito(
            capital_transaccion,
            interes_transaccion,
            mora_transaccion,
            iva_transaccion,
            bonif_transaccion,
            saldo_credito_transaccion,
            pago_total_transaccion,
            tipo_credito_transaccion,
            transaccion_id,
            cod_empresa,
            turno,
            fecha_transaccion,
            hora_transaccion,
            cuota_id,
            nom_usuario,
            Resultado
    );
  }


  public ModelCuotas obtenerPrimeraCuotaAfectada(int creditoId) {
    return repoCuotas
            .findFirstByCreditoIdAndFechaPRealizadaIsNotNullOrderByNumCuotaDesc(creditoId);
  }




  @Transactional
  public Map<String, Object> pa_PagarCredito(
      int opcion,
      double monto_capital,
      double intereses,
      double mora,
      double iva,
      int cuota_id,
      int credito_id,
      double total_cuota_pendiente,
      int numero_cuota,
      double bonificacion,
      double total_pago,
      int numero_cuotas,
      String nombre_usuario,
      String empresa,
      String hora,
      int num_socio,
      String turno,
      double total_credito,
      String datos_credito,
      int transaccion_id,
      String Resultado,
      String saldo_ticket,
      String intereses_devueltos,
      String mora_devueltos,
      String iva_devueltos,
      String capital_devueltos,
      String bonif_devuelto) {
    return repoCredito.pa_PagarCredito(
        opcion,
        monto_capital,
        intereses,
        mora,
        iva,
        cuota_id,
        credito_id,
        total_cuota_pendiente,
        numero_cuota,
        bonificacion,
        total_pago,
        numero_cuotas,
        nombre_usuario,
        empresa,
        hora,
        num_socio,
        turno,
            total_credito,
            datos_credito,
        transaccion_id,
        Resultado,
        saldo_ticket,
        intereses_devueltos,
        mora_devueltos,
        iva_devueltos,
        capital_devueltos,
            bonif_devuelto);
  }



  public LocalDate traerFechaHoy() {
    return repoConfiguracion.findById(1).get().getFechaSistema();
  }

  public ModelEmpresa traerEmpresaConNombre(String empresa) {
    return repoEmpresa.findByNombre(empresa);
  }


  //Nuevos

  public List<ModelCaja> traerCajas(int usuarioId, LocalDate fr, int estado, String turno) {
    return repoCaja.findByUsuarioIdAndFrAndEstadoAndTurno(usuarioId, fr, estado, turno);
  }

  public ModelCaja traerCajasParaCierre(int usuarioId, LocalDate fr, int estado, String turno, String empresa, int ajuste) {
    return repoCaja.findByUsuarioIdAndFrAndEstadoAndTurnoAndEmpresaAndAjuste(usuarioId, fr, estado, turno, empresa, ajuste);
  }

  public ModelCaja traerCuentaDeCaja(int usuarioId, LocalDate fr, int estado, String turno, String empresa) {
    return repoCaja.findByUsuarioIdAndFrAndEstadoAndTurnoAndEmpresa(usuarioId, fr, estado, turno, empresa);
  }

  public ModelCaja traerCaja(int usuarioId, LocalDate fr, int estado, String empresa) {
    return repoCaja.findByUsuarioIdAndFrAndEstadoAndEmpresa(usuarioId, fr, estado, empresa);
  }

  public List<ModelOperaciones> traerOperaciones() {
    return repoOperaciones.findAll();
  }

  public List<ModelCaja> traerCajasDiferentesAHoy(int usuarioId, int estado, LocalDate fr) {
    return repoCaja.findByUsuarioIdAndEstadoAndFrNot(usuarioId, estado, fr);
  }

  public ModelConfiguracion obtenerConfiguraciones() {
    return repoConfiguracion.findById(1).get();
  }

  //Pagar Crédito
  @Transactional
  public ModelTransaccion pagarCredito(List<PagoCuotaDTO> cuotas, int creditoId) {

    //Obtenemos el crédito
    System.out.println(creditoId);
    ModelCredito credito = repoCredito.findById(creditoId);

    //Cargar las cuotas
    Map<Integer, ModelCuotas> cuotasMap = new HashMap<>();
    for (PagoCuotaDTO dto : cuotas) {
      ModelCuotas cuota = repoCuotas.findByCreditoIdAndNumCuota(creditoId, dto.getNumCuota());
      cuotasMap.put(dto.getNumCuota(), cuota);
    }

    ModelUsuario usuario = traerDatosUsuario(LoginController.usuarioLoggeado);
    //Traer la caja
    ModelCaja caja = traerCaja(usuario.getId(), traerFechaHoy(), 1, credito.getEmpresa());

    double capitalPagado = 0;
    double totalPagado = 0;
    double bonifPagado = 0;
    double interesesPagado = 0;
    double ivaPagado = 0;
    double moraPagado = 0;

    //Primero guardar la transacción para despúes guardar en el Historial Credito Detalle
    ModelTransaccion transaccion = new ModelTransaccion();
    transaccion.setUsuarioId(usuario.getId());
    transaccion.setOperacionId(2);
    transaccion.setSocioId(credito.getSocio());

    int cuotaInmediata = 0;
    //Crear el primer ciclo solo para obtener acumulados
    for(PagoCuotaDTO dto : cuotas) {
      capitalPagado += (cuotasMap.get(dto.getNumCuota()).getCapital().doubleValue() - dto.getCapital().doubleValue());
      totalPagado += dto.getTotal().doubleValue();
      bonifPagado += dto.getBonif().doubleValue();
      interesesPagado += dto.getIntereses().doubleValue();
      moraPagado += dto.getMora().doubleValue();
      ivaPagado += dto.getIva().doubleValue();

      if (cuotaInmediata == 0) {
        cuotaInmediata = dto.getNumCuota();
      }
    }

    //Actualizar el saldo de cajas
    if (caja.getSal_final() == 0) {
      caja.setSal_final(caja.getSal_inicial() + totalPagado);
    } else {
      caja.setSal_final(caja.getSal_final() + totalPagado);
    }

    transaccion.setSaldo(BigDecimal.valueOf(totalPagado));
    transaccion.setStatus(true);
    transaccion.setFechaRegistro(traerFechaHoy());
    transaccion.setHora(LocalTime.now());
    transaccion.setEmpresa(credito.getEmpresa());
    transaccion.setCajaId(caja.getId());
    transaccion.setCapitalCreditoPagado(BigDecimal.valueOf(capitalPagado));
    transaccion.setInteresesCreditoPagado(BigDecimal.valueOf(interesesPagado));
    transaccion.setMoraCreditoPagado(BigDecimal.valueOf(moraPagado));
    transaccion.setBonifCreditoPagado(BigDecimal.valueOf(bonifPagado));
    transaccion.setIvaCreditoPagado(BigDecimal.valueOf(ivaPagado));
    transaccion.setSaldoCredito(BigDecimal.valueOf(credito.getSaldo() - capitalPagado));
    transaccion.setCuotaAfectada(cuotaInmediata);
    transaccion.setCreditoAfectado(creditoId);
    transaccion.setAhorroAlMomento(null);
    transaccion.setTotalCuotaPagada(BigDecimal.valueOf(totalPagado));

    //Insertar transacción
    ModelTransaccion transaccionInsertada = repoTransaccion.save(transaccion);


    double capitalCreditoIterable = credito.getSaldo();
    //Ahora, generar con un ciclo la inserción de Historial Credito Detalle
    for (PagoCuotaDTO dto : cuotas) {
      //Obtenemos la cuota

      if (dto.getFechaRealizada() != null) {
        //Creamos un nuevo ModelHistorialCredito
        ModelHistorialCredito historialCredito = new ModelHistorialCredito();
        historialCredito.setFechaP(traerFechaHoy());
        historialCredito.setFechaV(dto.getFechaP());
        historialCredito.setNumCuota(dto.getNumCuota());
        historialCredito.setCapitalPagado(BigDecimal.valueOf(cuotasMap.get(dto.getNumCuota()).getCapital().doubleValue() - dto.getCapital().doubleValue()));
        historialCredito.setInteresPagado(dto.getIntereses());
        historialCredito.setMoraPagado(dto.getMora());
        historialCredito.setIvaPagado(dto.getIva());
        historialCredito.setTotalPagado(dto.getTotal());
        //Es la resta de lo que tiene el capital menos lo que realmente pagué
        //Lo que realmente pagué es el monto de CUOTAS_EJEMPLO - LO QUE ENVÍO EN MI DTO
        historialCredito.setSaldoCredito(BigDecimal.valueOf(capitalCreditoIterable - (cuotasMap.get(dto.getNumCuota()).getCapital().doubleValue() - dto.getCapital().doubleValue())));
        capitalCreditoIterable -= (cuotasMap.get(dto.getNumCuota()).getCapital().doubleValue() - dto.getCapital().doubleValue());
        historialCredito.setOperacionId(transaccionInsertada.getId());
        repoHistorialCredito.save(historialCredito);
      }


    }



    //Recorremos las cuotas ya procesadas en el controller
    for (PagoCuotaDTO dto : cuotas) {
      //Obtenemos la cuota una por una
      //Iteramos la cuota una por una
      //Primero afecta el capital
      //Aquí no hay mucho problema, solo es settear lo que trajo la cuota de capital

      //Obtener la cuota
      ModelCuotas cuota = cuotasMap.get(dto.getNumCuota());

      cuota.setCapital(dto.getCapital());

      //Settear los intereses
      //Primero ver si es el primer pago, si lo es, settear directamente lo que trajo el DTO
      if (cuota.getFechaPRealizada() == null) {
        cuota.setIntereses(dto.getIntereses());
      } else {
        //Si no es el primer pago, es decir, fecha realizada ya tenía algo (Sumamos a lo que ya tenía lo que trajo)
        cuota.setIntereses(cuota.getIntereses().add(dto.getIntereses()));
      }

      //Settear el IVA, es lo mismo que como funcionan los intereses
      if (cuota.getFechaPRealizada() == null) {
        cuota.setIva(dto.getIva());
      } else {
        //Si no es el primer pago, es decir, fecha realizada ya tenía algo (Sumamos a lo que ya tenía lo que trajo)
        cuota.setIva(cuota.getIva().add(dto.getIva()));
      }

      //Settear la mora
      if (cuota.getFechaPRealizada() == null) {
        cuota.setMora(dto.getMora());
      } else {
        //Si no es el primer pago, es decir, fecha realizada ya tenía algo (Sumamos a lo que ya tenía lo que trajo)
        cuota.setMora(cuota.getMora().add(dto.getMora()));
      }

      //Settear la bonificación
      if (cuota.getFechaPRealizada() == null) {
        cuota.setBonif(dto.getBonif());
      } else {
        //Si no es el primer pago, es decir, fecha realizada ya tenía algo (Sumamos a lo que ya tenía lo que trajo)
        cuota.setBonif(cuota.getBonif().add(dto.getBonif()));
      }

      //Settear el total
      if (cuota.getFechaPRealizada() == null) {
        cuota.setTotal(dto.getTotal());
      } else {
        //Si no es el primer pago, es decir, fecha realizada ya tenía algo (Sumamos a lo que ya tenía lo que trajo)
        cuota.setTotal(cuota.getTotal().add(dto.getTotal()));
      }

      //Actualizar el status, si el capital es 0 pues ya se cubrió toda la cuota
      if (dto.getCapital().doubleValue() == 0) {
          cuota.setStatus(dto.getStatus());
      }

      //Como las fechas ya las setteamos directamente en el método del controller y ya iteramos ahí, aquí solo hace falta
      //settearlas
      cuota.setFechaAnterior(dto.getFechaAnterior());
      cuota.setFechaPRealizada(dto.getFechaRealizada());
      cuota.setFechaTerminoPago(dto.getFechaTerminoPago());

      //Settear acumulados de interes y de mora
      //El hecho de que sea cero indica que no tiene acumulados entonces le guardamos lo que haya traido el DTO
      //Si el DTO trajo 0 de acumulados entonces no hay problema seguirá siendo cero
      if (cuota.getInteresAcumulado().doubleValue() == 0) {
        cuota.setInteresAcumulado(dto.getInteresesAcumulados());
      } else {
        //Si tenía algo, hay que ver si lo cubrió, es decir, ver si el acumulado llegó en cero
        if (dto.getInteresesAcumulados().doubleValue() != 0) {
          //Si no llegó en cero es que aún debe algo
          //Por lo tanto le almacenamos ese algo
          cuota.setInteresAcumulado(dto.getInteresesAcumulados());
        } else {
          //Si llegó en cero ahora el acumulado y antes tenía algo quiere decir que lo cubrió el gaysito
          cuota.setInteresAcumulado(BigDecimal.ZERO);
        }
      }

      //Repetir misma lógica con la mora
      if (cuota.getMoraAcumulado().doubleValue() == 0) {
        cuota.setMoraAcumulado(dto.getMoraAcumulados());
      } else {
        //Si tenía algo, hay que ver si lo cubrió, es decir, ver si el acumulado llegó en cero
        if (dto.getMoraAcumulados().doubleValue() != 0) {
          //Si no llegó en cero es que aún debe algo
          //Por lo tanto le almacenamos ese algo
          cuota.setMoraAcumulado(dto.getMoraAcumulados());
        } else {
          //Si llegó en cero ahora el acumulado y antes tenía algo quiere decir que lo cubrió el gaysito
          cuota.setMoraAcumulado(BigDecimal.ZERO);
        }
      }

      repoCuotas.save(cuota);
    }

    //Una vez actualizadas las cuotas
    //Actualizamos el saldo del crédito en CAT_CREDITOS
    credito.setSaldo(credito.getSaldo() - capitalPagado);
    repoCredito.save(credito);

    return transaccionInsertada;




  }


}
