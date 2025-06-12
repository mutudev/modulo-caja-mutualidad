package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Models.ModelCapitalSocial;
import com.mutu.modulo_caja.Models.ModelPrevisionSocial;
import com.mutu.modulo_caja.Services.Servicio;
import com.mutu.modulo_caja.utils.PrintJob;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
      OperacionesController controller) {
    this.id = id;
    this.opcion = opcion;
    this.usuario = usuario;
    this.monto = monto;
    this.controller = controller;
    this.tipo_traslado = tipo_traslado;
    this.cuenta_origen = cuenta_origen;
    this.cuenta_destino = cuenta_destino;
    this.fecha = fecha;
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
        if (opcion == 6) {
          if (cuenta_origen.equals("BVDA-NGU")) {
            empresaEnviar = "0002";
          } else {
            empresaEnviar = "0001";
          }
        } else {
          if (cuenta_destino.equals("BVDA-NGU")) {
            empresaEnviar = "0002";
          } else {
            empresaEnviar = "0001";
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
          Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
          alert2.setTitle("OPERACIÓN CANCELADA CON ÉXITO");
          alert2.setHeaderText("OPERACIÓN CANCELADA CON ÉXITO");
          alert2.setContentText(
              "ID DE OPERACIÓN: " + txtID.getText().trim() + " CANCELADA CON ÉXITO");
          alert2.showAndWait();
          Stage ventanaActual = (Stage) btnReimprimir.getScene().getWindow();
          controller.traerOperaciones();
          ventanaActual.close();
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
