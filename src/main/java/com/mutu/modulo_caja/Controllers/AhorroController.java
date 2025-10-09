package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Services.Servicio;
import com.mutu.modulo_caja.utils.PrintJob;
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

  public Validator validator = new Validator();

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
            }));

    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCancelar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    //    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //    alert.setTitle("CIERRE DE VENTANA");
    //    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    //    alert.setContentText(
    //        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
    //            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
    //
    //    Optional<ButtonType> result = alert.showAndWait();
    //    if (result.isPresent() && result.get() == ButtonType.OK) {
    //
    //    }
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }


  @FXML
  public void cerrarConTeclaFocus(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      //      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      //      alert.setTitle("CIERRE DE VENTANA");
      //      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
      //      alert.setContentText(
      //          "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
      //              + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
      //
      //      Optional<ButtonType> result = alert.showAndWait();
      //      if (result.isPresent() && result.get() == ButtonType.OK) {
      //
      //      }
      validator = new Validator();
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }
  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      //      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      //      alert.setTitle("CIERRE DE VENTANA");
      //      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
      //      alert.setContentText(
      //          "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
      //              + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
      //
      //      Optional<ButtonType> result = alert.showAndWait();
      //      if (result.isPresent() && result.get() == ButtonType.OK) {
      //
      //      }
      validator = new Validator();
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
    if (event.getCode().equals(KeyCode.ENTER)) {
      AbonarAhorro();
    }
  }

  @FXML
  public void cerrarConBoton() {
    //    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //    alert.setTitle("CIERRE DE VENTANA");
    //    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    //    alert.setContentText(
    //        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
    //            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
    //
    //    Optional<ButtonType> result = alert.showAndWait();
    //    if (result.isPresent() && result.get() == ButtonType.OK) {
    //
    //    }
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
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

      LocalDateTime fecha = LocalDateTime.now();
      LocalTime hora = fecha.toLocalTime();
      DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
      String horaFormateada = hora.format(formatterHora);
      Map<String, Object> result =
          servicio.AbonarAhorro(
              abono,
              lblCuentaSocio.getText().trim(),
              usuario,
              empresaCod,
              horaFormateada,
              "",
              0,
              0);

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaTicket = fecha.format(formatter);
        String socio = lblNumSocio.getText();
        String folio = result.get("transaccion_id").toString();
        String numCuenta = lblCuentaSocio.getText().trim();
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
        String monto = formatoMoneda.format(abono);
        MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
        String moneyAsWords = converter.asWords(BigDecimal.valueOf(abono)).toUpperCase() + " MXN";
        String Ahorro = formatoMoneda.format(ahorrototal);
        PrintJob impresion = new PrintJob();
        PrinterMatrix printer =
            impresion.imprimirAhorro(
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
    } else {
      return;
    }
  }
}
