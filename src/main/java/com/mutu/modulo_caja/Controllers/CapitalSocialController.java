package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
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
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CapitalSocialController implements Initializable {

  private String empresa, nombre, usuario;
  private double monto;
  private int socio;

  @Autowired public Servicio servicio;

  public final Validator validator = new Validator();

  @FXML private TextField txtMontoC, txtNombre, txtEmpresa, txtSocio, txtMontoP;
  @FXML private Label lblError, lblNumSocio, lblCuentaSocio, lblNombreSocio;
  @FXML private Button btnProceder, btnCancelar;

  public void obtenerDatos(String empresa, String nombre, double monto, int socio, String usuario) {
    this.empresa = empresa;
    txtEmpresa.setText(empresa);
    this.socio = socio;
    txtSocio.setText(String.valueOf(socio));
    this.nombre = nombre;
    txtNombre.setText(nombre);
    this.monto = monto;
    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
    txtMontoC.setText(formatter.format(monto));
    this.usuario = usuario;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Validador
    validator
        .createCheck()
        .dependsOn("input", txtMontoP.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.equals("") || Integer.parseInt(texto) == 0) {
                c.error("Ingrese un valor mayor a cero");
                lblError.setText("Ingrese un valor mayor a cero");
              } else if (Integer.parseInt(texto) > 1000) {
                c.error("No puede ingresar valores mayores a $1,000.00");
                lblError.setText("No puede ingresar valores mayores a $1,000.00");
              } else if (Integer.parseInt(texto) < 1000) {
                c.error("No puede ingresar valores menores a $1,000.00");
                lblError.setText("No puede ingresar valores menores a $1,000.00");
              } else {
                lblError.setText("");
              }
            })
        .decorates(txtMontoP)
        .immediate();

    txtMontoP.setTextFormatter(
        new TextFormatter<>(
            change -> {
              change.setText(change.getText().replaceAll("[^0-9]", ""));
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
        Stage ventanaActual = (Stage) txtMontoP.getScene().getWindow();
        ventanaActual.close();
      }
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
      Stage ventanaActual = (Stage) txtMontoP.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void abonarCapitalSocial() {
    String empresaCod = "";

    if (validator.validate()) {

      if (empresa.equals("MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.")) {
        empresaCod = "0001";
      } else {
        empresaCod = "0002";
      }

      double montoP = Double.parseDouble(txtMontoP.getText().trim());
      Map<String, Object> res = servicio.AbonarCapitalSocial(socio, empresaCod, montoP, usuario, "", 0,0,0);

      if (res.get("Resultado").toString().equals("ACTUALIZADO")|| res.get("Resultado").toString().equals("ABONADO")) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ABONO A CAPITAL SOCIAL REALIZADO CON ÉXITO");
        alert.setHeaderText("ABONO REALIZADO CON ÉXITO");
        alert.setContentText(
            "ABONO DE: " + montoP + " REALIZADO CON ÉXITO AL SOCIO: " + txtSocio.getText());
        String nombreEmpresa = servicio.traerEmpresa(empresaCod).getRazonSocial();
        String rfcEmpresa = servicio.traerEmpresa(empresaCod).getRfc();
        String direcEmpresa =
            servicio.traerEmpresa(empresaCod).getCalle()
                + " "
                + servicio.traerEmpresa(empresaCod).getCruzamiento()
                + " COL. CENTRO";
        alert.showAndWait();
        LocalDateTime fecha = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaTicket = fecha.format(formatter);
        String folio = res.get("transaccion_id").toString();
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
        String monto = formatoMoneda.format(montoP);
        String parteNGU = formatoMoneda.format(Double.parseDouble(res.get("socialNGU").toString()));
        String parteMUT = formatoMoneda.format(Double.parseDouble(res.get("socialMUT").toString()));
        MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
        String moneyAsWords = converter.asWords(BigDecimal.valueOf(montoP)).toUpperCase() + " MXN";
        String letrasEnviar = "";
        LocalTime hora = fecha.toLocalTime();
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaFormateada = hora.format(formatterHora);

        PrinterMatrix printer =
            getPrinterMatrix(
                nombreEmpresa,
                rfcEmpresa,
                direcEmpresa,
                String.valueOf(socio),
                folio,
                nombre,
                monto,
                moneyAsWords,
                fechaTicket,
                horaFormateada,
                    parteMUT,
                    parteNGU);

        printer.toFile("impresion_Abono.txt");

        InputStream inputStream = null;
        try {
          inputStream = new FileInputStream("impresion_Abono.txt");
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

        CajeroController.bufferOperaciones += montoP;
        Stage ventanaActual = (Stage) txtSocio.getScene().getWindow();
        ventanaActual.close();
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR EN EL PAGO DE CAPITAL SOCIAL");
        alert.setHeaderText("ERROR EN EL PAGO");
        alert.setContentText(res.get("Resultado").toString().toUpperCase());
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
      String abono,
      String abonoletras,
      String fecha,
      String hora,
      String parteMUT,
      String parteNGU) {
    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String tipoCuenta = "TIPO DE CUENTA: CAPITAL SOCIAL - ABONO A CS";
    String efectivo = "ABONO: " + abono;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    String psmut = "PS MUT: " + parteMUT;
    String psngu = "PS NGU: " +parteNGU;
    //    String totalAhorro = "AHORRO: " + ahorro;
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
    printer.printTextWrap(9, 10, 1, 60, efectivo);
    printer.printTextWrap(10, 11, 1, 60, abonoletras);
    printer.printTextWrap(12, 13, 1, 60, "___________________________________");
    printer.printTextWrap(13, 14, 1, 60, nombre);
    printer.printTextWrap(14, 15, 1, 60, descripcion1);
    printer.printTextWrap(15, 16, 1, 60, descripcion2);
    printer.printTextWrap(16, 17, 1, 60, "HORA: " + hora);
    printer.printTextWrap(16, 17, 16, 60, cajero);
    if (socio <= 8543) {
      printer.printTextWrap(17, 18, 1, 60, psmut);
      printer.printTextWrap(17, 18, 25, 60, psngu);
    } else {
      printer.printTextWrap(17, 18, 1, 60, psngu);
    }

    return printer;
  }
}
