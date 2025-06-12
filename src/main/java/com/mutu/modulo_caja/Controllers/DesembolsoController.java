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
public class DesembolsoController implements Initializable {

  @FXML private Button btnCancelar;

  @FXML private TableView tableDesembolsos;

  @FXML private TableColumn<Object[], String> colSocio, colNombre, colMonto, colEstado, colCredito;

  @Autowired public Servicio servicio;

  public int socio;
  public String usuario;

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
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[3])));
    colMonto.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[1])));
    colEstado.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[2])));
    colCredito.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[4])));
  }

  public void setDatos(int socio, String usuario) {
    this.socio = socio;
    this.usuario = usuario;
    desembolsosPendientes(socio);
  }

  public void desembolsosPendientes(int socio) {
    List<Object[]> resultados = servicio.desembolsosPendientes(socio);

    ObservableList<Object[]> desembolsos = FXCollections.observableArrayList();

    for (Object[] resultado : resultados) {
      desembolsos.add(resultado);
    }

    tableDesembolsos.setItems(desembolsos);
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
      procesarDesembolso();
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
  public void procesarDesembolso() {
    if (!tableDesembolsos.getItems().isEmpty() && tableDesembolsos.getItems().size() == 1) {
      Object[] primeraFila = (Object[]) tableDesembolsos.getItems().get(0);

      String montoString = String.valueOf(primeraFila[1]);

      montoString = montoString.replaceAll("[\\$,]", "");
      double monto = Double.parseDouble(montoString);

      String colCredito = String.valueOf(primeraFila[4]);

      String socio = String.valueOf(primeraFila[0]);

      String result = servicio.ProcesarDesembolso(Integer.parseInt(colCredito), usuario, monto, "");

      if (result.equals("CORRECTO")) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("DESEMBOLSO PROCESADO CON ÉXITO");
        alert.setHeaderText("DESEMBOLSO PROCESADO CON ÉXITO");
        alert.setContentText(
            "DESEMBOLSO DEL SOCIO: " + socio + " POR: " + montoString + " HECHO CON EXITO");
        alert.showAndWait();
        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER PROCESAR EL DESEMBOLSO");
        alert.setHeaderText("ERROR EN EL DESEMBOLSO");
        alert.setContentText(result.toUpperCase());
        alert.showAndWait();
      }
    }else{
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("NO HAY DESEMBOLSO PENDIENTE");
      alert.setHeaderText("NO HAY DESEMBOLSO PENDIENTE");
      alert.setContentText("NO HAY DESEMBOLSO PENDIENTE");
      alert.showAndWait();
    }
  }
}
