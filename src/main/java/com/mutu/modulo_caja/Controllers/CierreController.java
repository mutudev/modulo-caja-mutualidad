package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelTraslado;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class CierreController implements Initializable {

  @FXML private TextField txtSaldoMut, txtSaldoNgu;
  @FXML private Button btnCerrar, btnCancelar;
  private Label lblParaCerrarLaPrincipal;

  @FXML private Label lblListo;

  @Autowired public Servicio servicio;

  public int cuentCajaMutu = 0;
  public int cuentaCajaNgu = 0;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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

  public void setDatos(List<Object[]> cuentaMUT, List<Object[]> cuentaNGU, Label lbl) {
    this.lblParaCerrarLaPrincipal = lbl;

    NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

    if (cuentaMUT.isEmpty() && cuentaNGU.isEmpty()) {
      txtSaldoNgu.setDisable(true);
      txtSaldoMut.setDisable(true);
      lblListo.setText("EL CAJERO NO CUENTA CON CUENTAS DE CAJA PARA CERRAR");
      btnCerrar.setDisable(true);
    } else if (cuentaMUT.size() == 1 && cuentaNGU.size() == 1) {
      ModelTraslado trasladoMut = servicio.traerTrasladoCajero(cuentaMUT.get(0)[0].toString());
      ModelTraslado trasladoNgu = servicio.traerTrasladoCajero(cuentaNGU.get(0)[0].toString());

      double montoEnCuentaMut =
          Double.parseDouble(cuentaMUT.get(0)[3].toString()) - trasladoMut.getMonto();
      double montoEnCuentaNgu =
          Double.parseDouble(cuentaNGU.get(0)[3].toString()) - trasladoNgu.getMonto();
      if (montoEnCuentaMut != 0 || montoEnCuentaNgu != 0) {
        lblListo.setText("EL CAJERO AÚN NO HA TRASALADO TODO SU SALDO EN SUS CUENTAS DE CAJA");
        btnCerrar.setDisable(true);
      } else {
        lblListo.setText("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA");
        cuentCajaMutu = Integer.parseInt(trasladoMut.getCuentaOrigen());
        cuentaCajaNgu = Integer.parseInt(trasladoNgu.getCuentaOrigen());
      }
      txtSaldoMut.setText(formatoMoneda.format(montoEnCuentaMut));
      txtSaldoNgu.setText(formatoMoneda.format(montoEnCuentaNgu));

    } else if (cuentaMUT.isEmpty() && cuentaNGU.size() == 1) {

      ModelTraslado trasladoNgu = servicio.traerTrasladoCajero(cuentaNGU.get(0)[0].toString());
      double montoEnCuentaNgu =
          Double.parseDouble(cuentaNGU.get(0)[3].toString()) - trasladoNgu.getMonto();
      if (montoEnCuentaNgu != 0) {
        lblListo.setText("EL CAJERO AÚN NO HA TRASALADO TODO SU SALDO EN SUS CUENTAS DE CAJA");
        btnCerrar.setDisable(true);
      } else {
        lblListo.setText("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA");
        cuentaCajaNgu = Integer.parseInt(trasladoNgu.getCuentaOrigen());
      }
      txtSaldoNgu.setText(formatoMoneda.format(montoEnCuentaNgu));

    } else {
      ModelTraslado trasladoMut = servicio.traerTrasladoCajero(cuentaMUT.get(0)[0].toString());

      double montoEnCuentaMut =
          Double.parseDouble(cuentaMUT.get(0)[3].toString()) - trasladoMut.getMonto();
      if (montoEnCuentaMut != 0) {
        lblListo.setText("EL CAJERO AÚN NO HA TRASALADO TODO SU SALDO EN SUS CUENTAS DE CAJA");
        btnCerrar.setDisable(true);
      } else {
        lblListo.setText("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA");
        cuentCajaMutu = Integer.parseInt(trasladoMut.getCuentaOrigen());
      }
      txtSaldoMut.setText(formatoMoneda.format(montoEnCuentaMut));
    }
  }

  @FXML
  public void cerrar() {
    if (lblListo.getText().equals("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA")) {

      LocalDate fecha = LocalDate.now();

      String resMutu =
          servicio.pa_procesarCierre(cuentCajaMutu, 0, fecha.toString(), "");

      String resNgu =
              servicio.pa_procesarCierre(0, cuentaCajaNgu, fecha.toString(), "");

      boolean valido = isValido(resMutu, resNgu);

      if (valido) {
        try {
          Stage ventanaActual = (Stage) btnCerrar.getScene().getWindow();
          Stage ventanaPrincipal = (Stage) lblParaCerrarLaPrincipal.getScene().getWindow();
          Stage nuevaVentana = new Stage();
          FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/login.fxml"));
          fxml.setControllerFactory(Main.context::getBean);
          Scene nuevaEscena = new Scene(fxml.load());
          nuevaEscena
                  .getStylesheets()
                  .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
          nuevaVentana.setTitle("AUTENTICACIÓN DE USUARIO");
          Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
          nuevaVentana.getIcons().add(icon);
          nuevaVentana.setScene(nuevaEscena);
          nuevaVentana.setResizable(false);
          nuevaVentana.centerOnScreen();
          nuevaVentana.show();
          ventanaActual.close();
          ventanaPrincipal.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private boolean isValido(String resMutu, String resNgu) {
    boolean valido = false;
    if (resMutu.equals("CORRECTO") && resNgu.equals("CORRECTO")) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("CIERRE DE CAJERO REALIZADO CORRECTAMENTE");
      alert.setHeaderText("CIERRE EXITOSO");
      alert.setContentText("EL CIERRE DEL CAJERO: " + LoginController.usuarioLoggeado + " FUE EXITOSO.");
      alert.showAndWait();
      valido = true;
    } else {
      String error = "";
      if (resMutu.equals("CORRECTO") && !resNgu.equals("CORRECTO")) {
          error = resNgu.toUpperCase();
      } else if(!resMutu.equals("CORRECTO") && resNgu.equals("CORRECTO")){
          error = resMutu.toUpperCase();
      } else {
          error = resMutu.toUpperCase();
      }
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL REALIZAR EL CIERRE DE CAJERO");
      alert.setHeaderText("ERROR EN EL CIERRE");
      alert.setContentText("ERROR: " + error);
      alert.showAndWait();
    }
    return valido;
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
}
