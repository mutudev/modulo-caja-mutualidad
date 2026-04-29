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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class AhorroController implements Initializable {

  // ── FXML ────────────────────────────────────────────────────────────────
  @FXML private Button btnAbonar, btnCancelar;
  @FXML private TextField txtMonto;
  @FXML private Label lblError, lblNumSocio, lblCuentaSocio, lblNombreSocio;

  // ── Estado interno ───────────────────────────────────────────────────────
  private String nombre;
  private String usuario;
  private int    numero;
  private double ahorroTotal;

  // ── Dependencias ─────────────────────────────────────────────────────────
  @Autowired private Servicio servicio;

  private final Validator validator = new Validator();

  // ── Formatters reutilizables ─────────────────────────────────────────────
  private static final DateTimeFormatter FMT_HORA  = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final NumberFormat      FMT_MONEDA =
          NumberFormat.getCurrencyInstance(Locale.US);

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
            .dependsOn("input", txtMonto.textProperty())
            .withMethod(c -> {
              String texto = c.get("input");
              if (texto == null || texto.isBlank()) {
                c.error("Ingrese un valor mayor a cero");
                lblError.setText("Ingrese un valor mayor a cero");
              } else {
                lblError.setText("");
              }
            })
            .decorates(txtMonto)
            .immediate();
  }

  private void configurarTextFormatter() {
    txtMonto.setTextFormatter(new TextFormatter<>(change -> {
      // Solo dígitos y un único punto decimal
      String newText = change.getControlNewText();
      if (!newText.matches("\\d*\\.?\\d*")) {
        return null; // Rechaza el cambio
      }
      return change;
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
  public void setDatos(int num, String nom, String usuario) {
    this.nombre  = nom;
    this.numero  = num;
    this.usuario = usuario;

    var cuenta = servicio.traerCuentaAhorroPorSocio(numero);
    lblNumSocio.setText(String.valueOf(cuenta.getSocio()));
    lblNombreSocio.setText(nombre);
    lblCuentaSocio.setText(cuenta.getNum_cuenta());
  }

  // ════════════════════════════════════════════════════════════════════════
  // Manejo de cierre
  // ════════════════════════════════════════════════════════════════════════
  public void cierreDeVentana(Event event) {
    event.consume();
    cerrarVentana();
  }

  @FXML public void cerrarConBoton() {
    cerrarVentana();
  }

  @FXML public void cerrarConTeclaFocus(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) cerrarVentana();
  }

  @FXML public void cerrarConTecla(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) {
      cerrarVentana();
    }
  }

  /** Cierra la ventana actual de forma limpia. */
  private void cerrarVentana() {
    Stage ventana = (Stage) btnCancelar.getScene().getWindow();
    ventana.close();
  }

  // ════════════════════════════════════════════════════════════════════════
  // Lógica principal
  // ════════════════════════════════════════════════════════════════════════
  @FXML
  public void AbonarAhorro() {
    if (!validator.validate()) return;

    double abono;
    try {
      abono = Double.parseDouble(txtMonto.getText().trim());
    } catch (NumberFormatException e) {
      mostrarError("Monto inválido", "El monto ingresado no es un número válido.");
      return;
    }

    if (abono <= 0) {
      mostrarError("Monto inválido", "El monto debe ser mayor a cero.");
      return;
    }

    String empresaCod = resolverCodigoEmpresa();
    LocalDateTime fecha = LocalDateTime.now();
    String horaFormateada = fecha.toLocalTime().format(FMT_HORA);

    Map<String, Object> result = servicio.AbonarAhorro(
            abono,
            lblCuentaSocio.getText().trim(),
            usuario,
            empresaCod,
            horaFormateada,
            "", 0, 0
    );

    if ("CORRECTO".equals(result.get("resultado"))) {
      ahorroTotal = Double.parseDouble(result.get("ahorro_total").toString());
      mostrarExito(abono);
      imprimirTicket(empresaCod, fecha, horaFormateada, abono, result);
      CajeroController.bufferOperaciones += abono;
      cerrarVentana();
    } else {
      mostrarError(
              "Error al abonar a cuenta de ahorro".toUpperCase(),
              result.get("resultado").toString().toUpperCase()
      );
    }
  }

  // ════════════════════════════════════════════════════════════════════════
  // Métodos auxiliares
  // ════════════════════════════════════════════════════════════════════════

  /** Determina el código de empresa según el número de socio. */
  private String resolverCodigoEmpresa() {
    return Integer.parseInt(lblNumSocio.getText().trim()) >= 8543 ? "0002" : "0001";
  }

  private void mostrarExito(double abono) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Abono a ahorro realizado con éxito".toUpperCase());
    alert.setHeaderText("Abono realizado con éxito".toUpperCase());
    alert.setContentText(String.format(
            "Abono de %s realizado con éxito al socio: %s".toUpperCase(),
            FMT_MONEDA.format(abono), lblNumSocio.getText()
    ));
    alert.showAndWait();
  }

  private void mostrarError(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(titulo);
    alert.setContentText(mensaje);
    alert.showAndWait();
  }

  /**
   * Genera e imprime el ticket de la operación.
   */
  private void imprimirTicket(
          String empresaCod,
          LocalDateTime fecha,
          String horaFormateada,
          double abono,
          Map<String, Object> result
  ) {
    ModelEmpresa empresa = servicio.traerEmpresa(empresaCod);
    String nombreEmpresa = empresa.getRazonSocial();
    String rfcEmpresa    = empresa.getRfc();
    String direcEmpresa  = empresa.getCalle()
            + " " + empresa.getCruzamiento()
            + " COL. CENTRO";

    String fechaTicket = fecha.format(FMT_FECHA);
    String folio       = result.get("transaccion_id").toString();
    String monto       = FMT_MONEDA.format(abono);

    MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
    String moneyAsWords = converter.asWords(BigDecimal.valueOf(abono)).toUpperCase() + " MXN";

    PrintJob impresion = new PrintJob();
    PrinterMatrix printer = impresion.imprimirAhorro(
            nombreEmpresa, rfcEmpresa, direcEmpresa,
            lblNumSocio.getText(), folio, nombre,
            lblCuentaSocio.getText().trim(),
            monto, moneyAsWords, fechaTicket, horaFormateada,
            FMT_MONEDA.format(ahorroTotal)
    );

    printer.toFile("impresion_Ahorro.txt");
    enviarImpresora("impresion_Ahorro.txt");
  }

  /** Envía el archivo al servicio de impresión por defecto. */
  private void enviarImpresora(String rutaArchivo) {
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    if (defaultPrintService == null) {
      mostrarError("Error de impresión".toUpperCase(), "No se encontró una impresora predeterminada.".toUpperCase());
      return;
    }

    try (InputStream inputStream = new FileInputStream(rutaArchivo)) {
      DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
      Doc document        = new SimpleDoc(inputStream, docFormat, null);
      PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();

      defaultPrintService.createPrintJob().print(document, attributeSet);

    } catch (IOException e) {
      mostrarError("Error de impresión".toUpperCase(), "No se pudo leer el archivo: ".toUpperCase() + e.getMessage());
    } catch (PrintException e) {
      mostrarError("Error de impresión ".toUpperCase(), e.getMessage());
    }
  }



}