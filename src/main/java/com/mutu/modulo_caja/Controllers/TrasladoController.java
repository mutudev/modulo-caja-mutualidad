package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class TrasladoController implements Initializable {
  @FXML private ComboBox cmbTipoTras;

  @FXML private TextField txtCantidad, txtEmpresa;
  @FXML private Button btnProcesar, btnCancelar;

  @FXML private Label lblError;

  public String empresa, usuario, turno;
  public CajeroController cajeroController;

  @Autowired public Servicio servicio;

  public final Validator validator = new Validator();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (cmbTipoTras != null) {
      ObservableList<String> empresas = FXCollections.observableArrayList();
      empresas.addAll("BOVEDA - CAJA", "CAJA - BOVEDA");
      cmbTipoTras.setItems(empresas);
      cmbTipoTras.getSelectionModel().selectFirst();
    }

    validator
        .createCheck()
        .dependsOn("input", txtCantidad.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.equals("") || Integer.parseInt(texto) == 0) {
                c.error("Ingrese un valor mayor a cero");
                lblError.setText("Ingrese un valor mayor a cero");
              } else {
                lblError.setText("");
              }
            })
        .decorates(txtCantidad)
        .immediate();

    txtCantidad.setTextFormatter(
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

  public void setEmpresa(
      String empresa, String usuario, String turno, CajeroController controller) {
    this.cajeroController = controller;
    this.turno = turno;
    this.usuario = usuario;
    this.empresa = empresa;
    txtEmpresa.setText(empresa);
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
      trasladar();
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
  public void trasladar() {

    if (validator.validate()) {
      String nom_empresa = txtEmpresa.getText().trim();
      String cod_empresa = "";

      switch (nom_empresa) {
        case "MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.":
          cod_empresa = "0001";
          break;
        case "NUEVA GENERACIÓN DE UMÁN, AC.":
          cod_empresa = "0002";
          break;
      }

      String tipoTraslado = cmbTipoTras.getSelectionModel().getSelectedItem().toString();
      int opcion = 0;
      switch (tipoTraslado) {
        case "BOVEDA - CAJA":
          opcion = 6;
          break;
        case "CAJA - BOVEDA":
          opcion = 7;
          break;
      }

      double monto_trasladar = Double.parseDouble(txtCantidad.getText().trim());

      if (opcion == 7) {

        LocalDate fecha = LocalDate.now();
        List<Object[]> permisoTraslado =
            servicio.traerCuentaCajero(
                LoginController.usuarioLoggeado, fecha.toString(), 1, turno, cod_empresa);

        for (Object[] fila : permisoTraslado) {
          if (Integer.parseInt(fila[8].toString()) == 1
              || Integer.parseInt(fila[8].toString()) == 2) {
            Alert alert2 = new Alert(Alert.AlertType.ERROR);
            alert2.setTitle("ERROR AL PROCESAR EL TRASLADO DE CIERRE");
            alert2.setHeaderText("ERROR AL INTENTAR TRASLADAR PARA CERRAR");
            alert2.setContentText("AÚN NO HA REALIZADO SU AJUSTE DE SOBRANTES Y FALTANTES.");
            alert2.showAndWait();
            return;
          }
        }
      }

      String res =
          servicio.procesarTraslado(usuario, monto_trasladar, opcion, cod_empresa, turno, "");
      boolean procesar = true;
      boolean permitir = false;

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      switch (res) {
        case "APERTURA":
          alert.setTitle("APERTURA EXITOSA");
          alert.setHeaderText("APERTURA REALIZADA CORRECTAMENTE");
          alert.setContentText(
              "EL CAJERO: "
                  + usuario
                  + " AHORA ESTÁ LISTO PARA REALIZAR SUS OPERACIONES EN: "
                  + nom_empresa);
          alert.showAndWait();
          permitir = true;
          break;
        case "CIERRE":
          alert.setTitle("TRASLADO DE CIERRE EXITOSO");
          alert.setHeaderText("TRASLADO DE CIERRE REALIZADO CORRECTAMENTE");
          alert.setContentText(
              "EL CAJERO: " + usuario + " AHORA ESTÁ LISTO PARA CERRAR EN: " + nom_empresa);
          alert.showAndWait();
          break;
        default:
          Alert alert2 = new Alert(Alert.AlertType.ERROR);
          alert2.setTitle("ERROR AL PROCESAR EL TRASLADO DEL CAJERO");
          alert2.setHeaderText("ERROR AL INTENTAR LA OPERACIÓN DESEADA");
          alert2.setContentText("ERROR: " + res.toUpperCase().trim());
          alert2.showAndWait();
          procesar = false;
      }

      if (procesar) {
        cajeroController.apertura = permitir;
        Stage ventanaActual = (Stage) txtCantidad.getScene().getWindow();
        ventanaActual.close();
      }

    } else {
      Alert alert2 = new Alert(Alert.AlertType.ERROR);
      alert2.setTitle("ERROR AL PROCESAR EL TRASLADO");
      alert2.setHeaderText("ERROR AL INTENTAR LA OPERACIÓN DESEADA");
      alert2.setContentText("POR FAVOR, CUMPLA LAS VALIDACIONES DEL CAMPO.");
      alert2.showAndWait();
    }
  }
}
