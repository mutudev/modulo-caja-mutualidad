package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Models.*;
import com.mutu.modulo_caja.Services.Servicio;
import com.mutu.modulo_caja.utils.PrintJob;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ReimpresionController {

  @FXML private TextField txtSocio, txtID, txtOperacion, txtEmpresa, txtFecha, txtMonto,
          txtNomSocio, txtCapital, txtInteres, txtMora, txtIva, txtBonif;
  @FXML private Label lblTitulo, lblEmpresa, lblSocio, lblOperacion, lblNombre,
          lblCapital, lblInteres, lblMora, lblIva, lblBonif;
  @FXML private Button btnReimprimir, btnRegresar;

  // Constantes de formato
  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FORMATO_HORA  = DateTimeFormatter.ofPattern("HH:mm:ss");

  String capital_pagado = "", interes_pagado = "", mora_pagada = "", iva_pagado = "";
  String bonif_aplicada = "", saldo_credito = "", tipo_credito = "", cuota_afectada = "";
  NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

  public OperacionesController controller;
  public HistorialController controllerhisto;

  public String nombre, usuario, socio, id, operacion, empresa, fecha, monto,
          turno, tipo_traslado, cuenta_origen, cuenta_destino, hora;
  public int opcion = 0;
  public int operaciontipo = 0;
  public double montoExtraido = 0;
  public String empresaEnviar = "";

  @Autowired public Servicio servicio;

  // ─── Helpers compartidos ────────────────────────────────────────────────

  /** Muestra un Alert y espera. contenido puede ser null para omitirlo. */
  private void mostrarAlerta(Alert.AlertType tipo, String titulo, String header, String contenido) {
    Alert alert = new Alert(tipo);
    alert.setTitle(titulo);
    alert.setHeaderText(header);
    if (contenido != null) alert.setContentText(contenido);
    alert.showAndWait();
  }

  /** Cierra la ventana asociada a un nodo cualquiera de la escena. */
  private void cerrarVentana(javafx.scene.Node nodo) {
    ((Stage) nodo.getScene().getWindow()).close();
  }

  /** Datos de empresa cacheados en un solo objeto para evitar 4 llamadas al servicio. */
  private String[] getDatosEmpresa(String cod) {
    ModelEmpresa e = servicio.traerEmpresa(cod);
    return new String[]{
            e.getRazonSocial(),
            e.getRfc(),
            e.getCalle() + " " + e.getCruzamiento() + " COL. CENTRO"
    };
  }

  /** Convierte monto en palabras. */
  private String enPalabras(double monto) {
    return MoneyConverters.SPANISH_BANKING_MONEY_VALUE
            .asWords(BigDecimal.valueOf(monto)).toUpperCase() + " MXN";
  }

  /** Extrae monto del campo txtMonto y lo guarda en montoExtraido. */
  private void parsearMontoExtraido() {
    try {
      montoExtraido = formatoMoneda.parse(txtMonto.getText().trim()).doubleValue();
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  /**
   * Lanza un reporte Jasper en un Task con pantalla de carga.
   * @param reportPath ruta al .jasper dentro de resources
   * @param pars       parámetros del reporte
   * @param titulo     título del JasperViewer
   */
  private void lanzarReporteJasper(String reportPath, Map<String, Object> pars, String titulo) {
    Stage loadingStage = crearLoadingStage();

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        try {
          InputStream isRepo = getClass().getResourceAsStream(reportPath);
          JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());
          Platform.runLater(() -> {
            JasperViewer viewer = new JasperViewer(jpRepo, false);
            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle(titulo);
            viewer.setVisible(true);
          });
        } catch (Exception e) {
          e.printStackTrace();
          Platform.runLater(() -> mostrarAlerta(Alert.AlertType.ERROR,
                  "ERROR", "ERROR AL GENERAR EL REPORTE", "OCURRIÓ UN ERROR: " + e.getMessage()));
        }
        return null;
      }
    };
    task.setOnSucceeded(e -> loadingStage.close());
    task.setOnFailed(e -> loadingStage.close());
    loadingStage.show();
    new Thread(task).start();
  }

  /** Construye y devuelve el Stage de carga. */
  private Stage crearLoadingStage() {
    Stage loadingStage = new Stage();
    loadingStage.initModality(Modality.APPLICATION_MODAL);
    loadingStage.initStyle(StageStyle.UNDECORATED);
    loadingStage.setAlwaysOnTop(true);

    ProgressIndicator pi = new ProgressIndicator();
    pi.setPrefSize(60, 60);

    Label lbl = new Label("Generando Reimpresión...");
    lbl.setFont(Font.font("System", FontWeight.BOLD, 16));
    lbl.setTextFill(Color.web("#39577c"));

    VBox box = new VBox(20, pi, lbl);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(30));
    box.setStyle("-fx-background-color: white; -fx-border-color: #185754; -fx-border-width: 2;");

    loadingStage.setScene(new Scene(box, 300, 150));
    loadingStage.centerOnScreen();
    return loadingStage;
  }

  /** Envía a la impresora matricial un archivo ya generado. */
  private void imprimirArchivo(String archivo) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(archivo);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return;
    }

    DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
    Doc document = new SimpleDoc(inputStream, docFormat, null);
    PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

    if (defaultPrintService == null) {
      mostrarAlerta(Alert.AlertType.ERROR, "ERROR IMPRIMIENDO", "ERROR IMPRIMIENDO", null);
      return;
    }
    try {
      defaultPrintService.createPrintJob().print(document, attributeSet);
    } catch (PrintException e) {
      mostrarAlerta(Alert.AlertType.ERROR, "ERROR IMPRIMIENDO", "ERROR IMPRIMIENDO", e.getMessage());
    }
  }

  /**
   * Muestra los campos de crédito (capital, interés, mora, iva, bonif)
   * cuando la operación es tipo 2. Reutilizado en setDatos y setDatosCancelacion.
   */
  private void mostrarCamposCredito(String cap, String inte, String mora, String iva, String bonif) {
    txtCapital.setText(cap);   txtCapital.setVisible(true);  lblCapital.setVisible(true);
    txtInteres.setText(inte);  txtInteres.setVisible(true);  lblInteres.setVisible(true);
    txtMora.setText(mora);     txtMora.setVisible(true);     lblMora.setVisible(true);
    txtIva.setText(iva);       txtIva.setVisible(true);      lblIva.setVisible(true);
    txtBonif.setText(bonif);   txtBonif.setVisible(true);    lblBonif.setVisible(true);
  }

  /**
   * Rellena los campos de traslado comunes a setDatosTraslados y setDatosTrasladosHistorial.
   */
  private void rellenarCamposTraslado(int tipoOp, String cuentaOrigen, String cuentaDestino,
                                      String tipoTras, String fechaVal, String montoVal,
                                      String usuarioVal, String idVal) {
    txtSocio.setText(usuarioVal);
    txtID.setText(idVal);
    txtEmpresa.setText(tipoTras);
    txtFecha.setText(fechaVal);
    txtMonto.setText(montoVal);
    lblSocio.setText("USUARIO:");
    lblEmpresa.setText("TIPO:");

    if (tipoOp == 6) {
      txtNomSocio.setText(cuentaOrigen);
      txtOperacion.setText("CUENTA DE CAJA: " + LoginController.usuarioLoggeado);
      lblNombre.setText("ORIGEN:");
      lblOperacion.setText("DESTINO:");
    } else {
      txtNomSocio.setText("CUENTA DE CAJA: " + LoginController.usuarioLoggeado);
      txtOperacion.setText(cuentaDestino);
      lblNombre.setText("DESTINO:");
      lblOperacion.setText("ORIGEN:");
    }
  }

  // ─── Setters de datos ───────────────────────────────────────────────────

  public void setDatosTraslados(String id, String usuario, String monto, String tipo_traslado,
                                String cuenta_origen, String cuenta_destino, String fecha,
                                int opcion, OperacionesController controller, String turno) {
    this.id = id;
    this.opcion = opcion;
    this.usuario = usuario;
    this.monto = monto;
    this.controller = controller;
    this.tipo_traslado = tipo_traslado;
    this.cuenta_origen = cuenta_origen;
    this.cuenta_destino = cuenta_destino;
    this.fecha = fecha;
    this.turno = turno;

    rellenarCamposTraslado(opcion, cuenta_origen, cuenta_destino, tipo_traslado, fecha, monto, usuario, id);
    lblTitulo.setText("CANCELAR TRASLADO");
    btnReimprimir.setText("CANCELAR");
  }

  public void setDatosTrasladosHistorial(String id, String usuario, String monto, String tipo_traslado,
                                         String cuenta_origen, String cuenta_destino, String fecha,
                                         int operacionreimpresion, HistorialController controller,
                                         String turno, String empresa, String hora) {
    this.id = id;
    this.operaciontipo = operacionreimpresion;
    this.usuario = usuario;
    this.monto = monto;
    this.controllerhisto = controller;
    this.tipo_traslado = tipo_traslado;
    this.cuenta_origen = cuenta_origen;
    this.cuenta_destino = cuenta_destino;
    this.fecha = fecha;
    this.turno = turno;
    this.empresa = empresa;
    this.hora = hora;

    rellenarCamposTraslado(operacionreimpresion, cuenta_origen, cuenta_destino, tipo_traslado, fecha, monto, usuario, id);
    lblTitulo.setText("REIMPRIMIR TRASLADO");
    btnReimprimir.setText("REIMPRIMIR");
  }

  public void setDatos(String socio, String id, String operacion, String empresa, String fecha,
                       String monto, String nombre, int operaciontipo, String hora,
                       String capital_pagado, String interes_pagado, String mora_pagada,
                       String iva_pagado, String bonif_aplicada, String saldo_credito,
                       String tipo_credito) {
    this.nombre = nombre;
    this.socio = socio;
    this.id = id;
    this.operacion = operacion;
    this.empresa = empresa;
    this.fecha = fecha;
    this.monto = monto;
    this.hora = hora;
    this.operaciontipo = operaciontipo;
    this.capital_pagado = capital_pagado;
    this.interes_pagado = interes_pagado;
    this.mora_pagada = mora_pagada;
    this.iva_pagado = iva_pagado;
    this.saldo_credito = saldo_credito;
    this.tipo_credito = tipo_credito;
    this.bonif_aplicada = bonif_aplicada;

    txtSocio.setText(socio);       txtID.setText(id);
    txtOperacion.setText(operacion); txtEmpresa.setText(empresa);
    txtFecha.setText(fecha);       txtMonto.setText(monto);
    txtNomSocio.setText(nombre);

    if (operaciontipo == 2) {
      mostrarCamposCredito(capital_pagado, interes_pagado, mora_pagada, iva_pagado, bonif_aplicada);
    }
  }

  public void setDatosCancelacion(String socio, String id, String operacion, String empresa,
                                  String fecha, String monto, String nombre, int opcion,
                                  String turno, String usuario, OperacionesController controller,
                                  String hora, String capital_pagado, String interes_pagado,
                                  String mora_pagada, String iva_pagado, String bonif_aplicada,
                                  String saldo_credito, String tipo_credito, String cuota_afectada) {
    this.nombre = nombre;
    this.socio = socio;
    this.capital_pagado = capital_pagado;
    this.usuario = usuario;
    this.id = id;
    this.operacion = operacion;
    this.empresa = empresa;
    this.hora = hora;
    this.cuota_afectada = cuota_afectada;
    this.interes_pagado = interes_pagado;
    this.mora_pagada = mora_pagada;
    this.iva_pagado = iva_pagado;
    this.bonif_aplicada = bonif_aplicada;
    this.saldo_credito = saldo_credito;
    this.tipo_credito = tipo_credito;
    this.fecha = fecha;
    this.monto = monto;
    this.opcion = opcion;
    this.turno = turno;
    this.controller = controller;

    txtSocio.setText(socio);
    txtID.setText(id);
    txtOperacion.setText(operacion);
    txtEmpresa.setText(empresa);
    txtFecha.setText(fecha);
    txtMonto.setText(monto);
    txtNomSocio.setText(nombre);
    lblTitulo.setText("CANCELACIÓN DE OPERACIONES");
    btnReimprimir.setText("CANCELAR");

    if (opcion == 2) {
      mostrarCamposCredito(capital_pagado, interes_pagado, mora_pagada, iva_pagado, bonif_aplicada);
    }
  }

  // ─── Eventos de teclado / cierre ────────────────────────────────────────

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      cerrar();
    } else if (event.getCode().equals(KeyCode.ENTER)) {
      filtrarOpcion();
    }
  }

  @FXML
  public void cerrar() {
    cerrarVentana(txtSocio);
  }

  // ─── Cancelar operación ──────────────────────────────────────────────────

  public void cancelarOperacion() {
    String headerConf = (opcion == 6 || opcion == 7)
            ? "¿ESTÁ SEGURO QUE DESEA CANCELAR ESTE TRASLADO?"
            : "¿ESTÁ SEGURO QUE DESEA CANCELAR ESTA OPERACIÓN?";

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("CANCELACIÓN");
    confirm.setHeaderText(headerConf);
    confirm.setContentText("EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");
    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isEmpty() || result.get() != ButtonType.OK) return;

    parsearMontoExtraido();

    if (opcion == 6 || opcion == 7) {
      cancelarTraslado();
    } else {
      cancelarOperacionNormal();
    }
  }

  // Extracción: lógica de cancelación de traslados separada
  private void cancelarTraslado() {
    int id_traslado = Integer.parseInt(txtID.getText().trim());
    InputStream isLogo;
    String protesonom;

    String cuentaRef = (opcion == 6) ? cuenta_origen : cuenta_destino;
    if (cuentaRef.equals("BVDA-NGU")) {
      empresaEnviar = "0002";
      protesonom = "CASIMIRO UITZ VILLANUEVA";
      isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
    } else {
      empresaEnviar = "0001";
      protesonom = "CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL";
      isLogo = getClass().getResourceAsStream("/assets/images/logo-mut.png");
    }

    String res = servicio.pa_CancelarTraslados(opcion, id_traslado,
            cuenta_origen, cuenta_destino, montoExtraido, empresaEnviar, "");

    if (!res.equals("CORRECTO")) {
      mostrarAlerta(Alert.AlertType.ERROR, "ERROR CANCELANDO EL TRASLADO",
              "ERROR CANCELANDO EL TRASLADO", "ERROR AL TRATAR DE CANCELAR");
      return;
    }

    try {
      LocalDate now = servicio.traerFechaHoy();
      String fechaTicket    = now.format(FORMATO_FECHA);
      String horaticket     = LocalTime.now().format(FORMATO_HORA);
      String nomcajero      = servicio.traerCajeroPorUsuario(usuario);
      String[] datosEmp     = getDatosEmpresa(empresaEnviar);
      String moneyAsWords   = enPalabras(montoExtraido);

      boolean esApertura = cuenta_origen.equals("BVDA-NGU") || cuenta_origen.equals("BVDA-MUT");
      String reportPath, titulo, tituloRep, origenRep, destinoRep, descripcion;

      if (esApertura) {
        reportPath  = "/Reports/cancelacion_traslado_apertura.jasper";
        titulo      = "REPORTE DE CANCELACION DE TRASLADO";
        tituloRep   = "REPORTE DE CANCELACION DE TRASLADO DE APERTURA";
        origenRep   = "CUENTA DE CAJERO " + LoginController.usuarioLoggeado;
        destinoRep  = cuenta_origen;
        descripcion = "TRASLADO DE APERTURA cancelado por " + txtMonto.getText()
                + " (" + moneyAsWords + ") efectuado en la sucursal de UMAN autorizado por " + protesonom + ".";
      } else {
        reportPath  = "/Reports/cancelar_traslado_cierre.jasper";
        titulo      = "REPORTE DE CANCELACION DE TRASLADO";
        tituloRep   = "REPORTE DE CANCELACION DE TRASLADO DE CIERRE";
        origenRep   = cuenta_destino;
        destinoRep  = "CUENTA DE CAJERO " + LoginController.usuarioLoggeado;
        descripcion = "TRASLADO DE CIERRE cancelado por " + txtMonto.getText()
                + " (" + moneyAsWords + ") efectuado en la sucursal de UMAN autorizado por " + protesonom + ".";
      }

      Map<String, Object> pars = new HashMap<>();
      pars.put("Empresa",    datosEmp[0]);  pars.put("Logo",     isLogo);
      pars.put("Rfc",        datosEmp[1]);  pars.put("Direccion",datosEmp[2]);
      pars.put("Id",         txtID.getText()); pars.put("Fecha",  fechaTicket);
      pars.put("Turno",      turno);        pars.put("Monto",    txtMonto.getText());
      pars.put("Montoletras",moneyAsWords); pars.put("Cajerouser",LoginController.usuarioLoggeado);
      pars.put("Cajeronom",  nomcajero);    pars.put("Hora",     horaticket);
      pars.put("Protesonom", protesonom);   pars.put("Titulo",   tituloRep);
      pars.put("Origen",     origenRep);    pars.put("Destino",  destinoRep);
      pars.put("Descripcion",descripcion);

      // Jasper sincrónico para cancelación (sin Task, igual que el original)
      InputStream isRepo = getClass().getResourceAsStream(reportPath);
      JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
      JasperPrint jpRepo  = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());
      JasperViewer viewer = new JasperViewer(jpRepo, false);
      viewer.setAlwaysOnTop(true); viewer.setSize(800, 600);
      viewer.setLocationRelativeTo(null); viewer.setTitle(titulo);
      viewer.setVisible(true);

    } catch (Exception e) {
      e.printStackTrace();
    }

    mostrarAlerta(Alert.AlertType.INFORMATION, "TRASLADO CANCELADO CON ÉXITO",
            "TRASLADO CANCELADO CON ÉXITO",
            "TRASLADO DE: " + txtSocio.getText().trim() + " CANCELADO CON ÉXITO");
    controller.traerOperaciones();
    cerrarVentana(btnReimprimir);
  }

  // Extracción: lógica de cancelación de operaciones normales separada
  private void cancelarOperacionNormal() {
    String cod = servicio.traerEmpresaConRS(txtEmpresa.getText().trim()).getCodigo();
    empresa = cod;

    double montooriginal = 0;
    String numcuenta = "";
    double csasignada = 0, psmut = 0, psngu = 0;

    switch (opcion) {
      case 1:
        montooriginal = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio)).getSaldo();
        break;
      case 4:
        ModelAhorro ah4 = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio));
        montooriginal = ah4.getSaldo();
        numcuenta = ah4.getNum_cuenta();
        break;
      case 5:
        List<ModelCapitalSocial> caps5 = servicio.traerCuentasCS(Integer.parseInt(socio));
        if (caps5.size() == 2) { psmut = caps5.get(0).getMonto_cubierto(); psngu = caps5.get(1).getMonto_cubierto(); }
        else { psngu = caps5.get(0).getMonto_cubierto(); }
        break;
      case 10:
        ModelPrevisionSocial ps = servicio.traerCuentaPS(Integer.parseInt(socio), empresa);
        montooriginal = ps.getPrevision();
        csasignada    = ps.getMontoAsignado();
        break;
    }


    String Result = "";
    ModelTransaccion cancelacionCredito = null;
    if(opcion == 2){
      cancelacionCredito = servicio.cancelarCredito(Integer.parseInt(txtID.getText()));

      if (cancelacionCredito == null) {
        mostrarAlerta(Alert.AlertType.ERROR, "ERROR CANCELANDO LA OPERACIÓN",
                "ERROR AL CANCELAR EL ABONO A CRÉDITO",
                "EXISTE UNA OPERACIÓN MÁS RECIENTE PARA ESTE CRÉDITO, CANCELE ESA PRIMERO.");
        return;
      }

      if(!cancelacionCredito.getStatus()) {
        Result = "CORRECTO";
      }

    } else {
      Result = servicio.pa_CancelarOperacion(opcion, usuario, Integer.parseInt(socio),
              Integer.parseInt(txtID.getText().trim()), montoExtraido, empresa, turno, "");
    }


    if (!Result.equals("CORRECTO")) {
      mostrarAlerta(Alert.AlertType.ERROR, "ERROR CANCELANDO LA OPERACIÓN",
              Result.toUpperCase(), "ERROR AL TRATAR DE CANCELAR");
      return;
    }

    mostrarAlerta(Alert.AlertType.INFORMATION, "OPERACIÓN CANCELADA CON ÉXITO",
            "OPERACIÓN CANCELADA CON ÉXITO",
            "ID DE OPERACIÓN: " + txtID.getText().trim() + " CANCELADA CON ÉXITO");

    // Preparar datos comunes de impresión
    LocalDate now     = servicio.traerFechaHoy();
    String horaFormateada = LocalTime.now().format(FORMATO_HORA);
    String fechaTicket    = now.format(FORMATO_FECHA);
    String[] datosEmp     = getDatosEmpresa(empresa);
    String moneyAsWords   = enPalabras(montoExtraido);
    String idoperacion    = txtID.getText();
    String montoTxt       = txtMonto.getText();
    PrintJob impresora    = new PrintJob();
    PrinterMatrix printer = null;
    InputStream isLogo    = null;

    // Montos originales formateados
    String montoorigenviar = formatoMoneda.format(montooriginal);

    switch (opcion) {
      case 1: {
        ModelAhorro ah = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio));
        String nc       = ah.getNum_cuenta();
        String nuevoFmt = formatoMoneda.format(ah.getSaldo());
        printer = impresora.imprimirCancelarAhorro(datosEmp[0], datosEmp[1], datosEmp[2],
                socio, idoperacion, nombre, nc, montoTxt, moneyAsWords,
                montoorigenviar, nuevoFmt, fechaTicket, horaFormateada);
        break;
      }
      case 2: {


        printer = impresora.imprimirCancelacionAbonoACredito(datosEmp[0], datosEmp[1], datosEmp[2],
                socio, cancelacionCredito.getId().toString(), fechaTicket, horaFormateada, nombre,
                LoginController.usuarioLoggeado, formatoMoneda.format(cancelacionCredito.getCapitalCreditoPagado()), enPalabras(cancelacionCredito.getSaldo().doubleValue()),
                formatoMoneda.format(cancelacionCredito.getSaldo().doubleValue()), cancelacionCredito.getTipoCredito(),
                formatoMoneda.format(cancelacionCredito.getMoraCreditoPagado()),
              formatoMoneda.format(cancelacionCredito.getInteresesCreditoPagado()),
                formatoMoneda.format(cancelacionCredito.getBonifCreditoPagado()),
               formatoMoneda.format(cancelacionCredito.getIvaCreditoPagado())
                ,formatoMoneda.format(cancelacionCredito.getSaldoCredito().doubleValue() + cancelacionCredito.getCapitalCreditoPagado().doubleValue()), formatoMoneda.format(cancelacionCredito.getInteresesCreditoPagado().doubleValue() + cancelacionCredito.getBonifCreditoPagado().doubleValue()));
        break;
      }
      case 3: {
        try {
          int colCredito = servicio.traerDatosDesembolsoCancelado(
                  Integer.parseInt(socio), 0, montoExtraido, "0002").getId();
          ModelCredito credito = servicio.traerDatosCredito(colCredito);
          isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          Map<String, Object> pars = new HashMap<>();
          pars.put("Empresa",     datosEmp[0]);  pars.put("LogoImg",    isLogo);
          pars.put("RFC",         datosEmp[1]);  pars.put("Direccion",  datosEmp[2]);
          pars.put("Numcredito",  String.valueOf(colCredito));
          pars.put("Titulo",      "REPORTE DE CANCELACIÓN DE DESEMBOLSO DE CRÉDITO");
          pars.put("Fecha",       fechaTicket);  pars.put("Folio",      id);
          pars.put("NumSocio",    String.valueOf(socio));
          pars.put("NombreSocio", txtNomSocio.getText());
          pars.put("ASESOR",      credito.getAsesor());
          pars.put("IntOrd",      credito.getTasa() + " %");
          pars.put("IntMora",     credito.getMora() + " %");
          pars.put("Vencimiento", String.valueOf(credito.getFv()));
          pars.put("Monto",       montoTxt);     pars.put("MontoLetras", moneyAsWords);
          pars.put("Cajero",      LoginController.usuarioLoggeado);
          pars.put("Hora",        horaFormateada);
          pars.put("Descripcion", "Devuelto a " + datosEmp[0] + " la cantidad de " + montoTxt
                  + " (" + moneyAsWords + ") recibido en efectivo a mi entera satisfacción. "
                  + "Así mismo, manifiesto conocer y apegarme al cumplimiento del acuerdo 2 "
                  + "de la Asamblea General efectuada el 22 de Julio de 2011, el cual menciona "
                  + "que todo socio que realice un crédito por sus ahorros o menos y que en seis "
                  + "meses consecutivos no realice abono alguno a su crédito, será dado de baja "
                  + "con el fin de evitar el incremento de su deuda y la cartera vencida.");

          // Jasper sincrónico para cancelación
          InputStream isRepo = getClass().getResourceAsStream("/Reports/cancelar_desembolso.jasper");
          JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint jpRepo  = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());
          JasperViewer viewer = new JasperViewer(jpRepo, false);
          viewer.setAlwaysOnTop(true); viewer.setSize(800, 600);
          viewer.setLocationRelativeTo(null);
          viewer.setTitle("REPORTE DE CANCELACIÓN DE DESEMBOLSO");
          viewer.setVisible(true);
        } catch (Exception e) { e.printStackTrace(); }
        break;
      }
      case 4: {
        try {
          isLogo = empresa.equals("0001")
                  ? getClass().getResourceAsStream("/assets/images/logo-mut.png")
                  : getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          ModelAhorro ah = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio));
          String nuevoFmt = formatoMoneda.format(ah.getSaldo());
          Map<String, Object> pars = new HashMap<>();
          pars.put("Empresa",       datosEmp[0]); pars.put("Logo",         isLogo);
          pars.put("Rfc",           datosEmp[1]); pars.put("Direccion",    datosEmp[2]);
          pars.put("Titulo",        "REPORTE DE CANCELACIÓN DE RETIRO DE AHORROS");
          pars.put("Montoanterior", montoorigenviar);
          pars.put("Fecha",         fechaTicket);  pars.put("Id",          id);
          pars.put("Numsocio",      String.valueOf(socio));
          pars.put("Nombresocio",   txtNomSocio.getText());
          pars.put("Numcuenta",     numcuenta);
          pars.put("Montoretirado", montoTxt);    pars.put("Montorestante",nuevoFmt);
          pars.put("Montoletras",   moneyAsWords);
          pars.put("Cajero",        LoginController.usuarioLoggeado);
          pars.put("Hora",          horaFormateada);
          pars.put("Descripcion",   "Devuelto a " + datosEmp[0] + " la cantidad de "
                  + montoTxt + " (" + moneyAsWords + ") por concepto de RETIRO DE CUENTA DE AHORRO.");

          InputStream isRepo = getClass().getResourceAsStream("/Reports/Cancelar_Retiro.jasper");
          JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint jpRepo  = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());
          JasperViewer viewer = new JasperViewer(jpRepo, false);
          viewer.setAlwaysOnTop(true); viewer.setSize(800, 600);
          viewer.setLocationRelativeTo(null);
          viewer.setTitle("REPORTE DE CANCELACIÓN DE RETIRO");
          viewer.setVisible(true);
        } catch (Exception e) { e.printStackTrace(); }
        break;
      }
      case 5: {
        List<ModelCapitalSocial> caps = servicio.traerCuentasCS(Integer.parseInt(socio));
        double montonuevongu, montonuevomut;
        String montoantiguomutFmt = formatoMoneda.format(psmut);
        String montoantiguonguFmt = formatoMoneda.format(psngu);
        if (caps.size() == 2) {
          montonuevomut = caps.get(0).getMonto_cubierto();
          montonuevongu = caps.get(1).getMonto_cubierto();
        } else {
          montonuevomut = 0;
          montonuevongu = caps.get(0).getMonto_cubierto();
        }
        printer = impresora.imprimirCancelacionCapSocial(datosEmp[0], datosEmp[1], datosEmp[2],
                socio, idoperacion, nombre, montoTxt, moneyAsWords, fechaTicket, horaFormateada,
                montoantiguomutFmt, montoantiguonguFmt,
                formatoMoneda.format(montonuevomut), formatoMoneda.format(montonuevongu));
        break;
      }
      case 10: {
        ModelPrevisionSocial ps2 = servicio.traerCuentaPS(Integer.parseInt(socio), empresa);
        printer = impresora.imprimirCancelacionPrevision(datosEmp[0], datosEmp[1], datosEmp[2],
                socio, idoperacion, nombre, montoTxt, moneyAsWords,
                formatoMoneda.format(montooriginal), formatoMoneda.format(csasignada),
                formatoMoneda.format(ps2.getPrevision()), formatoMoneda.format(ps2.getMontoAsignado()),
                fechaTicket, horaFormateada);
        break;
      }
    }

    if (opcion == 1 || opcion == 2 || opcion == 5 || opcion == 10) {
      printer.toFile("Cancelacion.txt");
      imprimirArchivo("Cancelacion.txt");
    }

    controller.traerOperaciones();
    cerrarVentana(btnReimprimir);
  }

  // ─── Reimpresión ─────────────────────────────────────────────────────────

  @FXML
  public void filtrarOpcion() {
    if (opcion != 0) {
      cancelarOperacion();
      return;
    }

    parsearMontoExtraido();
    String empresacod  = servicio.traerEmpresaConRS(empresa).getCodigo();
    String[] datosEmp  = getDatosEmpresa(empresacod);
    PrintJob impresora = new PrintJob();
    MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
    PrinterMatrix printer = null;
    InputStream isLogo = null;
    double abono = 0;

    switch (operaciontipo) {
      case 1: {
        abono = parseMoneda(monto);
        ModelAhorro ah = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio));
        printer = impresora.imprimirAhorro(datosEmp[0], datosEmp[1], datosEmp[2],
                socio, id, nombre, ah.getNum_cuenta(), monto,
                enPalabras(abono), fecha, hora, formatoMoneda.format(ah.getSaldo()));
        break;
      }
      case 2: {
//        abono = parseMoneda(monto);
//        double bonifrestar = parseMoneda(bonif_aplicada);
//        double interes     = parseMoneda(txtInteres.getText()) + bonifrestar;
//        List<ModelCapitalSocial> cs2 = servicio.traerCuentasCS(Integer.parseInt(socio));
//        double psmut2 = cs2.getFirst().getMonto_cubierto();
//        double psngu2 = cs2.size() > 1 ? cs2.get(1).getMonto_cubierto() : 0;
//        printer = impresora.imprimirAbonoACredito(datosEmp[0], datosEmp[1], datosEmp[2],
//                socio, id, nombre, fecha, hora, LoginController.usuarioLoggeado,
//                capital_pagado, enPalabras(abono), monto, tipo_credito, mora_pagada,
//                interes_pagado, bonif_aplicada, iva_pagado,
//                formatoMoneda.format(psngu2), formatoMoneda.format(psmut2),
//                saldo_credito, formatoMoneda.format(interes));
        break;
      }
      case 3: {
        try {
          int cuentaCredito = servicio.traerDatosDesembolsoCancelado(
                  Integer.parseInt(socio), 2, montoExtraido, "0002").getId();
          ModelCredito credito = servicio.traerDatosCredito(cuentaCredito);
          isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          String letras = enPalabras(montoExtraido);
          String nomcajero = servicio.traerCajeroPorUsuario(LoginController.usuarioLoggeado);
          Map<String, Object> pars = new HashMap<>();
          pars.put("Empresa",     datosEmp[0]); pars.put("Logo",       isLogo);
          pars.put("RFC",         datosEmp[1]); pars.put("Direccion",  datosEmp[2]);
          pars.put("Numcredito",  Integer.toString(cuentaCredito));
          pars.put("Titulo",      "REPORTE DE DESEMBOLSO DE CRÉDITO");
          pars.put("Fecha",       txtFecha.getText()); pars.put("Folio",    txtID.getText());
          pars.put("NumSocio",    txtSocio.getText());
          pars.put("NombreSocio", txtNomSocio.getText());
          pars.put("ASESOR",      credito.getAsesor());
          pars.put("IntOrd",      credito.getTasa() + " %");
          pars.put("IntMora",     credito.getMora() + " %");
          pars.put("Vencimiento", String.valueOf(credito.getFv()));
          pars.put("Monto",       txtMonto.getText()); pars.put("MontoLetras", letras);
          pars.put("Cajero",      LoginController.usuarioLoggeado);
          pars.put("LogoImg",     isLogo);             pars.put("Hora",    hora);
          pars.put("Descripcion", "Recibí de " + datosEmp[0] + " la cantidad de "
                  + txtMonto.getText() + " (" + letras + ") recibido en efectivo a mi entera satisfacción. "
                  + "Así mismo, manifiesto conocer y apegarme al cumplimiento del acuerdo 2 de la Asamblea "
                  + "General efectuada el 22 de Julio de 2011, el cual menciona que todo socio que realice un "
                  + "crédito por sus ahorros o menos y que en seis meses consecutivos no realice abono alguno "
                  + "a su crédito, será dado de baja con el fin de evitar el incremento de su deuda y la cartera vencida.");
          lanzarReporteJasper("/Reports/desembolso.jasper", pars, "REPORTE DE DESEMBOLSO");
        } catch (Exception e) { e.printStackTrace(); }
        break;
      }
      case 4: {
        try {
          ModelAhorro ah = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio));
          isLogo = empresa.equals("0001")
                  ? getClass().getResourceAsStream("/assets/images/logo-mut.png")
                  : getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          String letras       = enPalabras(montoExtraido);
          String montoanterior = formatoMoneda.format(ah.getSaldo() + montoExtraido);
          Map<String, Object> pars = new HashMap<>();
          pars.put("Empresa",      datosEmp[0]); pars.put("Logo",         isLogo);
          pars.put("Rfc",          datosEmp[1]); pars.put("Direccion",    datosEmp[2]);
          pars.put("Titulo",       "REPORTE DE RETIRO DE AHORROS");
          pars.put("Montoanterior",montoanterior);
          pars.put("Fecha",        txtFecha.getText()); pars.put("Id",     txtID.getText());
          pars.put("Numsocio",     String.valueOf(socio));
          pars.put("Nombresocio",  txtNomSocio.getText());
          pars.put("Numcuenta",    ah.getNum_cuenta());
          pars.put("Montoretirado",txtMonto.getText());
          pars.put("Montorestante",formatoMoneda.format(ah.getSaldo()));
          pars.put("Montoletras",  letras);
          pars.put("Cajero",       LoginController.usuarioLoggeado);
          pars.put("Hora",         hora);
          pars.put("Descripcion",  "Recibí de " + (empresa.equals("0001") ? "la " : "")
                  + datosEmp[0] + " la cantidad de " + txtMonto.getText()
                  + " (" + letras + ") por concepto de RETIRO DE CUENTA DE AHORRO.");
          lanzarReporteJasper("/Reports/retiro.jasper", pars, "REPORTE DE RETIRO");
        } catch (Exception e) { e.printStackTrace(); }
        break;
      }
      case 5: {
        abono = parseMoneda(monto);
        List<ModelCapitalSocial> cuentas = servicio.traerCuentasCS(Integer.parseInt(socio));
        double psmut5 = 0, psngu5 = 0;
        if (cuentas.size() == 2) {
          psmut5 = cuentas.get(0).getMonto_cubierto();
          psngu5 = cuentas.get(1).getMonto_cubierto();
        } else if (cuentas.get(0).getEmpresa_cod().equals("0001")) {
          psmut5 = cuentas.get(0).getMonto_cubierto();
        } else {
          psngu5 = cuentas.get(0).getMonto_cubierto();
        }
        printer = impresora.imprimirCapSocial(datosEmp[0], datosEmp[1], datosEmp[2],
                socio, id, nombre, monto, enPalabras(abono), fecha, hora,
                formatoMoneda.format(psmut5), formatoMoneda.format(psngu5));
        break;
      }
      case 6:
      case 7: {
        try {
          boolean esOp6 = (operaciontipo == 6);
          String protesonom = empresacod.equals("0001")
                  ? "CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL"
                  : "CASIMIRO UITZ VILLANUEVA";
          String desc = empresacod.equals("0001")
                  ? "NOMBRE Y FIRMA DE LA PROTESORERA"
                  : "NOMBRE Y FIRMA DEL PROTESORERO";
          isLogo = empresacod.equals("0001")
                  ? getClass().getResourceAsStream("/assets/images/logo-mut.png")
                  : getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");

          String letras    = enPalabras(montoExtraido);
          String nomcajero = servicio.traerCajeroPorUsuario(LoginController.usuarioLoggeado);
          String tituloRep = esOp6 ? "REPORTE DE TRASLADO DE APERTURA" : "REPORTE DE TRASLADO DE CIERRE";
          String reportPath = esOp6 ? "/Reports/traslado_apertura.jasper" : "/Reports/traslado_cierre.jasper";
          String logoKey   = esOp6 ? "Logoempresa" : "Logo";
          String origenRep = esOp6 ? cuenta_origen : "CUENTA DE CAJERO " + LoginController.usuarioLoggeado;
          String destinoRep = esOp6 ? "CUENTA DE CAJERO " + LoginController.usuarioLoggeado : cuenta_destino;

          Map<String, Object> pars = new HashMap<>();
          pars.put("Empresa",     datosEmp[0]); pars.put(logoKey,      isLogo);
          pars.put("Rfc",         datosEmp[1]); pars.put("Direccion",  datosEmp[2]);
          pars.put("Titulo",      tituloRep);
          pars.put("Id",          txtID.getText()); pars.put("Fecha",  esOp6 ? txtFecha.getText() : fecha);
          pars.put("Turno",       turno);
          pars.put("Origen",      origenRep);   pars.put("Destino",    destinoRep);
          pars.put("Monto",       txtMonto.getText()); pars.put("Montoletras", letras);
          pars.put("Cajerouser",  LoginController.usuarioLoggeado);
          pars.put("Cajeronom",   nomcajero);   pars.put("Hora",       hora);
          pars.put("Protesonom",  protesonom);  pars.put("DescProteso",desc);
          String accion = esOp6 ? "TRASLADO DE APERTURA" : "TRASLADO DE CIERRE";
          pars.put("Descripcion", accion + " realizado por " + txtMonto.getText()
                  + " (" + letras + ") efectuado en la sucursal de UMAN por "
                  + nomcajero + " y autorizado por " + protesonom);
          lanzarReporteJasper(reportPath, pars, "REPORTE DE TRASLADO");
        } catch (Exception e) { e.printStackTrace(); }
        break;
      }
      case 10: {
        ModelPrevisionSocial ps = servicio.traerCuentaPS(Integer.parseInt(socio), empresacod);
        printer = impresora.imprimirPrevision(datosEmp[0], datosEmp[1], datosEmp[2],
                socio, id, nombre, monto, enPalabras(abono), fecha, hora,
                formatoMoneda.format(ps.getMontoAsignado()),
                formatoMoneda.format(ps.getPrevision()));
        break;
      }
    }

    if (operaciontipo == 1 || operaciontipo == 2 || operaciontipo == 5 || operaciontipo == 10) {
      printer.toFile("Reimpresion.txt");
      imprimirArchivo("Reimpresion.txt");
    }

    mostrarAlerta(Alert.AlertType.INFORMATION, "REIMPRESIÓN REALIZADA CON ÉXITO",
            "REIMPRESIÓN REALIZADA CON ÉXITO", "REIMPRESIÓN REALIZADA CON ÉXITO");
    cerrarVentana(txtMonto);
  }

  private double parseMoneda(String moneda) {
    try {
      return formatoMoneda.parse(moneda).doubleValue();
    } catch (ParseException e) {
      e.printStackTrace();
      return 0;
    }
  }
}