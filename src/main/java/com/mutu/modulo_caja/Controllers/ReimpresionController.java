package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Models.*;
import com.mutu.modulo_caja.Services.Servicio;
import com.mutu.modulo_caja.utils.PrintJob;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ReimpresionController {

  @FXML
  private TextField txtSocio, txtID, txtOperacion, txtEmpresa, txtFecha, txtMonto, txtNomSocio;
  @FXML private Label lblTitulo, lblEmpresa, lblSocio, lblOperacion, lblNombre;
  @FXML private Button btnReimprimir, btnRegresar;

  public OperacionesController controller;

  public String nombre,
      usuario,
      socio,
      id,
      operacion,
      empresa,
      fecha,
      monto,
      turno,
      tipo_traslado,
      cuenta_origen,
      cuenta_destino,
      hora;
  public int opcion = 0;
  public int operaciontipo = 0;
  public double montoExtraido = 0;
  public String empresaEnviar = "";

  @Autowired public Servicio servicio;

  public void setDatosTraslados(
      String id,
      String usuario,
      String monto,
      String tipo_traslado,
      String cuenta_origen,
      String cuenta_destino,
      String fecha,
      int opcion,
      OperacionesController controller, String turno) {
    this.id = id;
    this.opcion = opcion;
    this.usuario = usuario;
    this.monto = monto;
    this.controller = controller;
    this.tipo_traslado = tipo_traslado;
    this.cuenta_origen = cuenta_origen;
    this.cuenta_destino = cuenta_destino;
    this.fecha = fecha;
    this.turno=turno;
    txtSocio.setText(usuario);
    txtID.setText(id);
    if (opcion == 6) {
      txtNomSocio.setText(cuenta_origen);
      txtOperacion.setText("CUENTA DE CAJA: " + cuenta_destino);
      lblNombre.setText("ORIGEN:");
      lblOperacion.setText("DESTINO:");
    } else {
      txtNomSocio.setText("CUENTA DE CAJA: " + cuenta_origen);
      txtOperacion.setText(cuenta_destino);

      lblNombre.setText("DESTINO:");
      lblOperacion.setText("ORIGEN:");
    }
    txtEmpresa.setText(tipo_traslado);
    txtFecha.setText(fecha);
    txtMonto.setText(monto);
    lblSocio.setText("USUARIO:");
    lblEmpresa.setText("TIPO:");
    lblTitulo.setText("CANCELAR TRASLADO");
    btnReimprimir.setText("CANCELAR");
  }

  public void setDatos(
      String socio,
      String id,
      String operacion,
      String empresa,
      String fecha,
      String monto,
      String nombre,
      int operaciontipo,
      String hora) {
    this.nombre = nombre;
    this.socio = socio;
    this.id = id;
    this.operacion = operacion;
    this.empresa = empresa;
    this.fecha = fecha;
    this.monto = monto;
    this.hora = hora;
    this.operaciontipo = operaciontipo;
    txtSocio.setText(socio);
    txtID.setText(id);
    txtOperacion.setText(operacion);
    txtEmpresa.setText(empresa);
    txtFecha.setText(fecha);
    txtMonto.setText(monto);
    txtNomSocio.setText(nombre);
  }

  @FXML
  public void cerrar() {
    Stage ventanaActual = (Stage) txtSocio.getScene().getWindow();
    ventanaActual.close();
  }

  public void setDatosCancelacion(
      String socio,
      String id,
      String operacion,
      String empresa,
      String fecha,
      String monto,
      String nombre,
      int opcion,
      String turno,
      String usuario,
      OperacionesController controller) {
    this.nombre = nombre;
    this.socio = socio;
    this.usuario = usuario;
    this.id = id;
    this.operacion = operacion;
    this.empresa = empresa;
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
  }

  public void cancelarOperacion() {
    boolean validador = false;
    InputStream isLogo = null;
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CANCELACIÓN");
    if (opcion == 6 || opcion == 7) {
      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CANCELAR ESTE TRASLADO?");
    } else {
      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CANCELAR ESTA OPERACIÓN?");
    }
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      validador = true;
    }

    if (validador) {
      int id_traslado = 0;
      NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
      try {
        Number numero = formatoMoneda.parse(txtMonto.getText().trim());
        montoExtraido = numero.doubleValue();
      } catch (ParseException e) {
        e.printStackTrace();
      }

      if (opcion == 6 || opcion == 7) {
        id_traslado = Integer.parseInt(txtID.getText().trim());
        String protesonom = "";

        if (opcion == 6) {
          if (cuenta_origen.equals("BVDA-NGU")) {
            empresaEnviar = "0002";
            protesonom ="CASIMIRO UITZ VILLANUEVA";
            isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          } else {
            empresaEnviar = "0001";
            isLogo = getClass().getResourceAsStream("/assets/images/logo-mut.png");
            protesonom ="CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL";
          }
        } else {
          if (cuenta_destino.equals("BVDA-NGU")) {
            empresaEnviar = "0002";
            isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
            protesonom ="CASIMIRO UITZ VILLANUEVA";
          } else {
            empresaEnviar = "0001";
            isLogo = getClass().getResourceAsStream("/assets/images/logo-mut.png");
            protesonom ="CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL";
          }
        }
        String res =
            servicio.pa_CancelarTraslados(
                opcion,
                id_traslado,
                cuenta_origen,
                cuenta_destino,
                montoExtraido,
                empresaEnviar,
                "");

        if (res.equals("CORRECTO")) {
          try {

            LocalDateTime fecha = LocalDateTime.now();
            LocalTime hora = fecha.toLocalTime();
            DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaTicket = fecha.format(formatter);
            String horaticket = hora.format(formatterHora);

            String id = txtID.getText();
            String nomcajero = servicio.traerCajeroPorUsuario(usuario);
            int usuarioid = servicio.traerDatosUsuario(usuario).getId();

            String horaFormateada = hora.format(formatterHora);
            String nombreEmpresa = servicio.traerEmpresa(empresaEnviar).getRazonSocial();
            String rfcEmpresa = servicio.traerEmpresa(empresaEnviar).getRfc();
            String direcEmpresa =
                    servicio.traerEmpresa(empresaEnviar).getCalle()
                            + " "
                            + servicio.traerEmpresa(empresaEnviar).getCruzamiento()
                            + " COL. CENTRO";


            MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
            String moneyAsWords = converter.asWords(BigDecimal.valueOf(montoExtraido)).toUpperCase() + " MXN";



            Map pars = new HashMap<>();
            pars.put("Empresa", nombreEmpresa);
            pars.put("Logo", isLogo);
            pars.put("Rfc", rfcEmpresa);
            pars.put("Direccion", direcEmpresa);
            pars.put("Id", id);
            pars.put("Fecha", fechaTicket);
            pars.put("Turno", turno);


            pars.put("Monto", txtMonto.getText());
            pars.put("Montoletras", moneyAsWords);



            pars.put("Cajerouser",LoginController.usuarioLoggeado);
            pars.put("Cajeronom", nomcajero);
            pars.put("Hora", horaFormateada);

            pars.put("Protesonom", protesonom);
            InputStream isRepo = null;
            if(cuenta_origen.equals("BVDA-NGU")  || cuenta_origen.equals("BVDA-MUT")){

              pars.put("Titulo", "REPORTE DE CANCELACION DE TRASLADO DE APERTURA");

              pars.put("Origen", "CUENTA DE CAJERO " +  cuenta_destino);
              pars.put("Destino", cuenta_origen) ;
              pars.put(
                      "Descripcion",
                      "TRASLADO DE APERTURA cancelado por "+ txtMonto.getText()+ " (" +moneyAsWords +
                              ") efectuado en la sucursal de " +
                              "UMAN autorizado por " + protesonom+ ".");
              isRepo = getClass().getResourceAsStream("/Reports/cancelacion_traslado_apertura.jasper");

            }else{
              pars.put("Titulo", "REPORTE DE CANCELACION DE TRASLADO DE CIERRE");

              pars.put("Origen",  cuenta_destino );
              pars.put("Destino", "CUENTA DE CAJERO " + cuenta_origen) ;
              pars.put(
                      "Descripcion",
                      "TRASLADO DE CIERRE cancelado por "+ txtMonto.getText()+ " (" +moneyAsWords +
                              ") efectuado en la sucursal de " +
                              "UMAN autorizado por " + protesonom+ ".");
              isRepo = getClass().getResourceAsStream("/Reports/cancelar_traslado_cierre.jasper");


            }


            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(jpRepo, false);

            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("REPORTE DE CANCELACION DE TRASLADO");
            viewer.setVisible(true);

          } catch (Exception e) {
            e.printStackTrace();
          }

          Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
          alert2.setTitle("TRASLADO CANCELADO CON ÉXITO");
          alert2.setHeaderText("TRASLADO CANCELADO CON ÉXITO");
          alert2.setContentText(
              "TRASLADO DE: " + txtSocio.getText().trim() + " CANCELADO CON ÉXITO");
          alert2.showAndWait();
          Stage ventanaActual = (Stage) btnReimprimir.getScene().getWindow();
          controller.traerOperaciones();
          ventanaActual.close();
        } else {
          Alert alert2 = new Alert(Alert.AlertType.ERROR);
          alert2.setTitle("ERROR CANCELANDO EL TRASLADO");
          alert2.setHeaderText("ERROR CANCELANDO EL TRASLADO");
          alert2.setContentText("ERROR AL TRATAR DE CANCELAR");
          alert2.showAndWait();
        }
      } else {

        if (empresa.equals("MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.")) {
          empresa = "0001";
        } else {
          empresa = "0002";
        }
        double montooriginal = 0;
        double csasignada=0;
        double psmut = 0;
        double psngu = 0;

        switch (opcion){
          case 1:
            montooriginal= servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio)).getSaldo();
            break;
          case 5:
            List<ModelCapitalSocial> caps = servicio.traerCuentasCS(Integer.parseInt(socio));
            if(caps.size() == 2){
              psmut = caps.get(0).getMonto_cubierto();
              psngu=caps.get(1).getMonto_cubierto();
            }else{
              psngu=caps.get(0).getMonto_cubierto();
            }
            break;
          case 10:
            montooriginal=servicio.traerCuentaPS(Integer.parseInt(socio),empresa).getPrevision();
            csasignada=servicio.traerCuentaPS(Integer.parseInt(socio),empresa).getMontoAsignado();
            break;

        }
        String Result =
            servicio.pa_CancelarOperacion(
                opcion,
                usuario,
                Integer.parseInt(socio),
                Integer.parseInt(txtID.getText().trim()),
                montoExtraido,
                empresa,
                turno,
                "");

        if (Result.equals("CORRECTO")) {

          String idoperacion = txtID.getText();
          PrinterMatrix printer = null;
          String nombreEmpresa = servicio.traerEmpresa(empresa).getRazonSocial();
          String rfcEmpresa = servicio.traerEmpresa(empresa).getRfc();
          String direcEmpresa =
                  servicio.traerEmpresa(empresa).getCalle()
                          + " "
                          + servicio.traerEmpresa(empresa).getCruzamiento()
                          + " COL. CENTRO";
          MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
          alert2.setTitle("OPERACIÓN CANCELADA CON ÉXITO");
          alert2.setHeaderText("OPERACIÓN CANCELADA CON ÉXITO");
          alert2.setContentText(
                  "ID DE OPERACIÓN: " + txtID.getText().trim() + " CANCELADA CON ÉXITO");
          alert2.showAndWait();
          PrintJob impresora = new PrintJob();
          LocalDateTime fecha = LocalDateTime.now();
          LocalTime hora = fecha.toLocalTime();
          DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
          String horaFormateada = hora.format(formatterHora);
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
          String fechaTicket = fecha.format(formatter);
          String monto = txtMonto.getText();
          String moneyAsWords = converter.asWords(BigDecimal.valueOf(montoExtraido)).toUpperCase() + " MXN";
          String id = txtID.getText();
          String nomcajero = servicio.traerCajeroPorUsuario(usuario);
          int usuarioid = servicio.traerDatosUsuario(usuario).getId();

          switch (opcion){
            case 1:
              double montonuevo = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio)).getSaldo();
              String montonuevoenviar = formatoMoneda.format(montonuevo);
              String montoorigenviar = formatoMoneda.format(montooriginal);
              String numcuenta = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio)).getNum_cuenta();
              printer = impresora.imprimirCancelarAhorro(nombreEmpresa, rfcEmpresa,direcEmpresa,socio,idoperacion, nombre,numcuenta,
                      monto, moneyAsWords, montoorigenviar,montonuevoenviar,fechaTicket,horaFormateada);
              break;
            case 3:
