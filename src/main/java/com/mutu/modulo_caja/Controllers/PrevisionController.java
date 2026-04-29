package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Models.ModelEmpresa;
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
import java.util.ResourceBundle;

@Component
public class PrevisionController implements Initializable {

  @Autowired private Servicio servicio;

  @FXML private TextField txtMontoC, txtNombre, txtEmpresa, txtSocio, txtMontoP;
  @FXML private Button btnProceder, btnCancelar;
  @FXML private Label lblError;

  public Validator validator = new Validator();
  NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

  public String nombreSocio, usuario, empresa;
  public int socio;
  public double montoCubierto;

  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FORMATO_HORA  = DateTimeFormatter.ofPattern("HH:mm:ss");

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    validator
            .createCheck()
            .dependsOn("input", txtMontoP.textProperty())
            .withMethod(c -> {
              String texto = c.get("input");
              boolean vacio = texto == null || texto.isEmpty();
              lblError.setText(vacio ? "Ingrese un valor mayor a cero" : "");
              if (vacio) c.error("Ingrese un valor mayor a cero");
            })
            .decorates(txtMontoP)
            .immediate();

    txtMontoP.setTextFormatter(new TextFormatter<>(change -> {
      change.setText(change.getText().replaceAll("[^0-9.]", ""));
      String texto = change.getText();
      if (texto.matches(".*\\..*\\..*")) {
        change.setText(texto.substring(0, texto.lastIndexOf('.')));
      }
      return change;
    }));

    Platform.runLater(() -> {
      Stage stage = (Stage) btnCancelar.getScene().getWindow();
      stage.setOnCloseRequest(this::cierreDeVentana);
    });
  }

  public void obtenerDatos(String empresa, String usuario, int socio,
                           String nombreSocio, double montoCubierto) {
    this.empresa      = empresa;
    this.usuario      = usuario;
    this.socio        = socio;
    this.nombreSocio  = nombreSocio;
    this.montoCubierto = montoCubierto;

    txtEmpresa.setText(empresa);
    txtSocio.setText(String.valueOf(socio));
    txtNombre.setText(nombreSocio);
    txtMontoC.setText(formatoMoneda.format(montoCubierto));
  }

  // Extracción: lógica de cierre repetida en 3 métodos → un solo método privado
  private void cerrarVentana() {
    validator = new Validator();
    ((Stage) btnCancelar.getScene().getWindow()).close();
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    cerrarVentana();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      cerrarVentana();
    }
  }

  @FXML
  public void cerrarConBoton() {
    cerrarVentana();
  }

  // Extracción: obtener hora formateada actual
  private String getHoraFormateada() {
    return LocalTime.now().format(FORMATO_HORA);
  }

  @FXML
  public void AbonarPrevision() {
    if (!validator.validate()) {
      mostrarAlerta(Alert.AlertType.ERROR,
              "ERROR AL QUERER PAGAR LA PREVISIÓN SOCIAL",
              "ERROR EN EL PAGO DE PREVISIÓN SOCIAL",
              "POR FAVOR, CUMPLA CON LAS VALIDACIONES DEL CAMPO");
      return;
    }

    double monto       = Double.parseDouble(txtMontoP.getText().trim());
    int numSocio       = Integer.parseInt(txtSocio.getText().trim());
    String codigoEmp   = servicio.traerEmpresaConRS(txtEmpresa.getText().trim()).getCodigo();
    String horaFormateada = getHoraFormateada();
    LocalDateTime fecha = LocalDateTime.now();

    Map<String, Object> resultado =
            servicio.AbonarPrevisionSocial(numSocio, codigoEmp, horaFormateada, monto, usuario, "", 0, 0, 0);

    String resultadoStr = resultado.get("Resultado").toString();

    if (resultadoStr.equals("ABONADO") || resultadoStr.equals("ACTUALIZADO")) {
      mostrarAlerta(Alert.AlertType.INFORMATION,
              "PAGO DE PREVISIÓN SOCIAL EXITOSO",
              "PAGO DE PREVISIÓN SOCIAL HECHO CORRECTAMENTE",
              "PAGO PROCESADO CORRECTAMENTE PARA EL SOCIO: " + numSocio);

      imprimirTicket(codigoEmp, numSocio, monto, fecha, horaFormateada, resultado);

      CajeroController.bufferOperaciones += monto;
      cerrarVentana();
    } else {
      mostrarAlerta(Alert.AlertType.ERROR,
              "ERROR AL QUERER PAGAR LA PREVISIÓN SOCIAL",
              "ERROR EN EL PAGO DE PREVISIÓN SOCIAL",
              resultadoStr.toUpperCase());
    }
  }

  // Extracción: bloque de impresión separado de la lógica de negocio
  private void imprimirTicket(String codigoEmp, int numSocio, double monto,
                              LocalDateTime fecha, String horaFormateada,
                              Map<String, Object> resultado) {
    ModelEmpresa empresaModel = servicio.traerEmpresa(codigoEmp);
    String nombreEmpresa = empresaModel.getRazonSocial();
    String rfcEmpresa    = empresaModel.getRfc();
    String direcEmpresa  = empresaModel.getCalle() + " "
            + empresaModel.getCruzamiento() + " COL. CENTRO";

    String fechaTicket    = fecha.format(FORMATO_FECHA);
    String folio          = resultado.get("transaccion_id").toString();
    String montoCubierto  = formatoMoneda.format(
            Double.parseDouble(resultado.get("monto_ticket").toString()));
    String montoAbonado   = formatoMoneda.format(monto);
    String montoAsignado  = formatoMoneda.format(
            Double.parseDouble(resultado.get("monto_asignado").toString()));
    String moneyAsWords   = MoneyConverters.SPANISH_BANKING_MONEY_VALUE
            .asWords(BigDecimal.valueOf(monto)).toUpperCase() + " MXN";

    PrintJob impresion = new PrintJob();
    PrinterMatrix printer = impresion.imprimirPrevision(
            nombreEmpresa, rfcEmpresa, direcEmpresa,
            String.valueOf(numSocio), folio, nombreSocio,
            montoAbonado, moneyAsWords, fechaTicket, horaFormateada,
            montoAsignado, montoCubierto);

    printer.toFile("impresion_PRESOC.txt");

    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream("impresion_PRESOC.txt");
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
      mostrarAlerta(Alert.AlertType.ERROR, "ERROR IMPRIMIENDO", "ERROR IMPRIMIENDO", null);
    }
  }

  // Extracción: construcción repetida de Alert → un solo método utilitario
  private void mostrarAlerta(Alert.AlertType tipo, String titulo, String header, String contenido) {
    Alert alert = new Alert(tipo);
    alert.setTitle(titulo);
    alert.setHeaderText(header);
    if (contenido != null) alert.setContentText(contenido);
    alert.showAndWait();
  }
}