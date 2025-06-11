package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class CambioController implements Initializable {

  @FXML TextField txtTotal, txtRecibido, txtCambio;
  @FXML Button btnCalcular, btnCerrar;
  @Autowired public Servicio servicio;

  public double totalOperaciones;
  NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    txtRecibido.setTextFormatter(
        new TextFormatter<>(
            change -> {
              change.setText(change.getText().replaceAll("[^0-9]", ""));
              return change;
            }));

    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCerrar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cerrar());
        });
  }

  public void setDatos(double totalOperaciones) {
    this.totalOperaciones = totalOperaciones;
    txtTotal.setText(formatoMoneda.format(totalOperaciones));
  }

  @FXML
  public void obtenerCambio() {

    if (txtRecibido.getText().isEmpty()) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL REALIZAR EL AJUSTE");
      alert.setHeaderText("ERROR AL PROCESAR EL CAMBIO");
      alert.setContentText("POR FAVOR, RELLENE EL CAMPO DEL DINERO RECIBIDO.");
      alert.showAndWait();
      return;
    }

    double recibido = Double.parseDouble(txtRecibido.getText().trim());

    if (recibido < totalOperaciones) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL REALIZAR EL AJUSTE");
      alert.setHeaderText("ERROR AL PROCESAR EL CAMBIO");
      alert.setContentText("EL MONTO RECIBIDO NO PUEDE SER MENOR AL TOTAL DE OPERACIONES.");
      alert.showAndWait();
      return;
    }

    double cambio = recibido - totalOperaciones;
    txtCambio.setText(formatoMoneda.format(cambio));
  }

  public void limpiarCampos() {
    txtRecibido.setText("");
    txtCambio.setText("");
  }

  @FXML
  public void obtenerCambioConTecla(KeyEvent event) {
    switch (event.getCode()) {
      case KeyCode.ENTER -> obtenerCambio();
      case KeyCode.CONTROL -> limpiarCampos();
      case KeyCode.ALT -> cerrar();
      case KeyCode.F5 -> imprimir();
    }
  }

  // COMENTARIO

  @FXML
  public void cerrar() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE VENTANA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    alert.setContentText("SI LA CIERRA, EL TOTAL DE SUS OPERACIONES SE REINICIARÁ A $0.00");
    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      CajeroController.bufferOperaciones = 0;
      Stage ventanaActual = (Stage) btnCerrar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void imprimir() {
    if (txtCambio.getText().isEmpty() || txtRecibido.getText().isEmpty()) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("DATOS FALTANTES");
      alert.setHeaderText("INGRESE TODO LOS VALORES");
      alert.setContentText("FAVOR DE INGRESAR TODOS LOS VALORES ");
      alert.showAndWait();
      return;
    }
    LocalDateTime fecha = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String fechaTicket = fecha.format(formatter);
    LocalTime hora = fecha.toLocalTime();
    DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    String horaFormateada = hora.format(formatterHora);
    String datosusuario = servicio.traerCajero(LoginController.usuarioLoggeado);
    System.out.println(datosusuario);
    String nombre = datosusuario;
    String totalop = txtTotal.getText();
    String cambio = txtCambio.getText();
    String recibido = formatoMoneda.format(Double.parseDouble(txtRecibido.getText()));
    PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
    if (services != null && services.length > 0) {
      PrinterMatrix printer =
          getPrinterMatrix(nombre, totalop, cambio, recibido, fechaTicket, horaFormateada);

      printer.toFile("impresion_Cambio.txt");

      InputStream inputStream = null;
      try {
        inputStream = new FileInputStream("impresion_Cambio.txt");
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
          alert2.setContentText("IMPRESIÓN REALIZADA CON ÉXITO");
          alert2.showAndWait();
        } catch (PrintException b) {

          Alert alert2 = new Alert(Alert.AlertType.ERROR);
          alert2.setTitle("ERROR IMPRIMIENDO");
          alert2.setHeaderText("ERROR IMPRIMIENDO");
          alert2.setContentText("ERROR IMPRIMIENDO");
          alert2.showAndWait();
        }
      } else {

        Alert alert2 = new Alert(Alert.AlertType.ERROR);
        alert2.setTitle("ERROR IMPRIMIENDO");
        alert2.setHeaderText("ERROR IMPRIMIENDO");
        alert2.setContentText("ERROR IMPRIMIENDO");
        alert2.showAndWait();
      }
    } else {

      Alert alert2 = new Alert(Alert.AlertType.ERROR);
      alert2.setTitle("ERROR IMPRIMIENDO");
      alert2.setHeaderText("ERROR IMPRIMIENDO");
      alert2.setContentText("ERROR IMPRIMIENDO");
      alert2.showAndWait();
    }
  }

  private PrinterMatrix getPrinterMatrix(
      String nombre,
      String totalop,
      String cambio,
      String recibido,
      String fechaTicket,
      String horaFormateada) {
    PrinterMatrix printer = new PrinterMatrix();
    String Cajero = "CAJERO: " + nombre;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60); // Columna 1
    printer.printTextWrap(1, 2, 1, 60, "FECHA DE OPERACIONES: " + fechaTicket); // Columna 2
    printer.printTextWrap(2, 3, 1, 60, "HORA DE OPERACIONES: " + horaFormateada); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, Cajero);
    printer.printTextWrap(5, 6, 1, 60, "--LISTADO DE OPERACIONES--");
    printer.printTextWrap(6, 7, 1, 60, "TOTAL OPERACIONES: " + totalop);
    printer.printTextWrap(7, 8, 1, 60, "TOTAL RECIBIDO: " + recibido);
    printer.printTextWrap(8, 9, 1, 60, "CAMBIO: " + cambio);
    printer.printTextWrap(10, 11, 1, 60, descripcion1);
    printer.printTextWrap(11, 12, 1, 60, descripcion2);

    return printer;
  }
}
