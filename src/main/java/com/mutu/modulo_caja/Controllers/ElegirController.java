package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelCapitalSocial;
import com.mutu.modulo_caja.Models.ModelPrevisionSocial;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class ElegirController implements Initializable {

  private String titulo;

  private String opcion, nombre, usuario, turno;

  private int socio;

  public CajeroController cajeroController;

  @FXML private TableView tablePrestamos;

  @FXML private TableColumn<Object[], String> colCredito, colTipo, colFC, colMonto, colEmpresa;

  @FXML private Label lblTitulo;

  @FXML private ComboBox cmbEmpresa;

  @Autowired public Servicio servicio;

  @FXML
  public void cambiarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      cambiarConBoton();
    } else if (event.getCode().equals(KeyCode.CONTROL)) {
      Stage ventanaActual = (Stage) lblTitulo.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void cambiarConBoton() {
    try {
      Object[] selectedRow = (Object[]) tablePrestamos.getSelectionModel().getSelectedItem();

      if (selectedRow == null && !tablePrestamos.getItems().isEmpty()) {
        selectedRow = (Object[]) tablePrestamos.getItems().get(0);
      } else if (selectedRow == null && tablePrestamos.getItems().isEmpty()) {
        return;
      }

      String numSocio = String.valueOf(selectedRow[5]);
      String nomSocio = (String) selectedRow[6];
      String numCredito = String.valueOf(selectedRow[0]);
      String plazos = String.valueOf(selectedRow[8]);
      String tasa = String.valueOf(selectedRow[9]);
      String mora = String.valueOf(selectedRow[10]);
      String iva = String.valueOf(selectedRow[11]);
      String tipoCredito = (String) selectedRow[12];
      String codigoSistema = (String) selectedRow[1];
      String fechaDesembolso = (String) selectedRow[2];
      String capital = String.valueOf(selectedRow[3]);
      boolean bonifAplicable = (Boolean) selectedRow[13];

      Stage ventanaActual = (Stage) lblTitulo.getScene().getWindow();
      Stage nuevaVentana = new Stage();
      FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/abonoCredito.fxml"));
      fxml.setControllerFactory(Main.context::getBean);
      Scene nuevaEscena = new Scene(fxml.load());
      CreditoController controlador = fxml.getController();
      controlador.setDatos(
          numSocio,
          nomSocio,
          numCredito,
          plazos,
          tasa,
          mora,
          iva,
          tipoCredito,
          codigoSistema,
          fechaDesembolso,
          capital,
          bonifAplicable,
          3,
          turno);
      nuevaEscena
          .getStylesheets()
          .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
      JMetro jMetro = new JMetro(Style.LIGHT);
      // jMetro.setScene(nuevaEscena);
      nuevaVentana.setTitle("INICIO - CAJERO");
      Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
      nuevaVentana.getIcons().add(icon);
      nuevaVentana.setScene(nuevaEscena);
      nuevaVentana.setResizable(false);
      nuevaVentana.centerOnScreen();
      nuevaVentana.initModality(Modality.APPLICATION_MODAL);
      nuevaVentana.show();
      ventanaActual.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void cambiar() {

    if (opcion.equals("CS")) {
      double monto = 0;

      boolean validador = true;

      List<ModelCapitalSocial> cuentas = servicio.traerCuentasCS(socio);

      if (cuentas.isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL");
        alert.setHeaderText("ERROR EN EL PAGO DE CAPITAL SOCIAL");
        alert.setContentText("EL SOCIO NO TIENE UNA CUENTA EN NINGUNA EMPRESA.");
        alert.showAndWait();
        validador = false;
      }

      if (cuentas.size() == 1) {
        String empresaCod = cuentas.getFirst().getEmpresa_cod();
        if (empresaCod.equals("0001")) {
          empresaCod = "MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.";
        } else {
          empresaCod = "NUEVA GENERACIÓN DE UMÁN, AC.";
        }
        if (empresaCod.equals(cmbEmpresa.getSelectionModel().getSelectedItem().toString())) {
          double montoCubierto = cuentas.getFirst().getMonto_cubierto();
          if (montoCubierto >= 1000) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL");
            alert.setHeaderText("ERROR EN EL PAGO DE CAPITAL SOCIAL");
            alert.setContentText(
                    "LA CUENTA A LA QUE INTENTA ACCEDER YA TIENE EL MONTO CUBIERTO EN " + empresaCod);
            alert.showAndWait();
            validador = false;
          } else {
            monto = montoCubierto;
          }
        } else {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL");
          alert.setHeaderText("ERROR EN EL PAGO DE CAPITAL SOCIAL");
          alert.setContentText("EL SOCIO NO TIENE UNA CUENTA EN LA EMPRESA SELECCIONADA.");
          alert.showAndWait();
          validador = false;
        }
      } else if (cuentas.size() == 2) {
        String primer_empresa = cuentas.get(0).getEmpresa_cod();
        String segunda_empresa = cuentas.get(1).getEmpresa_cod();

        if (primer_empresa.equals("0001")) {
          primer_empresa = "MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.";
        }

        if (segunda_empresa.equals("0002")) {
          segunda_empresa = "NUEVA GENERACIÓN DE UMÁN, AC.";
        }

        if (primer_empresa.equals(cmbEmpresa.getSelectionModel().getSelectedItem().toString())) {
          double montoCubierto = cuentas.get(0).getMonto_cubierto();
          if (montoCubierto >= 1000) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL");
            alert.setHeaderText("ERROR EN EL PAGO DE CAPITAL SOCIAL");
            alert.setContentText(
                    "LA CUENTA A LA QUE INTENTA ACCEDER YA TIENE EL MONTO CUBIERTO EN " + primer_empresa);
            alert.showAndWait();
            validador = false;
          } else {
            monto = montoCubierto;
          }
        } else if (segunda_empresa.equals(
            cmbEmpresa.getSelectionModel().getSelectedItem().toString())) {
          double montoCubierto = cuentas.get(1).getMonto_cubierto();
          if (montoCubierto >= 1000) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL");
            alert.setHeaderText("ERROR EN EL PAGO DE CAPITAL SOCIAL");
            alert.setContentText(
                    "LA CUENTA A LA QUE INTENTA ACCEDER YA TIENE EL MONTO CUBIERTO EN " + segunda_empresa);
            alert.showAndWait();
            validador = false;
          } else {
            monto = montoCubierto;
          }
        }
      }

      if (validador) {
        try {
          Stage ventanaActual = (Stage) lblTitulo.getScene().getWindow();
          Stage nuevaVentana = new Stage();
          FXMLLoader fxml =
              new FXMLLoader(getClass().getResource("/com/java/fx/capitalSocial.fxml"));
          fxml.setControllerFactory(Main.context::getBean);
          Scene nuevaEscena = new Scene(fxml.load());
          CapitalSocialController controlador = fxml.getController();
          controlador.obtenerDatos(
              cmbEmpresa.getSelectionModel().getSelectedItem().toString(),
              nombre,
              monto,
              socio,
              usuario);
          nuevaEscena
              .getStylesheets()
              .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
          JMetro jMetro = new JMetro(Style.LIGHT);
          jMetro.setScene(nuevaEscena);
          nuevaVentana.setTitle("PAGO DE CAPITAL SOCIAL");
          Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
          nuevaVentana.getIcons().add(icon);
          nuevaVentana.setScene(nuevaEscena);
          nuevaVentana.setResizable(false);
          nuevaVentana.centerOnScreen();
          nuevaVentana.initModality(Modality.APPLICATION_MODAL);
          nuevaVentana.show();
          ventanaActual.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    } else if (opcion.equals("TRAS")) {
      try {
        Stage ventanaActual = (Stage) lblTitulo.getScene().getWindow();
        Stage nuevaVentana = new Stage();
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/traslados.fxml"));
        fxml.setControllerFactory(Main.context::getBean);
        Scene nuevaEscena = new Scene(fxml.load());
        TrasladoController controlador = fxml.getController();
        controlador.setEmpresa(
            cmbEmpresa.getSelectionModel().getSelectedItem().toString().trim(),
            usuario,
            turno,
            cajeroController);
        nuevaEscena
            .getStylesheets()
            .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(nuevaEscena);
        nuevaVentana.setTitle("TRASLADOS");
        Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
        nuevaVentana.getIcons().add(icon);
        nuevaVentana.setScene(nuevaEscena);
        nuevaVentana.setResizable(false);
        nuevaVentana.centerOnScreen();
        nuevaVentana.initModality(Modality.APPLICATION_MODAL);
        nuevaVentana.show();
        ventanaActual.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (opcion.equals("PRESOC")) {
      String empresaCod = "";
      boolean validador = true;
      String mensajeError = "";

      if (cmbEmpresa
          .getSelectionModel()
          .getSelectedItem()
          .toString()
          .equals("MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.")) {
        empresaCod = "0001";
      } else {
        empresaCod = "0002";
      }

      ModelPrevisionSocial resultado = servicio.traerCuentaPS(socio, empresaCod);

      if (resultado != null) {
        if (resultado.getPrevision() == 200 && resultado.getMontoAsignado() == 3500) {
          mensajeError = "EL SOCIO YA TIENE CUBIERTA SU PREVISIÓN SOCIAL EN ESTA EMPRESA.";
          validador = false;
        }
      } else {
        mensajeError = "EL SOCIO NO TIENE UNA CUENTA DE PREVISIÓN SOCIAL EN ESTA EMPRESA.";
        validador = false;
      }

      if (validador) {
        try {
          Stage ventanaActual = (Stage) lblTitulo.getScene().getWindow();
          Stage nuevaVentana = new Stage();
          FXMLLoader fxml =
              new FXMLLoader(getClass().getResource("/com/java/fx/previsionSocial.fxml"));
          fxml.setControllerFactory(Main.context::getBean);
          Scene nuevaEscena = new Scene(fxml.load());
          PrevisionController controlador = fxml.getController();
          controlador.obtenerDatos(
              cmbEmpresa.getSelectionModel().getSelectedItem().toString(),
              usuario,
              socio,
              nombre,
              resultado.getPrevision());
          nuevaEscena
              .getStylesheets()
              .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
          JMetro jMetro = new JMetro(Style.LIGHT);
          jMetro.setScene(nuevaEscena);
          nuevaVentana.setTitle("PAGO DE PREVISIÓN SOCIAL");
          Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
          nuevaVentana.getIcons().add(icon);
          nuevaVentana.setScene(nuevaEscena);
          nuevaVentana.setResizable(false);
          nuevaVentana.centerOnScreen();
          nuevaVentana.initModality(Modality.APPLICATION_MODAL);
          nuevaVentana.show();
          ventanaActual.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER PAGAR LA PREVISIÓN SOCIAL");
        alert.setHeaderText("ERROR EN EL PAGO DE PREVISIÓN SOCIAL");
        alert.setContentText(mensajeError);
        alert.showAndWait();
      }
    }
  }

  public void setTitulo(String tituloEleccion) {
    this.titulo = tituloEleccion;
    lblTitulo.setText(titulo);
  }

  public void setOpcion(String opcion) {
    this.opcion = opcion;
  }

  public void setSocio(int socio, String turno) {
    this.socio = socio;
    this.turno = turno;
    if (tablePrestamos != null) {
      colCredito.setCellValueFactory(
          cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[0])));
      colTipo.setCellValueFactory(
          cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[1])));
      colFC.setCellValueFactory(
          cellData -> new SimpleStringProperty(cellData.getValue()[2].toString()));

      NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
      colMonto.setCellValueFactory(
          cellData -> {
            BigDecimal monto = (BigDecimal) cellData.getValue()[3];
            return new SimpleStringProperty(currencyFormat.format(monto));
          });

      colEmpresa.setCellValueFactory(
          cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[4])));
      traerCoincidencias(socio);
    }
  }

  public void setSocioCapitalSocial(int socio, String nombre, String usuario) {
    this.socio = socio;
    this.nombre = nombre;
    this.usuario = usuario;
  }

  public void setUsuarioTralado(String usuario, String turno, CajeroController controller) {
    this.cajeroController = controller;
    this.turno = turno;
    this.usuario = usuario;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (cmbEmpresa != null) {
      ObservableList<String> empresas = FXCollections.observableArrayList();
      empresas.addAll(
          "MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.", "NUEVA GENERACIÓN DE UMÁN, AC.");
      cmbEmpresa.setItems(empresas);
      cmbEmpresa.getSelectionModel().selectFirst();
    }
  }

  public void traerCoincidencias(int socio) {
    List<Object[]> resultados = servicio.consultarCreditos(socio, 2);

    ObservableList<Object[]> socios = FXCollections.observableArrayList();

    for (Object[] resultado : resultados) {
      socios.add(resultado);
    }

    tablePrestamos.setItems(socios);
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      Stage ventanaActual = (Stage) lblTitulo.getScene().getWindow();
      ventanaActual.close();
    }
    if (event.getCode().equals(KeyCode.ENTER)) {
      cambiar();
    }
  }



  @FXML
  public void cerrarConBoton() {
    Stage ventanaActual = (Stage) lblTitulo.getScene().getWindow();
    ventanaActual.close();
  }
}
