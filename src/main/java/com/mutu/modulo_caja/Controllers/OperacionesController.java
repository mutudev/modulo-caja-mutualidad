package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelEmpresa;
import com.mutu.modulo_caja.Models.ModelOperaciones;
import com.mutu.modulo_caja.Models.ModelUsuario;
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
public class OperacionesController implements Initializable {

  @FXML private ComboBox cmbEmpresa, cmbOperacion, cmbCajero;

  @Autowired public Servicio servicio;

  @FXML private TableView tablaOperaciones;
  @FXML private TableView tableTraslados;

  @FXML
  private TableColumn<Object[], String> colTrasID,
      colTrasUser,
      colTrasOrigen,
      colTrasDestino,
      colTrasMonto,
      colTrasFecha;
  @FXML
  private TableColumn<Object[], String> colSocio,
      colNombre,
      colFecha,
      colHora,
      colOperacion,
      colMonto;

  String turno = "";

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

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

    List<ModelUsuario> usuarios = servicio.traerCajero(1);
    ObservableList<String> cajeros = FXCollections.observableArrayList();
    for (ModelUsuario usuario : usuarios) {
      cajeros.add(usuario.getUsuario().toUpperCase());
    }
    cmbCajero.setItems(cajeros);
    cmbCajero.getSelectionModel().selectFirst();

