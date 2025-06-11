package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class AhorroController implements Initializable {

  @FXML private Button btnAbonar, btnCancelar;

  @FXML private TextField txtMonto;

  @FXML private Label lblError, lblNumSocio, lblCuentaSocio, lblNombreSocio;
  public String nombre, usuario;

  public int numero;
  public double ahorrototal;

  @Autowired private Servicio servicio;

  public final Validator validator = new Validator();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Validador
    validator
        .createCheck()
        .dependsOn("input", txtMonto.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.equals("")) {
                c.error("Ingrese un valor mayor a cero");
                lblError.setText("Ingrese un valor mayor a cero");
              } else {
                lblError.setText("");
              }
            })
        .decorates(txtMonto)
        .immediate();

    txtMonto.setTextFormatter(
            new TextFormatter<>(
                    change -> {
                      // Permite solo dígitos y el punto decimal
                      change.setText(change.getText().replaceAll("[^0-9.]", ""));

                      // Verifica si ya hay más de un punto decimal
                      if (change.getText().matches(".*\\..*\\..*")) {
                        change.setText(change.getText().substring(0, change.getText().lastIndexOf('.')));
                      }

                      return change;
                    }
            )
    );


    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCancelar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE VENTANA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.CONTROL)) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("CIERRE DE VENTANA");
      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
      alert.setContentText(
          "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
              + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      }
    }
    if (event.getCode().equals(KeyCode.ENTER)) {
      AbonarAhorro();
    }
  }

  @FXML
  public void cerrarConBoton() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE VENTANA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  public void setDatos(int num, String nom, String usuario) {
    this.nombre = nom;
    this.numero = num;
    this.usuario = usuario;
    lblNumSocio.setText(String.valueOf(servicio.traerCuentaAhorroPorSocio(numero).getSocio()));
    lblNombreSocio.setText(nombre);
    lblCuentaSocio.setText(servicio.traerCuentaAhorroPorSocio(numero).getNum_cuenta());
  }

  @FXML
  public void AbonarAhorro() {

    if (validator.validate()) {

      String empresaCod = "";

      if (Integer.parseInt(lblNumSocio.getText().trim()) >= 8543) {
        empresaCod = "0002";
      } else {
        empresaCod = "0001";
      }

      Double abono = Double.parseDouble(txtMonto.getText().trim());
      Map<String, Object> result =
          servicio.AbonarAhorro(abono, lblCuentaSocio.getText().trim(), usuario, empresaCod, "",0,0);

      if (result.get("resultado").toString().equals("CORRECTO")) {
        ahorrototal = Double.parseDouble(result.get("ahorro_total").toString());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ABONO A AHORRO REALIZADO CON ÉXITO");
        alert.setHeaderText("ABONO REALIZADO CON ÉXITO");
        alert.setContentText(
            "ABONO DE: "
                + txtMonto.getText().trim()
                + " REALIZADO CON ÉXITO AL SOCIO: "
                + lblNumSocio.getText());
        alert.showAndWait();
        String nombreEmpresa = servicio.traerEmpresa(empresaCod).getRazonSocial();
        String rfcEmpresa = servicio.traerEmpresa(empresaCod).getRfc();
        String direcEmpresa =
            servicio.traerEmpresa(empresaCod).getCalle()
                + " "
                + servicio.traerEmpresa(empresaCod).getCruzamiento()
                + " COL. CENTRO";
        LocalDateTime fecha = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaTicket = fecha.format(formatter);
        String socio = lblNumSocio.getText();
        String folio = result.get("transaccion_id").toString();
        System.out.println(folio);
        String numCuenta = lblCuentaSocio.getText().trim();
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
        String monto = formatoMoneda.format(abono);
        MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
        String moneyAsWords = converter.asWords(BigDecimal.valueOf(abono)).toUpperCase() + " MXN";

        String letrasEnviar = "";

        // Contar chars para mejor visualizacion
        LocalTime hora = fecha.toLocalTime();
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaFormateada = hora.format(formatterHora);
        String Ahorro = formatoMoneda.format(ahorrototal);
        PrinterMatrix printer =
            getPrinterMatrix(
                nombreEmpresa,
                rfcEmpresa,
                direcEmpresa,
                socio,
                folio,
                nombre,
                numCuenta,
                monto,
                moneyAsWords,
                fechaTicket,
                horaFormateada,
                Ahorro);

        printer.toFile("impresion_Ahorro.txt");

        InputStream inputStream = null;
        try {
          inputStream = new FileInputStream("impresion_Ahorro.txt");
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
            Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
            alert2.setTitle("IMPRESIÓN REALIZADA CON ÉXITO");
            alert2.setHeaderText("IMPRESIÓN REALIZADA CON ÉXITO");
          } catch (PrintException b) {

            Alert alert2 = new Alert(Alert.AlertType.ERROR);
            alert2.setTitle("ERROR IMPRIMIENDO");
            alert2.setHeaderText("ERROR IMPRIMIENDO");
            alert2.showAndWait();
          }
        } else {

          Alert alert2 = new Alert(Alert.AlertType.ERROR);
          alert2.setTitle("ERROR IMPRIMIENDO");
          alert2.setHeaderText("ERROR IMPRIMIENDO");
          alert2.showAndWait();
        }
        CajeroController.bufferOperaciones += abono;
        Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
        ventanaActual.close();
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER ABONAR A CUENTA DE AHORRO");
        alert.setHeaderText("ERROR EN EL ABONO");
        alert.setContentText(result.get("resultado").toString().toUpperCase());
        alert.showAndWait();
      }
    }
  }

  private PrinterMatrix getPrinterMatrix(
      String empresa,
      String rfc,
      String direc,
      String numsocio,
      String folio,
      String nomsocio,
      String numcuenta,
      String abono,
      String abonoletras,
      String fecha,
      String hora,
      String ahorro) {
    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String cuenta = "No. CUENTA: " + numcuenta;
    String tipoCuenta = "TIPO DE CUENTA: AHORRO   -   ABONO AHORRO";
    String efectivo = "ABONO: " + abono;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    String totalAhorro = "AHORRO: " + ahorro;
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60);
    printer.printTextWrap(1, 2, 1, 60, empresa); // Columna 1
    printer.printTextWrap(2, 3, 1, 60, "RFC: " + rfc); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, direc); // Columna 3
    printer.printTextWrap(4, 5, 1, 60, "FECHA: " + fecha); // Columna 4
    printer.printTextWrap(5, 6, 1, 60, numSocio);
    printer.printTextWrap(5, 6, 30, 60, "FOLIO: " + folio); // Columna 5// Columna 6
    printer.printTextWrap(6, 7, 1, 60, nombre); // Columna 7
    printer.printTextWrap(7, 8, 1, 60, cuenta); // Columna 8
    printer.printTextWrap(8, 9, 1, 60, tipoCuenta);
    printer.printTextWrap(10, 11, 1, 60, efectivo); // Columna 10

    int numchar = abonoletras.length();
    if (numchar <= 43) {
      printer.printTextWrap(10, 11, 1, 60, efectivo);
      printer.printTextWrap(11, 12, 1, 60, abonoletras);
      printer.printTextWrap(13, 14, 1, 60, "___________________________________");
      printer.printTextWrap(14, 15, 1, 60, nombre);
      printer.printTextWrap(15, 16, 1, 60, descripcion1);
      printer.printTextWrap(16, 17, 1, 60, descripcion2);
      printer.printTextWrap(17, 18, 1, 60, "HORA: " + hora);
      printer.printTextWrap(17, 18, 16, 60, cajero); // Columna 16
      printer.printTextWrap(18, 19, 1, 60, totalAhorro); // Columna 17// Columna 18
      printer.printTextWrap(18, 19, 20, 60, "CREDITO PREI: 8600.00");

    } else {
      String primeralinea = "";
      String segundalinea = "";
      String[] partes = abonoletras.split("(?i)\\s*CON\\s*");
      if (partes.length >= 2) {
        primeralinea = partes[0];
        segundalinea = "CON " + partes[1];
      }
      printer.printTextWrap(9, 10, 1, 60, efectivo);
      printer.printTextWrap(10, 11, 1, 60, primeralinea);
      printer.printTextWrap(11, 12, 1, 60, segundalinea);
      printer.printTextWrap(13, 14, 1, 60, "___________________________________");
      printer.printTextWrap(14, 15, 1, 60, nombre);
      printer.printTextWrap(15, 16, 1, 60, descripcion1);
      printer.printTextWrap(16, 17, 1, 60, descripcion2);
      printer.printTextWrap(17, 18, 1, 60, "HORA: " + hora);
      printer.printTextWrap(17, 18, 16, 60, cajero); // Columna 16
      printer.printTextWrap(18, 19, 1, 60, totalAhorro); // Columna 17// Columna 18
    }

    return printer;
  }
}
