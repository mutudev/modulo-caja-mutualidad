package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
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
      cuenta_destino;
  public int opcion = 0;
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
      String nombre) {
    this.nombre = nombre;
    this.socio = socio;
    this.id = id;
    this.operacion = operacion;
    this.empresa = empresa;
    this.fecha = fecha;
    this.monto = monto;
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
      PrinterMatrix printer = new PrinterMatrix();
      String empresa = "MUTUALIDAD DOCE DE AGOSTO, S.C. DE R.L. DE C.V.";
      String rfc = "RFC: MDA-770513-NHB";
      String direccion = "CALLE 23 No. 100-B ENTRE 18 Y 20 COL. CENTRO";
      String fecha = "FECHA: 05/06/2025";
      String folio = "FOlIO: 000001";
      String numSocio = "SOCIO: 8995";
      String nombre = "NOMBRE: JOSE YAEL MEX MONTERO";
      String cuenta = "No. CUENTA: 00028995";
      String tipoCuenta = "TIPO DE CUENTA: AHORRO   -   ABONO AHORRO";
      String efectivo = "ABONO: 5274.00";
      String Documentos = "RECIBIDO:";
      String linea = "________________________________";
      String total = "TOTAL:";
      String letras = "CINCO MIL DOSCIENTOS SETENTA Y CUATRO PESOS";
      String descripcion = "LA NO OBJECION A ESTE COMPROBANTE IMPLICA SU ACEPTACION";
      String hora = "08:14";
      String cajero = "USUARIO: JMEX";
      String totalAhorro = "AHORRO: 17000.00";
      String parteSocialMUT = "PARTE SOCIAL MUT: 1,000.00";
      String parteSocialNGU = "PARTE SOCIAL NGU: 1,000.00";
      Extenso e = new Extenso();
      e.setNumber(21.59);printer.setOutSize(30, 60);
      printer.printTextWrap(1, 2, 1, 60, empresa);           // Columna 1
      printer.printTextWrap(2, 3, 1, 60, rfc);              // Columna 2
      printer.printTextWrap(3, 4, 1, 60, direccion);        // Columna 3
      printer.printTextWrap(4, 5, 1, 60, fecha);            // Columna 4
      printer.printTextWrap(5, 6, 1, 60, numSocio);
      printer.printTextWrap(5, 6, 30, 60, folio);            // Columna 5// Columna 6
      printer.printTextWrap(6, 7, 1, 60, nombre);           // Columna 7
      printer.printTextWrap(7, 8, 1, 60, cuenta);           // Columna 8
      printer.printTextWrap(8, 9, 1, 60, tipoCuenta);
      printer.printTextWrap(10, 11, 1, 60, efectivo);// Columna 10
      printer.printTextWrap(11, 12, 1, 60, letras);
      printer.printTextWrap(12, 13, 1, 60, "00/100 MXN");
      printer.printTextWrap(14, 15, 1, 60, "_____________________________");
      printer.printTextWrap(15, 16, 1, 60, nombre);
      printer.printTextWrap(16, 17, 1, 60, hora);
      printer.printTextWrap(16, 17, 8, 60, hora);
      printer.printTextWrap(16, 17, 16, 60, "JTEC");   // Columna 16
      printer.printTextWrap(17, 18, 1, 60, "CREDITO PREI: 8600.00");
      printer.printTextWrap(18, 19, 1, 60, totalAhorro);    // Columna 17// Columna 18
      // Columna 18
      printer.toFile("impresion.txt");

      InputStream inputStream = null;
      try {
          inputStream = new FileInputStream("impresion.txt");
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
      if(defaultPrintService!=null){
        DocPrintJob printJob = defaultPrintService.createPrintJob();
          try{
          printJob.print( document, attributeSet);
              Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
              alert2.setTitle("IMPRESIÓN REALIZADA CON ÉXITO");
              alert2.setHeaderText("IMPRESIÓN REALIZADA CON ÉXITO");
              Stage ventanaActual = (Stage) btnReimprimir.getScene().getWindow();
              ventanaActual.close();
        }catch (PrintException b){
          System.out.println("Error "+ b.toString());
        }
      }else{
        System.err.println("NO HAY IMPRESORAS INSTALADAS");
        Alert alert2 = new Alert(Alert.AlertType.ERROR);
        alert2.setTitle("ERROR IMPRIMIENDO");
        alert2.setHeaderText("ERROR IMPRIMIENDO");
        alert2.showAndWait();

      }
    }
  }
}

