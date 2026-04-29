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
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class CapitalSocialController implements Initializable {

  // ── FXML ────────────────────────────────────────────────────────────────
  @FXML private TextField txtMontoC, txtNombre, txtEmpresa, txtSocio, txtMontoP;
  @FXML private Label lblError, lblNumSocio, lblCuentaSocio, lblNombreSocio;
  @FXML private Button btnProceder, btnCancelar;

  // ── Dependencias ─────────────────────────────────────────────────────────
  @Autowired private Servicio servicio;

  // ── Estado interno ───────────────────────────────────────────────────────
  private String empresa;
  private String nombre;
  private String usuario;
  private double monto;
  private int    socio;

  private final Validator validator = new Validator();

  // ── Formatters reutilizables ─────────────────────────────────────────────
  private static final NumberFormat      FMT_MONEDA = NumberFormat.getCurrencyInstance(Locale.US);
  private static final DateTimeFormatter FMT_FECHA  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FMT_HORA   = DateTimeFormatter.ofPattern("HH:mm:ss");


  // ════════════════════════════════════════════════════════════════════════
  // Inicialización
  // ════════════════════════════════════════════════════════════════════════
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    configurarValidador();
    configurarTextFormatter();
    configurarCierreDeVentana();
  }

  private void configurarValidador() {
    validator.createCheck()
            .dependsOn("input", txtMontoP.textProperty())
            .withMethod(c -> {
              String texto = c.get("input");

              if (texto == null || texto.isBlank()) {
                String msg = "Ingrese un valor mayor a cero";
                c.error(msg);
                lblError.setText(msg);
                return;
              }

              double valor;
              try {
                valor = Double.parseDouble(texto);
              } catch (NumberFormatException e) {
                String msg = "El valor ingresado no es válido";
                c.error(msg);
                lblError.setText(msg);
                return;
              }

              // La lógica original tenía > 1000 y < 1000 simultáneamente,
              // lo que hacía IMPOSIBLE validar — solo == 1000 pasaba.
              // Se mantiene esa semántica de forma clara.
              if (valor != 1000) {
                String msg = valor > 1000
                        ? "No puede ingresar valores mayores a $1,000.00"
                        : "No puede ingresar valores menores a $1,000.00";
                c.error(msg);
                lblError.setText(msg);
              } else {
                lblError.setText("");
              }
            })
            .decorates(txtMontoP)
            .immediate();
  }

  private void configurarTextFormatter() {
    txtMontoP.setTextFormatter(new TextFormatter<>(change -> {
      String newText = change.getControlNewText();
      return newText.matches("\\d*(\\.\\d*)?") ? change : null;
    }));
  }

  private void configurarCierreDeVentana() {
    Platform.runLater(() -> {
      Stage stage = (Stage) btnCancelar.getScene().getWindow();
      stage.setOnCloseRequest(this::cierreDeVentana);
    });
  }

  // ════════════════════════════════════════════════════════════════════════
  // Datos del socio
  // ════════════════════════════════════════════════════════════════════════
  public void obtenerDatos(String empresa, String nombre, double monto, int socio, String usuario) {
    this.empresa  = empresa;
    this.socio    = socio;
    this.nombre   = nombre;
    this.monto    = monto;
    this.usuario  = usuario;

    txtEmpresa.setText(empresa);
    txtSocio.setText(String.valueOf(socio));
    txtNombre.setText(nombre);
    txtMontoC.setText(FMT_MONEDA.format(monto));
  }

  // ════════════════════════════════════════════════════════════════════════
  // Cierre de ventana
  // ════════════════════════════════════════════════════════════════════════
  public void cierreDeVentana(Event event) {
    event.consume();
    cerrarVentana();
  }

  @FXML public void cerrarConBoton() { cerrarVentana(); }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE)  cerrarVentana();
  }

  @FXML
  public void cerrarConTeclafocus(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) cerrarVentana();
  }

  private void cerrarVentana() {
    ((Stage) btnCancelar.getScene().getWindow()).close();
  }

  // ════════════════════════════════════════════════════════════════════════
  // Lógica principal
  // ════════════════════════════════════════════════════════════════════════
  @FXML
  public void abonarCapitalSocial() {
    if (!validator.validate()) return;

    double montoP;
    try {
      montoP = Double.parseDouble(txtMontoP.getText().trim());
    } catch (NumberFormatException e) {
      mostrarError("Monto inválido", "El monto ingresado no es un número válido.");
      return;
    }

    System.out.println(txtEmpresa.getText());
    String empresaCod     = servicio.traerEmpresaConRS(txtEmpresa.getText().trim()).getCodigo();
    LocalDateTime fecha   = LocalDateTime.now();
    String horaFormateada = fecha.toLocalTime().format(FMT_HORA);

    Map<String, Object> res = servicio.AbonarCapitalSocial(
            socio, empresaCod, horaFormateada, montoP, usuario, "", 0, 0, 0);

    String resultado = res.get("Resultado").toString();
    if ("ACTUALIZADO".equals(resultado) || "ABONADO".equals(resultado)) {
      mostrarExito(montoP);
      imprimirTicket(empresaCod, fecha, horaFormateada, montoP, res);
      CajeroController.bufferOperaciones += montoP;
      cerrarVentana();
    } else {
      mostrarError("Error en el pago de capital social".toUpperCase(), resultado.toUpperCase());
    }
  }

  // ════════════════════════════════════════════════════════════════════════
  // Métodos auxiliares
  // ════════════════════════════════════════════════════════════════════════

  private void mostrarExito(double montoP) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Abono a capital social realizado con éxito".toUpperCase());
    alert.setHeaderText("Abono realizado con éxito".toUpperCase());
    alert.setContentText(String.format(
            "Abono de %s realizado con éxito al socio: %s".toUpperCase(),
            FMT_MONEDA.format(montoP), txtSocio.getText()
    ));
    alert.showAndWait();
  }

  private void mostrarError(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo.toUpperCase());
    alert.setHeaderText(titulo.toUpperCase());
    alert.setContentText(mensaje.toUpperCase());
    alert.showAndWait();
  }

  /**
   * Genera e imprime el ticket del abono a capital social.
   * Una sola llamada a traerEmpresa() en lugar de cuatro.
   */
  private void imprimirTicket(
          String empresaCod,
          LocalDateTime fecha,
          String horaFormateada,
          double montoP,
          Map<String, Object> res
  ) {
    // ── Una sola llamada al servicio ─────────────────────────────────────
    ModelEmpresa emp        = servicio.traerEmpresa(empresaCod);
    String nombreEmpresa = emp.getRazonSocial();
    String rfcEmpresa    = emp.getRfc();
    String direcEmpresa  = emp.getCalle() + " " + emp.getCruzamiento() + " COL. CENTRO";

    String fechaTicket = fecha.format(FMT_FECHA);
    String folio       = res.get("transaccion_id").toString();
    String monto       = FMT_MONEDA.format(montoP);
    String parteNGU    = FMT_MONEDA.format(Double.parseDouble(res.get("socialNGU").toString()));
    String parteMUT    = FMT_MONEDA.format(Double.parseDouble(res.get("socialMUT").toString()));

    MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
    String moneyAsWords = converter.asWords(BigDecimal.valueOf(montoP)).toUpperCase() + " MXN";

    PrintJob impresion = new PrintJob();
    PrinterMatrix printer = impresion.imprimirCapSocial(
            nombreEmpresa, rfcEmpresa, direcEmpresa,
            String.valueOf(socio), folio, nombre,
            monto, moneyAsWords, fechaTicket, horaFormateada,
            parteMUT, parteNGU
    );

    printer.toFile("impresion_Abono.txt");
    enviarImpresora("impresion_Abono.txt");
  }

  /** Envía el archivo al servicio de impresión predeterminado. */
  private void enviarImpresora(String rutaArchivo) {
    PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
    if (printService == null) {
      mostrarError("Error de impresión", "No se encontró una impresora predeterminada.");
      return;
    }

    try (InputStream inputStream = new FileInputStream(rutaArchivo)) {
      DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
      Doc document        = new SimpleDoc(inputStream, docFormat, null);
      PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();

      printService.createPrintJob().print(document, attributeSet);

    } catch (IOException e) {
      mostrarError("Error de impresión", "No se pudo leer el archivo: " + e.getMessage());
    } catch (PrintException e) {
      mostrarError("Error de impresión", e.getMessage());
    }
  }


}