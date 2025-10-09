package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Services.Servicio;
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

import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class FalSobController implements Initializable {

  @FXML private TextField txtSaldoFin, txtSaldoFis, txtAjuste, txtEmpresa;
  @FXML private Button btnRegistrar, btnCancelar;
  @FXML private Label lblTipo, lblError;
  public String empresa, turno;

  public Validator validator = new Validator();

  @Autowired public Servicio servicio;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Validador
    validator
        .createCheck()
        .dependsOn("input", txtSaldoFis.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.equals("") || Double.parseDouble(texto) == 0) {
                c.error("Ingrese un valor mayor a cero");
                lblError.setText("Ingrese un valor mayor a cero");
              } else {
                lblError.setText("");
              }
            })
        .decorates(txtSaldoFis)
        .immediate();

    txtSaldoFis.setTextFormatter(
        new TextFormatter<>(
            change -> {
              String newText = change.getControlNewText();
              // Permitir solo dígitos y un punto decimal
              if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
              } else {
                return null; // Rechaza el cambio
              }
            }));

    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCancelar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });
  }

  @FXML
  public void realizarAjuste(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)
        && event != null
        && !txtSaldoFis.getText().isEmpty()) {
      double ajuste = 0;
      double valorNumerico = 0;

      NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
      try {
        Number numero = formatoMoneda.parse(txtSaldoFin.getText().trim());
        valorNumerico = numero.doubleValue();
      } catch (ParseException e) {
        e.printStackTrace();
      }

      if (Double.parseDouble(txtSaldoFis.getText()) > valorNumerico) {
        lblTipo.setText("SOBRANTE:");
        ajuste = Double.parseDouble(txtSaldoFis.getText()) - valorNumerico;
      } else if (Double.parseDouble(txtSaldoFis.getText()) < valorNumerico) {
        lblTipo.setText("FALTANTE:");
        ajuste = valorNumerico - Double.parseDouble(txtSaldoFis.getText());
      } else {
        lblTipo.setText("SIN AJUSTE");
      }

      String valorFormateado = formatoMoneda.format(ajuste);
      txtAjuste.setText(valorFormateado);
      txtSaldoFis.setDisable(true);
      btnRegistrar.setDisable(false);
    }else if(event.getCode().equals(KeyCode.ESCAPE)){
      cerrarConBoton();
    }
  }

  @FXML
  public void focus(KeyEvent event) {
    if(event.getCode().equals(KeyCode.ENTER)
            && event != null
            && !txtAjuste.getText().isEmpty()){
      procesarAjuste();

    }
  }


  @FXML
  public void procesarAjuste() {

    double valorNumerico = 0;
    NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
    try {
      Number numero = formatoMoneda.parse(txtAjuste.getText().trim());
      valorNumerico = numero.doubleValue();
    } catch (ParseException e) {
      e.printStackTrace();
    }

    double saldoFis = Double.parseDouble(txtSaldoFis.getText().trim());

    String res = "";

    if (empresa.equals("MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.")) {
      empresa = "0001";
    } else {
      empresa = "0002";
    }

    if (lblTipo.getText().equals("SOBRANTE:")) {
      res =
          servicio.procesarAjuste(
              LoginController.usuarioLoggeado, 0, valorNumerico, turno, empresa, saldoFis, "");
    } else if (lblTipo.getText().equals("SIN AJUSTE")) {
      res =
          servicio.procesarAjuste(
              LoginController.usuarioLoggeado, 0, 0, turno, empresa, saldoFis, "");
    } else {
      res =
          servicio.procesarAjuste(
              LoginController.usuarioLoggeado, valorNumerico, 0, turno, empresa, saldoFis, "");
    }

    if (res.equals("CORRECTO")) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("AJUSTE ÉXITOSO");
      alert.setHeaderText("AJUSTE DE SALDO DE CAJERO HECHO CORRECTAMENTE");
      alert.setContentText(
          "EL CAJERO YA SE ENCUENTRA LISTO PARA REALIZAR SU TRASLADO EN: " + txtEmpresa.getText());
      alert.showAndWait();

      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();

    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR EN EL AJUSTE");
      alert.setHeaderText("ERROR AL REALIZAR EL AJUSTE DE SALDO");
      alert.setContentText("ERROR: " + res.toUpperCase());
      alert.showAndWait();
    }
  }

  public void setDatos(String empresa, String turno) {
    this.empresa = empresa;
    txtEmpresa.setText(empresa);
    this.turno = turno;

    double saldoFinal = 0;

    if (empresa.equals("MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.")) {
      empresa = "0001";
    } else {
      empresa = "0002";
    }

    LocalDate fecha = LocalDate.now();

    List<Object[]> cuenta =
        servicio.traerCuentaCajero(
            LoginController.usuarioLoggeado, fecha.toString(), 1, turno, empresa);

    if (cuenta.size() != 0) {
      for (Object[] fila : cuenta) {
        saldoFinal = Double.parseDouble(String.valueOf(fila[3]));
        break;
      }
    } else {
      txtAjuste.setDisable(true);
      txtSaldoFin.setDisable(true);
      txtSaldoFis.setDisable(true);
      btnRegistrar.setDisable(true);
    }

    NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
    String valorFormateado = formatoMoneda.format(saldoFinal);

    txtSaldoFin.setText(valorFormateado);

    if (saldoFinal == 0) {
      btnRegistrar.setDisable(true);
      txtSaldoFis.setDisable(true);
    }
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    //    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //    alert.setTitle("CIERRE DE VENTANA");
    //    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    //    alert.setContentText(
    //        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
    //            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
    //
    //    Optional<ButtonType> result = alert.showAndWait();
    //    if (result.isPresent() && result.get() == ButtonType.OK) {
    //
    //    }
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      //      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      //      alert.setTitle("CIERRE DE VENTANA");
      //      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
      //      alert.setContentText(
      //          "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
      //              + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
      //
      //      Optional<ButtonType> result = alert.showAndWait();
      //      if (result.isPresent() && result.get() == ButtonType.OK) {
      //
      //      }
      validator = new Validator();
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void cerrarConBoton() {
    //    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //    alert.setTitle("CIERRE DE VENTANA");
    //    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    //    alert.setContentText(
    //        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
    //            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
    //
    //    Optional<ButtonType> result = alert.showAndWait();
    //    if (result.isPresent() && result.get() == ButtonType.OK) {
    //
    //    }
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }
}
