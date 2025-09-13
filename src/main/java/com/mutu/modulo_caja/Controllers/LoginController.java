package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
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
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class LoginController implements Initializable {

  @FXML private Button btnIngresar;

  @FXML private Button btnCancelar;

  @FXML private TextField txtUsuario;

  @FXML private ListView listUsuarios;

  @FXML private PasswordField txtPass;

  @FXML private Label lblHora;

  @FXML private Label lblFecha, lblValUser;

  public int rol = 0;

  private final Validator validator = new Validator();

  public static String usuarioLoggeado = "";

  @Autowired private Servicio usuarioService;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    // Validación del usuario
    validator
        .createCheck()
        .dependsOn("input", txtUsuario.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.isEmpty()) {
                c.error("El campo no puede estar vacío");
                lblValUser.setText("El campo no puede estar vacío");
              } else if (texto.matches(".*\\d.*")) {
                c.error("No se permiten números en este campo");
                lblValUser.setText("No se permiten números en este campo");
              } else if (texto.length() < 3) {
                c.error("El texto debe tener al menos 3 caracteres");
                lblValUser.setText("El texto debe tener al menos 3 caracteres");
              } else {
                lblValUser.setText("");
              }
            })
        .decorates(txtUsuario)
        .immediate();

    // Formateo a mayúsculas
    txtUsuario.setTextFormatter(
        new TextFormatter<>(
            change -> {
              change.setText(change.getText().toUpperCase());
              return change;
            }));

    iniciarReloj();

    LocalDateTime fecha = LocalDateTime.now();
    lblFecha.setText(
        "FECHA: " + fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear());

    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCancelar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE APLICATIVO");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR EL APLICATIVO?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  public void iniciarReloj() {
    Thread hiloReloj =
        new Thread(
            () -> {
              SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
              while (true) {
                try {
                  String horaActual = formatoHora.format(new Date());
                  Platform.runLater(() -> lblHora.setText("HORA: " + horaActual));
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            });
    hiloReloj.setDaemon(true);
    hiloReloj.start();
  }

  @FXML
  public void validarLogin() {
    Map<String, Object> result =
        usuarioService.validarLogin(txtUsuario.getText().trim(), txtPass.getText().trim(), "", 0);
    if (validator.validate() && result.get("Resultado").toString().equals("CORRECTO")) {
      try {
        rol = Integer.parseInt(result.get("Rol").toString());
        usuarioLoggeado = txtUsuario.getText().trim();
        Stage ventanaActual = (Stage) btnIngresar.getScene().getWindow();
        Stage nuevaVentana = new Stage();
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/InicioCajero.fxml"));
        fxml.setControllerFactory(Main.context::getBean);
        Scene nuevaEscena = new Scene(fxml.load());
        CajeroController controlador = fxml.getController();
        controlador.setUsuario(txtUsuario.getText().trim(), rol);
        nuevaEscena
            .getStylesheets()
            .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
        nuevaVentana.setTitle("INICIO - CAJERO");
        Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
        nuevaVentana.getIcons().add(icon);
        nuevaVentana.setScene(nuevaEscena);
        nuevaVentana.setResizable(false);
        nuevaVentana.centerOnScreen();
        nuevaVentana.show();
        ventanaActual.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL INTENTAR INICIAR SESIÓN");
      alert.setHeaderText(result.get("Resultado").toString().toUpperCase());
      alert.setContentText("ERROR CON SEVERIDAD: " + result.get("Rol"));
      alert.showAndWait();
    }
  }

  @FXML
  public void cerrarConBoton() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE APLICATIVO");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR EL APLICATIVO?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }
  @FXML
  public void EntrarConBoton(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      validarLogin();
    }
  }
}
