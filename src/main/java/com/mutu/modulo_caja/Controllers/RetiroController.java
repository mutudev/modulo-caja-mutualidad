package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class RetiroController implements Initializable {

  @FXML private Button btnCancelar, btnProcesar;

  @FXML private TableView tableRetiros;

  @FXML private TableColumn<Object[], String> colSocio, colNombre, colMonto;
  @FXML private TextField txtAhorroAnt, txtAhorroDesp, txtIdentificador, txtEstado;

  public int socio;
  public String usuario;
  public String turno;
  public String empresa;

  @Autowired public Servicio servicio;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCancelar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });

    colSocio.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[0])));
    colNombre.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[6])));
    colMonto.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[3])));
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

  public void setDatos(int socio, String usuario, String turno) {
    this.socio = socio;
    this.usuario = usuario;
    this.turno = turno;
    traerRetiro(socio);
  }

  public void traerRetiro(int socio) {
    List<Object[]> resultados = servicio.retirosPendientes(socio);

    ObservableList<Object[]> retiros = FXCollections.observableArrayList();

    for (Object[] resultado : resultados) {
      retiros.add(resultado);
    }

    tableRetiros.setItems(retiros);

    if (!retiros.isEmpty()) {
      Object[] primeraFila = retiros.get(0);
      txtAhorroAnt.setText(String.valueOf(primeraFila[1]));
      txtAhorroDesp.setText(String.valueOf(primeraFila[2]));
      txtEstado.setText(String.valueOf(primeraFila[5]));
      txtIdentificador.setText(String.valueOf(primeraFila[4]));
      empresa = primeraFila[7].toString();
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
      procesarRetiro();
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
  public void procesarRetiro() {

    if (!tableRetiros.getItems().isEmpty() && tableRetiros.getItems().size() == 1) {
      Object[] primeraFila = (Object[]) tableRetiros.getItems().get(0);

      String montoString = String.valueOf(primeraFila[3]);

      montoString = montoString.replaceAll("[\\$,]", "");
      double monto = Double.parseDouble(montoString);

      int id = Integer.parseInt(txtIdentificador.getText().trim());
      String result = servicio.ProcesarRetiro(id, socio, usuario, monto, empresa, turno, "");

      if (result.equals("CORRECTO")) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("RETIRO PROCESADO CON ÉXITO");
        alert.setHeaderText("RETIRO PROCESADO CON ÉXITO");
        alert.setContentText(
            "RETIRO DEL SOCIO: " + socio + " POR: " + montoString + " HECHO CON EXITO");
        alert.showAndWait();
        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER PROCESAR EL RETIRAR");
        alert.setHeaderText("ERROR EN EL RETIRO");
        alert.setContentText(result.toUpperCase());
        alert.showAndWait();
      }
    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("NO TIENE RETIROS PENDIENTES");
      alert.setHeaderText("NO TIENE RETIROS PENDIENTES");
      alert.setContentText("NO TIENE RETIROS PENDIENTES");
      alert.showAndWait();
    }
  }
}
