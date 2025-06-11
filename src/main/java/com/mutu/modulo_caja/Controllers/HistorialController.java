package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelEmpresa;
import com.mutu.modulo_caja.Models.ModelOperaciones;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class HistorialController implements Initializable {

  @FXML private ComboBox cmbEmpresa, cmbOperacion;
  @FXML private Button btnCancelar, btnVer;

  @FXML private TableView tableHistorial;

  @FXML private TableColumn<Object[], String> colSocio, colNombre, colFecha, colHora, colMonto;
  public String usuario, turno;

  List<Object[]> CopiaOperaciones;

  @Autowired public Servicio servicio;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (cmbEmpresa != null && cmbOperacion != null) {


      List<ModelEmpresa> empresas = servicio.traerEmpresas();
      ObservableList<String> empresasProcesadas = FXCollections.observableArrayList();

      for (ModelEmpresa empresa : empresas) {
        empresasProcesadas.add(empresa.getRazonSocial());
      }

      cmbEmpresa.setItems(empresasProcesadas);
      cmbEmpresa.getSelectionModel().selectFirst();

      List<ModelOperaciones> operaciones = servicio.traerOperacions();

      ObservableList<String> operacionesProcesadas = FXCollections.observableArrayList();

      for (ModelOperaciones operacion : operaciones) {
        operacionesProcesadas.add(operacion.getOperacion().toUpperCase());
      }

      cmbOperacion.setItems(operacionesProcesadas);
      cmbOperacion.getSelectionModel().selectFirst();

      cmbEmpresa.setItems(empresasProcesadas);
      cmbOperacion.setItems(operacionesProcesadas);
      cmbEmpresa.getSelectionModel().selectFirst();
      cmbOperacion.getSelectionModel().selectFirst();
    }

    colSocio.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[0])));
    colNombre.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[1])));
    colFecha.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[2])));
    colHora.setCellValueFactory(
        cellData -> new SimpleStringProperty((String) cellData.getValue()[6]));
    colMonto.setCellValueFactory(
        cellData -> new SimpleStringProperty((String) cellData.getValue()[3]));

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
        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      }
    }

    if (event.getCode().equals(KeyCode.ENTER)) {
      verOperacion();
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
  public void traerOperaciones() {
    LocalDateTime fecha = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String fechaEnviar = fecha.format(formatter);

    String empresa =
        cmbEmpresa
                .getSelectionModel()
                .getSelectedItem()
                .toString()
                .equals("MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.")
            ? "0001"
            : "0002";
    int usuario_id = servicio.traerDatosUsuario(usuario).getId();
    int tipo_operacion = 0;

    // Determina el tipo de operación basado en la selección
    switch (cmbOperacion.getSelectionModel().getSelectedItem().toString()) {
      case "ABONO A CUENTA DE AHORRO":
        tipo_operacion = 1;
        break;
      case "ABONO A CUENTA DE CRÉDITO":
        tipo_operacion = 2;
        break;
      case "PROCESAMIENTO DE DESEMBOLSO":
        tipo_operacion = 3;
        break;
      case "PROCESAMIENTO DE RETIRO":
        tipo_operacion = 4;
        break;
      case "ABONO A CUENTA DE CAPITAL SOCIAL":
        tipo_operacion = 5;
        break;
      case "ABONO A CUENTA DE PREVISIÓN SOCIAL":
        tipo_operacion = 10;
        break;
    }

    List<Object[]> operaciones =
        servicio.traerHistorial(fechaEnviar, 1, empresa, turno, usuario_id, tipo_operacion, 1);
    CopiaOperaciones = operaciones;


    ObservableList<Object[]> datosOperaciones = FXCollections.observableArrayList();
    for (Object[] resultado : operaciones) {
      int operacion = (int) resultado[4];
      if (operacion == tipo_operacion) {
        datosOperaciones.add(resultado);
      }
    }

    // Asignar los datos filtrados al TableView
    tableHistorial.setItems(datosOperaciones);
  }

  @FXML
  public void verOperacion() {

    Object[] selectedRow = (Object[]) tableHistorial.getSelectionModel().getSelectedItem();

    String nombre = "", numSocio = "", fecha = "", monto = "";
    String IdOperacion = "";
    if (selectedRow != null) {
      nombre = (String) selectedRow[1];
      numSocio = String.valueOf(selectedRow[0]);
      fecha = (String) selectedRow[2];
      monto = (String) selectedRow[3];
      IdOperacion = String.valueOf(selectedRow[11]);
    }else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("NO HA SELECCIONADO NINGUNA OPERACIÓN");
      alert.setHeaderText("SIN OPERACIÓN SELECCIONADA");
      alert.setContentText("POR FAVOR, SELECCIONE UNA OPERACIÓN.");
      alert.showAndWait();
      return;
    }

    String operacion = cmbOperacion.getSelectionModel().getSelectedItem().toString();
    String empresa = cmbEmpresa.getSelectionModel().getSelectedItem().toString();

    try {
      Stage nuevaVentana = new Stage();
      FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/verOperacion.fxml"));
      fxml.setControllerFactory(Main.context::getBean);
      Scene nuevaEscena = new Scene(fxml.load());
      ReimpresionController controller = fxml.getController();
      controller.setDatos(numSocio, IdOperacion, operacion, empresa, fecha, monto, nombre);
      nuevaEscena
          .getStylesheets()
          .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
      nuevaVentana.setTitle("REIMPRESIÓN DE OPERACIÓN");
      nuevaVentana.setScene(nuevaEscena);
      nuevaVentana.setResizable(false);
      nuevaVentana.centerOnScreen();
      nuevaVentana.initModality(Modality.APPLICATION_MODAL);
      nuevaVentana.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void detectarClicks(MouseEvent event) {
    if (event.getClickCount() == 2) {
      verOperacion();
    }
  }

  public void setDatos(String usuario, String turno) {
    this.usuario = usuario;
    this.turno = turno;
    traerOperaciones();
  }
}
