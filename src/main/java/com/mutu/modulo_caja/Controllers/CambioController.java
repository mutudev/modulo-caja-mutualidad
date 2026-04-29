package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Services.Servicio;
import com.mutu.modulo_caja.utils.PrintJob;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class CambioController implements Initializable {

  // ── FXML ────────────────────────────────────────────────────────────────
  @FXML private TextField txtTotal, txtRecibido, txtCambio;
  @FXML private Button btnCalcular, btnCerrar;

  // ── Dependencias ─────────────────────────────────────────────────────────
  @Autowired private Servicio servicio;

  // ── Estado interno ───────────────────────────────────────────────────────
  private double totalOperaciones;
  public static double operacionesAnterior = 0;

  // ── Formatters reutilizables ─────────────────────────────────────────────
  private static final NumberFormat      FMT_MONEDA = NumberFormat.getCurrencyInstance(Locale.US);
  private static final DateTimeFormatter FMT_FECHA  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FMT_HORA   = DateTimeFormatter.ofPattern("HH:mm:ss");

  // ════════════════════════════════════════════════════════════════════════
  // Inicialización
  // ════════════════════════════════════════════════════════════════════════
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    configurarTextFormatter();
    configurarCierreDeVentana();
  }

  private void configurarTextFormatter() {
    txtRecibido.setTextFormatter(new TextFormatter<>(change -> {
      String newText = change.getControlNewText();
      return newText.matches("\\d*(\\.\\d*)?") ? change : null;
    }));
  }

  private void configurarCierreDeVentana() {
    Platform.runLater(() -> {
      Stage stage = (Stage) btnCerrar.getScene().getWindow();
      stage.setOnCloseRequest(event -> cerrar());
    });
  }

  // ════════════════════════════════════════════════════════════════════════
  // Datos
  // ════════════════════════════════════════════════════════════════════════
  public void setDatos(double totalOperaciones) {
    this.totalOperaciones = totalOperaciones;
    txtTotal.setText(FMT_MONEDA.format(totalOperaciones));
  }

  // ════════════════════════════════════════════════════════════════════════
  // Acciones principales
  // ════════════════════════════════════════════════════════════════════════
  @FXML
  public void obtenerCambio() {
    if (txtRecibido.getText().isBlank()) {
      mostrarError("Error al procesar el cambio",
              "Por favor, rellene el campo del dinero recibido.");
      return;
    }

    double recibido;
    try {
      recibido = Double.parseDouble(txtRecibido.getText().trim());
    } catch (NumberFormatException e) {
      mostrarError("Error al procesar el cambio", "El monto ingresado no es válido.");
      return;
    }

    if (recibido < totalOperaciones) {
      mostrarError("Error al procesar el cambio",
              "El monto recibido no puede ser menor al total de operaciones.");
      return;
    }

    txtCambio.setText(FMT_MONEDA.format(recibido - totalOperaciones));
  }

  @FXML
  public void cerrar() {
    CajeroController.bufferOperaciones = 0;
    operacionesAnterior = totalOperaciones;
    ((Stage) btnCerrar.getScene().getWindow()).close();
  }

  @FXML
  public void imprimir() {
    if (txtCambio.getText().isBlank() || txtRecibido.getText().isBlank()) {
      mostrarError("Datos faltantes", "Favor de ingresar todos los valores.");
      return;
    }

    LocalDateTime fecha = LocalDateTime.now();
    String fechaTicket   = fecha.format(FMT_FECHA);
    String horaFormateada = fecha.toLocalTime().format(FMT_HORA);

    // Una sola llamada al servicio — el resultado ya es el nombre
    String nombre   = servicio.traerCajeroPorUsuario(LoginController.usuarioLoggeado);
    String totalop  = txtTotal.getText();
    String cambio   = txtCambio.getText();
    String recibido = FMT_MONEDA.format(Double.parseDouble(txtRecibido.getText()));

    PrintJob impresion = new PrintJob();
    PrinterMatrix printer = impresion.imprimirCambio(
            nombre, totalop, cambio, recibido, fechaTicket, horaFormateada);

    printer.toFile("impresion_Cambio.txt");
    enviarImpresora("impresion_Cambio.txt");
  }

  // ════════════════════════════════════════════════════════════════════════
  // Teclado
  // ════════════════════════════════════════════════════════════════════════
  @FXML
  public void obtenerCambioConTecla(KeyEvent event) {
    switch (event.getCode()) {
      case ENTER  -> obtenerCambio();
      case CONTROL -> limpiarCampos();
      case ALT    -> cerrar();
      case F5     -> imprimir();
      default     -> { /* ignorar */ }
    }
  }

  // ════════════════════════════════════════════════════════════════════════
  // Métodos auxiliares
  // ════════════════════════════════════════════════════════════════════════
  public void limpiarCampos() {
    txtRecibido.clear();
    txtCambio.clear();
  }

  private void mostrarError(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(titulo);
    alert.setContentText(mensaje);
    alert.showAndWait();
  }

  /**
   * Envía el archivo al servicio de impresión predeterminado.
   * Usa try-with-resources para garantizar el cierre del InputStream.
   * Llama a lookupDefaultPrintService() una sola vez.
   */
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