//              try {
//
//                String folio = txtID.getText();
//                String nomsocio = txtNomSocio.getText();
//                LocalDateTime fechaVenc = fecha.plusYears(1);
//                String fechaVencticket = fechaVenc.format(formatter);
//                isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
//                //ModelCredito credito = servicio.traerDatosCredito(Integer.parseInt(colCredito));
//                String ord = String.valueOf(credito.getOrdinarios()) + " %";
//                String mor = String.valueOf(credito.getMoratorios()) + " %";
//                String fechvenc = String.valueOf(credito.getFecha_venci());
//                isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
//                Map pars = new HashMap<>();
//                pars.put("Empresa", nombreEmpresa);
//                pars.put("Logo", isLogo);
//                pars.put("RFC", rfcEmpresa);
//                pars.put("Direccion", direcEmpresa);
//                pars.put("Numcredito", colCredito);
//                pars.put("Titulo", "REPORTE DE DESEMBOLSO DE CRÉDITO");
//                pars.put("Fecha", fechaTicket);
//                pars.put("Folio", folio);
//                pars.put("NumSocio", String.valueOf(socio));
//                pars.put("NombreSocio", nomsocio);
//
//                pars.put("IntOrd", ord);
//                pars.put("IntMora", mor);
//                pars.put("Vencimiento", fechaVencticket);
//
//                pars.put("Monto", montoenviar);
//                pars.put("MontoLetras", moneyAsWords);
//                pars.put("Cajero", LoginController.usuarioLoggeado);
//                pars.put("Hora", horaFormateada);
//                pars.put("LogoImg", isLogo);
//                pars.put(
//                        "Descripcion",
//                        "Recibí de " + nombreEmpresa + " la cantidad de "+ montoenviar +
//                                " ("+moneyAsWords +
//                                ") recibido en efectivo a mi entera satisfacción. Así mismo, manifiesto conocer y apegarme al " +
//                                "cumplimiento del acuerdo 2 de la Asamblea General efectuada el 22 de Julio de 2011," +
//                                " el cual menciona que todo socio que realice un crédito por sus ahorros o menos y que " +
//                                "en seis meses consecutivos no realice abono alguno a su crédito, será dado de baja con el fin " +
//                                "de evitar el incremento de su deuda y la cartera vencida.");
//
//                InputStream isRepo = getClass().getResourceAsStream("/Reports/desembolso.jasper");
//                JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
//                JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());
//
//                JasperViewer viewer = new JasperViewer(jpRepo, false);
//
//                viewer.setAlwaysOnTop(true);
//                viewer.setSize(800, 600);
//                viewer.setLocationRelativeTo(null);
//                viewer.setTitle("REPORTE DE DESEMBOLSO");
//                viewer.setVisible(true);
//
//              } catch (Exception e) {
//                e.printStackTrace();
//              }
            break;
            case 5:
              List<ModelCapitalSocial> caps = servicio.traerCuentasCS(Integer.parseInt(socio));
              double montonuevongu=0;
              double montonuevomut=0;
              String montonuevonguenviar = "";
              String montonuevomutgenviar = "";

              if(caps.size() == 2){
                      montonuevongu = caps.get(1).getMonto_cubierto();
                      montonuevomut=caps.get(0).getMonto_cubierto();
                String montoantiguonguenviar = formatoMoneda.format(psngu);
                String montoantiguomutgenviar = formatoMoneda.format(psmut);
                montonuevonguenviar = formatoMoneda.format(montonuevongu);
                      montonuevomutgenviar = formatoMoneda.format(montonuevomut);
                printer = impresora.imprimirCancelacionCapSocial(nombreEmpresa,rfcEmpresa,direcEmpresa,socio,
                        idoperacion,nombre,monto,moneyAsWords,fechaTicket,horaFormateada,montoantiguomutgenviar,
                        montonuevonguenviar,montonuevomutgenviar,montonuevonguenviar);
                    }else{
                montonuevongu = caps.get(0).getMonto_cubierto();
                String montoantiguonguenviar = formatoMoneda.format(psngu);
                String montoantiguomutgenviar = formatoMoneda.format(0);
                montonuevonguenviar = formatoMoneda.format(montonuevonguenviar);
                montonuevomutgenviar = formatoMoneda.format(0);
                printer = impresora.imprimirCancelacionCapSocial(nombreEmpresa,rfcEmpresa,direcEmpresa,socio,
                        idoperacion,nombre,monto,moneyAsWords,fechaTicket,horaFormateada,montoantiguomutgenviar,
                        montonuevonguenviar,montonuevomutgenviar,montonuevonguenviar);
                    }


              break;
            case 10:

              double montocubierto=servicio.traerCuentaPS(Integer.parseInt(socio),empresa).getPrevision();
              double csasignadanuevo=servicio.traerCuentaPS(Integer.parseInt(socio),empresa).getMontoAsignado();
              String montocap = txtMonto.getText();
              String montocubiertoantiguo = formatoMoneda.format(montooriginal);
              String montocubiertnuevo = formatoMoneda.format(montocubierto);
              String csanuevo = formatoMoneda.format(csasignadanuevo);
              String csocialantiguo = formatoMoneda.format(csasignada);
              printer=impresora.imprimirCancelacionPrevision(nombreEmpresa,rfcEmpresa,direcEmpresa,socio,idoperacion,nombre,
                      montocap,moneyAsWords,montocubiertoantiguo,csocialantiguo,montocubiertnuevo,csanuevo,fechaTicket,horaFormateada);

          }
          printer.toFile("Cancelacion.txt");

          InputStream inputStream = null;
          try {
            inputStream = new FileInputStream("Cancelacion.txt");
          } catch (FileNotFoundException a) {
            a.printStackTrace();
          }

          if (inputStream == null) {
            return;
          }

          DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
          Doc document = new SimpleDoc(inputStream, docFormat, null);
          PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
          PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
          if (defaultPrintService != null) {
            DocPrintJob printJob = defaultPrintService.createPrintJob();

            try {
              printJob.print(document, attributeSet);
            } catch (PrintException b) {
              Alert alert3 = new Alert(Alert.AlertType.ERROR);
              alert3.setTitle("ERROR IMPRIMIENDO");
              alert3.setHeaderText("ERROR IMPRIMIENDO");
              alert3.setContentText(b.getMessage());
              alert3.showAndWait();
            }
          } else {

            Alert alert5 = new Alert(Alert.AlertType.ERROR);
            alert5.setTitle("ERROR IMPRIMIENDO");
            alert5.setHeaderText("ERROR IMPRIMIENDO");
            alert5.showAndWait();
          }

          Stage ventanaActual = (Stage) btnReimprimir.getScene().getWindow();
          ventanaActual.close();
          controller.traerOperaciones();

        } else {
          Alert alert2 = new Alert(Alert.AlertType.ERROR);
          alert2.setTitle("ERROR CANCELANDO LA OPERACIÓN");
          alert2.setHeaderText("ERROR CANCELANDO EL TRASLADO");
          alert2.setContentText("ERROR AL TRATAR DE CANCELAR");
          alert2.showAndWait();
        }
      }
    }
  }

  @FXML
  public void filtrarOpcion() {
    if (opcion != 0) {
      cancelarOperacion();
    } else {
      String empresacod =
          empresa.equals("MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.") ? "0001" : "0002";
      String nombreEmpresa = servicio.traerEmpresa(empresacod).getRazonSocial();
      String rfcEmpresa = servicio.traerEmpresa(empresacod).getRfc();
      String direcEmpresa =
          servicio.traerEmpresa(empresacod).getCalle()
              + " "
              + servicio.traerEmpresa(empresacod).getCruzamiento()
              + " COL. CENTRO";

      PrintJob impresora = new PrintJob();
      double abono = 0;
      NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
      PrinterMatrix printer = null;
      MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
      switch (operaciontipo) {
        case 1:
          try {
            Number numero = formatoMoneda.parse(monto);
            abono = numero.doubleValue();
          } catch (ParseException e) {
            e.printStackTrace();
          }
          String moneyAsWords = converter.asWords(BigDecimal.valueOf(abono)).toUpperCase() + " MXN";
          String cuenta =
              servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio)).getNum_cuenta();
          double ahorro = servicio.traerCuentaAhorroPorSocio(Integer.parseInt(socio)).getSaldo();
          String totalhorro = formatoMoneda.format(ahorro);
          printer =
              impresora.imprimirAhorro(
                  nombreEmpresa,
                  rfcEmpresa,
                  direcEmpresa,
                  socio,
                  id,
                  nombre,
                  cuenta,
                  monto,
                  moneyAsWords,
                  fecha,
                  hora,
                  totalhorro);
          break;
        case 5:
          try {
            Number numero = formatoMoneda.parse(monto);
            abono = numero.doubleValue();
          } catch (ParseException e) {
            e.printStackTrace();
          }
          String abonoletras = converter.asWords(BigDecimal.valueOf(abono)).toUpperCase() + " MXN";
          double psmut = 0;
          double psngu = 0;
          List<ModelCapitalSocial> cuentas = servicio.traerCuentasCS(Integer.parseInt(socio));
          if (cuentas.size() == 2) {
            psmut = cuentas.get(0).getMonto_cubierto();
            psngu = cuentas.get(1).getMonto_cubierto();
          } else {
            if (cuentas.get(0).getEmpresa_cod().equals("0001")) {
              psmut = cuentas.get(0).getMonto_cubierto();
            } else {
              psngu = cuentas.get(0).getMonto_cubierto();
            }
          }
          String psmutEnviar = formatoMoneda.format(psmut);
          String psnguEnviar = formatoMoneda.format(psngu);
          printer =
              impresora.imprimirCapSocial(
                  nombreEmpresa,
                  rfcEmpresa,
                  direcEmpresa,
                  socio,
                  id,
                  nombre,
                  monto,
                  abonoletras,
                  fecha,
                  hora,
                  psmutEnviar,
                  psnguEnviar);
          break;

        case 10:

          ModelPrevisionSocial prevsocial = servicio.traerCuentaPS(Integer.parseInt(socio),empresacod);
          String montoletras = converter.asWords(BigDecimal.valueOf(abono)).toUpperCase() + " MXN";
          double montoAsignado = prevsocial.getMontoAsignado();
          double montoCubierto = prevsocial.getPrevision();

          String montoAsigEnviar = formatoMoneda.format(montoAsignado);
          String montoCubEnviar = formatoMoneda.format(montoCubierto);
          printer = impresora.imprimirPrevision(nombreEmpresa,rfcEmpresa,direcEmpresa,socio,id,nombre,monto,
                montoletras,fecha,hora,montoAsigEnviar,montoCubEnviar);
          break;
      }

      printer.toFile("Reimpresion.txt");

      InputStream inputStream = null;
      try {
        inputStream = new FileInputStream("Reimpresion.txt");
      } catch (FileNotFoundException a) {
        a.printStackTrace();
      }

      if (inputStream == null) {
        return;
      }

      DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
      Doc document = new SimpleDoc(inputStream, docFormat, null);
      PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
      PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
      if (defaultPrintService != null) {
        DocPrintJob printJob = defaultPrintService.createPrintJob();

        try {
          printJob.print(document, attributeSet);
        } catch (PrintException b) {
          Alert alert2 = new Alert(Alert.AlertType.ERROR);
          alert2.setTitle("ERROR IMPRIMIENDO");
          alert2.setHeaderText("ERROR IMPRIMIENDO");
          alert2.setContentText(b.getMessage());
          alert2.showAndWait();
        }
      } else {

        Alert alert2 = new Alert(Alert.AlertType.ERROR);
        alert2.setTitle("ERROR IMPRIMIENDO");
        alert2.setHeaderText("ERROR IMPRIMIENDO");
        alert2.showAndWait();
      }
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("REIMPRESIÓN REALIZADA CON ÉXITO");
      alert.setHeaderText("REIMPRESIÓN REALIZADA CON ÉXITO");
      alert.setContentText("REIMPRESIÓN REALIZADA CON ÉXITO");
      alert.showAndWait();
      Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
      ventanaActual.close();
    }

  }
}
