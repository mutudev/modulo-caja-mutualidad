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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class PrevisionController implements Initializable {

  @Autowired private Servicio servicio;

  @FXML private TextField txtMontoC, txtNombre, txtEmpresa, txtSocio, txtMontoP;
  @FXML private Button btnProceder, btnCancelar;

  @FXML private Label lblError;

  public final Validator validator = new Validator();
  NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

  public String nombreSocio, usuario, empresa;
  public int socio;
  public double montoCubierto;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    validator
        .createCheck()
        .dependsOn("input", txtMontoP.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.equals("") ) {
                c.error("Ingrese un valor mayor a cero");
                lblError.setText("Ingrese un valor mayor a cero");
              } else {
                lblError.setText("");
              }
            })
        .decorates(txtMontoP)
        .immediate();

    txtMontoP.setTextFormatter(
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

  public void obtenerDatos(
      String empresa, String usuario, int socio, String nombreSocio, double montoCubierto) {
    this.empresa = empresa;
    txtEmpresa.setText(empresa);
    this.usuario = usuario;
    this.socio = socio;
    txtSocio.setText(String.valueOf(socio));
    this.nombreSocio = nombreSocio;
    txtNombre.setText(nombreSocio);
    this.montoCubierto = montoCubierto;
    txtMontoC.setText(formatoMoneda.format(montoCubierto));
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
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.CONTROL)) {
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
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
    if (event.getCode().equals(KeyCode.ENTER)) {
      AbonarPrevision();
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
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }

  @FXML
  public void AbonarPrevision() {

    if (validator.validate()) {
      double monto = Double.parseDouble(txtMontoP.getText().trim());
      int numSocio = Integer.parseInt(txtSocio.getText().trim());
      String empresa = "";
      if (txtEmpresa.getText().trim().equals("MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.")) {
        empresa = "0001";
      } else {
        empresa = "0002";
      }

      LocalDateTime fecha = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      LocalTime hora = fecha.toLocalTime();
      DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
      String horaFormateada = hora.format(formatterHora);
      Map<String, Object>  resultado = servicio.AbonarPrevisionSocial(numSocio, empresa,horaFormateada, monto, usuario, "",
              0,0,0);

      if (resultado.get("Resultado").toString().equals("ABONADO")||resultado.get("Resultado").toString().equals("ACTUALIZADO")) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PAGO DE PREVISIÓN SOCIAL EXITOSO");
        alert.setHeaderText("PAGO DE PREVISIÓN SOCIAL HECHO CORRECTAMENTE");
        alert.setContentText("PAGO PROCESADO CORRECTAMENTE PARA EL SOCIO: " + numSocio);
        alert.showAndWait();

        String nombreEmpresa = servicio.traerEmpresa(empresa).getRazonSocial();
        String rfcEmpresa = servicio.traerEmpresa(empresa).getRfc();
        String direcEmpresa =
            servicio.traerEmpresa(empresa).getCalle()
                + " "
                + servicio.traerEmpresa(empresa).getCruzamiento()
                + " COL. CENTRO";
        String fechaTicket = fecha.format(formatter);
        String socio = String.valueOf(numSocio);
        String folio = resultado.get("transaccion_id").toString();
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
        String montoCubierto = formatoMoneda.format(Double.parseDouble(resultado.get("monto_ticket").toString()));
        String montoAbonado = formatoMoneda.format(monto);
        MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
        String moneyAsWords = converter.asWords(BigDecimal.valueOf(monto)).toUpperCase() + " MXN";
        String montoAsignado = formatoMoneda.format(Double.parseDouble(resultado.get("monto_asignado").toString()));;

        // Contar chars para mejor visualizacion
        PrintJob impresion = new PrintJob();
        PrinterMatrix printer =
            impresion.imprimirPrevision(
                nombreEmpresa,
                rfcEmpresa,
                direcEmpresa,
                socio,
                folio,
                nombreSocio,
                montoAbonado,
                moneyAsWords,
                fechaTicket,
                horaFormateada,
                montoAsignado,
                    montoCubierto);

        printer.toFile("impresion_PRESOC.txt");

        InputStream inputStream = null;
        try {
          inputStream = new FileInputStream("impresion_PRESOC.txt");
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

        CajeroController.bufferOperaciones += monto;
        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER PAGAR LA PREVISIÓN SOCIAL");
        alert.setHeaderText("ERROR EN EL PAGO DE PREVISIÓN SOCIAL");
        alert.setContentText(resultado.get("Resultado").toString().toUpperCase());
        alert.showAndWait();
      }

    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL QUERER PAGAR LA PREVISIÓN SOCIAL");
      alert.setHeaderText("ERROR EN EL PAGO DE PREVISIÓN SOCIAL");
      alert.setContentText("POR FAVOR, CUMPLA CON LAS VALIDACIONES DEL CAMPO");
      alert.showAndWait();
    }
  }
}
