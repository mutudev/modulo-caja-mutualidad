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
    txtMontoC.setText(String.valueOf(montoCubierto));
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
      AbonarPrevision();
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

      Map<String, Object>  resultado = servicio.AbonarPrevisionSocial(numSocio, empresa, monto, usuario, "",
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
        LocalDateTime fecha = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
        LocalTime hora = fecha.toLocalTime();
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaFormateada = hora.format(formatterHora);
        PrinterMatrix printer =
            getPrinterMatrix(
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

  private PrinterMatrix getPrinterMatrix(
          String empresa,
          String rfc,
          String direc,
          String numsocio,
          String folio,
          String nomsocio,
          String abono,
          String abonoletras,
          String fecha,
          String hora,
          String montoAsignado, String montoCubierto) {
    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String tipoCuenta = "TIPO DE CUENTA: PREVISION SOCIAL - ABONO A PS";
    String efectivo = "ABONO: " + abono;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
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
    printer.printTextWrap(7, 8, 1, 60, tipoCuenta);
    printer.printTextWrap(9, 10, 1, 60, efectivo); // Columna 10
    printer.printTextWrap(10, 11, 1, 60, abonoletras);
    printer.printTextWrap(12, 13, 1, 60, "___________________________________");
    printer.printTextWrap(13, 14, 1, 60, nombre);
    printer.printTextWrap(14, 15, 1, 60, descripcion1);
    printer.printTextWrap(15, 16, 1, 60, descripcion2);
    printer.printTextWrap(16, 17, 1, 60, "HORA: " + hora);
    printer.printTextWrap(16, 17, 16, 60, cajero);
    printer.printTextWrap(17, 18, 1, 60, "MONTO CUBIERTO: " +   montoCubierto);
    printer.printTextWrap(18, 19, 1, 60, "MONTO ASIGNADO: " +   montoAsignado); // Columna 16

    return printer;
  }
}