    // Para operaciones
    colSocio.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[0])));
    colNombre.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[1])));
    colFecha.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[2])));
    colHora.setCellValueFactory(
        cellData -> new SimpleStringProperty((String) cellData.getValue()[6]));
    colOperacion.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[4])));
    colMonto.setCellValueFactory(
        cellData -> new SimpleStringProperty((String) cellData.getValue()[3]));

    // Para traslados
    colTrasID.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[0])));

    colTrasUser.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[1])));

    colTrasOrigen.setCellValueFactory(
        cellData -> {
          Object valor = cellData.getValue()[5];

          if (valor instanceof Number) {
            return new SimpleStringProperty(String.valueOf(valor));
          } else if (valor instanceof String) {
            return new SimpleStringProperty((String) valor);
          } else {
            return new SimpleStringProperty("");
          }
        });

    colTrasDestino.setCellValueFactory(
        cellData -> {
          Object valor = cellData.getValue()[6];

          if (valor instanceof Number) {
            return new SimpleStringProperty(String.valueOf(valor));
          } else if (valor instanceof String) {
            return new SimpleStringProperty((String) valor);
          } else {
            return new SimpleStringProperty("");
          }
        });

    colTrasMonto.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[3])));

    colTrasFecha.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[7])));

    Platform.runLater(
        () -> {
          Stage stage = (Stage) cmbEmpresa.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });
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
    int usuario_id =
        servicio
            .traerDatosUsuario(cmbCajero.getSelectionModel().getSelectedItem().toString())
            .getId();
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
      case "TRASLADO DE BÓVEDA A CAJAS":
        tipo_operacion = 6;
        break;
      case "TRASLADO DE CAJAS A BÓVEDA":
        tipo_operacion = 7;
        break;
      case "ABONO A CUENTA DE PREVISIÓN SOCIAL":
        tipo_operacion = 10;
        break;
    }

    List<Object[]> operaciones;

    if (tipo_operacion == 6 || tipo_operacion == 7) {
      operaciones = servicio.historialTraslados(usuario_id, tipo_operacion, 1, fechaEnviar, turno, empresa);
      tablaOperaciones.setVisible(false);
      tableTraslados.setVisible(true);
    } else {
      operaciones =
          servicio.traerHistorial(fechaEnviar, 1, empresa, turno, usuario_id, tipo_operacion, 1);
      tablaOperaciones.setVisible(true);
      tableTraslados.setVisible(false);
    }

    ObservableList<Object[]> datosOperaciones = FXCollections.observableArrayList();

    for (Object[] resultado : operaciones) {
      int operacion = (int) resultado[4];
      if (operacion == tipo_operacion) {
        datosOperaciones.add(resultado);
      }
    }

    // Asignar los datos filtrados al TableView
    if (tablaOperaciones.isVisible()) {
      tablaOperaciones.setItems(datosOperaciones);
    } else {
      tableTraslados.setItems(datosOperaciones);
    }
  }

  public void setDatos(String turno) {
    this.turno = turno;
    traerOperaciones();
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
      Stage ventanaActual = (Stage) cmbEmpresa.getScene().getWindow();
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
        Stage ventanaActual = (Stage) cmbEmpresa.getScene().getWindow();
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
      Stage ventanaActual = (Stage) cmbEmpresa.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void verOperacion() {
    String id_traslado = "";
    String cuenta_origen = " ";
    String cuenta_destino = " ";
    String nombre_usuario = "";
    String numero_socio = "";
    String id_transaccion = "";
    String monto = "";
    String fecha = "";
    String nombre_socio = "";
    int tipo_operacion = 0;
    String capital_pagado = "";
    String interes_pagado = "";
    String mora_pagada = "";
    String iva_pagado = "";
    String bonif_aplicada = "";
    boolean validador = false;
    String empresa =
        cmbEmpresa
                .getSelectionModel()
                .getSelectedItem()
                .toString()
                .equals("MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.")
            ? "0001"
            : "0002";

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
      case "TRASLADO DE BÓVEDA A CAJAS":
        tipo_operacion = 6;
        break;
      case "TRASLADO DE CAJAS A BÓVEDA":
        tipo_operacion = 7;
        break;
      case "ABONO A CUENTA DE PREVISIÓN SOCIAL":
        tipo_operacion = 10;
        break;
    }

    if (tipo_operacion == 6 || tipo_operacion == 7) {
      Object[] selectedRow = (Object[]) tableTraslados.getSelectionModel().getSelectedItem();

      if (selectedRow != null) {
        if (tipo_operacion == 6) {
          cuenta_origen = (String) selectedRow[5];
          cuenta_destino = (String) selectedRow[6];
        } else {
          cuenta_origen = (String) selectedRow[5];
          cuenta_destino = (String) selectedRow[6];
        }
        id_traslado = (String) String.valueOf(selectedRow[0]);
        monto = (String) selectedRow[3];
        fecha= (String) selectedRow[7];
        nombre_usuario = (String) selectedRow[1];
      }
    } else {
      validador = true;
      Object[] selectedRow = (Object[]) tablaOperaciones.getSelectionModel().getSelectedItem();

      if (selectedRow != null) {
        nombre_usuario = cmbCajero.getSelectionModel().getSelectedItem().toString();
        numero_socio = (String) String.valueOf(selectedRow[0]);
        id_transaccion = (String) String.valueOf(selectedRow[11]);
        monto = (String) selectedRow[3];
        fecha = (String) selectedRow[2];
        nombre_socio = (String) selectedRow[1];
        capital_pagado = (String) selectedRow[13];
        interes_pagado = (String) selectedRow[14];
        mora_pagada = (String) selectedRow[15];
        iva_pagado = (String) selectedRow[16];
        bonif_aplicada = (String) selectedRow[17];
      }
    }

    try {

      Stage nuevaVentana = new Stage();
      FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/verOperacion.fxml"));
      fxml.setControllerFactory(Main.context::getBean);
      Scene nuevaEscena = new Scene(fxml.load());
      ReimpresionController controller = fxml.getController();
      if (validador) {
        controller.setDatosCancelacion(
            numero_socio,
            id_transaccion,
            cmbOperacion.getSelectionModel().getSelectedItem().toString(),
            cmbEmpresa.getSelectionModel().getSelectedItem().toString(),
            fecha,
            monto,
            nombre_socio,
            tipo_operacion,
            turno,nombre_usuario,this);
      } else {
        controller.setDatosTraslados(
            id_traslado,
            nombre_usuario,
            monto,
            cmbOperacion.getSelectionModel().getSelectedItem().toString(),
            cuenta_origen,
            cuenta_destino,
            fecha,
            tipo_operacion, this, turno);
      }

      nuevaEscena
          .getStylesheets()
          .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
      nuevaVentana.setTitle("DATOS DE LA OPERACIÓN");
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
}
