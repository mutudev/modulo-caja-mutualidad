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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class HistorialController implements Initializable {

  // ─── Constantes ───────────────────────────────────────────────────────────

  private static final DateTimeFormatter FORMATTER_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  /** Mapeo operación (label) → tipo_operacion (id). */
  private static final Map<String, Integer> OPERACION_ID = Map.of(
          "ABONO A CUENTA DE AHORRO",           1,
          "ABONO A CUENTA DE CRÉDITO",          2,
          "PROCESAMIENTO DE DESEMBOLSO",         3,
          "PROCESAMIENTO DE RETIRO",             4,
          "ABONO A CUENTA DE CAPITAL SOCIAL",   5,
          "TRASLADO DE BÓVEDA A CAJAS",          6,
          "TRASLADO DE CAJAS A BÓVEDA",          7,
          "ABONO A CUENTA DE PREVISIÓN SOCIAL", 10
  );

  // ─── FXML ─────────────────────────────────────────────────────────────────

  @FXML private ComboBox<String> cmbEmpresa, cmbOperacion;
  @FXML private Button          btnCancelar, btnVer;

  @FXML private TableView<Object[]> tableHistorial, tableTraslados;

  @FXML private TableColumn<Object[], String> colSocio, colNombre, colFecha, colHora, colMonto;
  @FXML private TableColumn<Object[], String> colID, colUsuario, colOrigen, colDestino, colMontoT, colFechaT;

  // ─── Estado ───────────────────────────────────────────────────────────────

  public int    operacionreimpresion;
  public String usuario, turno;

  List<Object[]> CopiaOperaciones;

  @Autowired public Servicio servicio;

  // ─── Initializable ────────────────────────────────────────────────────────

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (cmbEmpresa != null && cmbOperacion != null) {
      List<String> empresaNames = servicio.traerEmpresas().stream()
              .map(ModelEmpresa::getRazonSocial)
              .toList();
      cmbEmpresa.setItems(FXCollections.observableArrayList(empresaNames));
      cmbEmpresa.getSelectionModel().selectFirst();

      List<String> operacionNames = servicio.traerOperacions().stream()
              .map(o -> o.getOperacion().toUpperCase())
              .toList();
      cmbOperacion.setItems(FXCollections.observableArrayList(operacionNames));
      cmbOperacion.getSelectionModel().selectFirst();
    }

    // Tabla historial
    colSocio .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[0])));
    colNombre.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[1])));
    colFecha .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[2])));
    colHora  .setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue()[6]));
    colMonto .setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue()[3]));

    // Tabla traslados
    colID     .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[0])));
    colUsuario.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[1])));
    colOrigen .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[5])));
    colDestino.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[6])));
    colMontoT .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[3])));
    colFechaT .setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue()[7]));

    Platform.runLater(() -> {
      Stage stage = (Stage) btnCancelar.getScene().getWindow();
      stage.setOnCloseRequest(this::cierreDeVentana);
    });
  }

  // ─── Datos ────────────────────────────────────────────────────────────────

  public void setDatos(String usuario, String turno) {
    this.usuario = usuario;
    this.turno   = turno;
    traerOperaciones();
  }

  @FXML
  public void traerOperaciones() {
    String fechaEnviar    = LocalDate.now().format(FORMATTER_FECHA);
    String empresa        = resolverCodigoEmpresa();
    int    usuario_id     = servicio.traerDatosUsuario(LoginController.usuarioLoggeado).getId();
    int    tipo_operacion = resolverTipoOperacion();

    boolean esTraslado = (tipo_operacion == 6 || tipo_operacion == 7);

    List<Object[]> operaciones = esTraslado
            ? servicio.historialTraslados(usuario_id, tipo_operacion, 1, fechaEnviar, turno, empresa)
            : servicio.traerHistorial(fechaEnviar, 1, empresa, turno, usuario_id, tipo_operacion, 1);

    tableHistorial.setVisible(!esTraslado);
    tableTraslados.setVisible(esTraslado);

    final int tipoFinal = tipo_operacion;
    ObservableList<Object[]> datos = FXCollections.observableArrayList(
            operaciones.stream()
                    .filter(r -> (int) r[4] == tipoFinal)
                    .toList()
    );

    if (esTraslado) tableTraslados.setItems(datos);
    else            tableHistorial.setItems(datos);
  }

  // ─── Ver / reimprimir operación ───────────────────────────────────────────

  @FXML
  public void verOperacion() {
    boolean historialVisible  = tableHistorial.isVisible();
    Object[] fila             = historialVisible
            ? tableHistorial.getSelectionModel().getSelectedItem()
            : tableTraslados.getSelectionModel().getSelectedItem();

    if (fila == null) {
      mostrarError("NO HA SELECCIONADO NINGUNA OPERACIÓN",
              "SIN OPERACIÓN SELECCIONADA",
              "POR FAVOR, SELECCIONE UNA OPERACIÓN.");
      return;
    }

    String operacion = cmbOperacion.getSelectionModel().getSelectedItem();
    String empresa   = cmbEmpresa.getSelectionModel().getSelectedItem();
    operacionreimpresion = resolverTipoOperacion();

    try {
      FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/verOperacion.fxml"));
      fxml.setControllerFactory(Main.context::getBean);
      Scene escena = new Scene(fxml.load());
      ReimpresionController controller = fxml.getController();

      if (historialVisible) {
        controller.setDatos(
                String.valueOf(fila[0]),   // numSocio
                String.valueOf(fila[11]),  // IdOperacion
                operacion, empresa,
                (String) fila[2],          // fecha
                (String) fila[3],          // monto
                (String) fila[1],          // nombre
                operacionreimpresion,
                (String) fila[6],          // hora
                (String) fila[13],         // capital_pagado
                (String) fila[14],         // interes_pagado
                (String) fila[15],         // mora_pagada
                (String) fila[16],         // iva_pagado
                (String) fila[17],         // bonif_aplicada
                (String) fila[18],         // saldo_credito
                (String) fila[19]          // tipo_credito
        );
      } else {
        boolean boveda = fila[4].equals("6");
        controller.setDatosTrasladosHistorial(
                String.valueOf(fila[0]),                          // IdOperacion
                (String) fila[1],                                 // nombre
                (String) fila[3],                                 // monto
                operacion,
                boveda ? (String) fila[6] : (String) fila[5],    // cuenta_origen
                boveda ? (String) fila[5] : (String) fila[6],    // cuenta_destino
                (String) fila[7],                                 // fecha
                operacionreimpresion,
                this, turno, empresa,
                (String) fila[8]                                  // hora
        );
      }

      escena.getStylesheets().add(getClass().getResource("/assets/css/estilos.css").toExternalForm());

      Stage nueva = new Stage();
      nueva.setTitle("REIMPRESIÓN DE OPERACIÓN");
      nueva.setScene(escena);
      nueva.setResizable(false);
      nueva.centerOnScreen();
      nueva.initModality(Modality.APPLICATION_MODAL);
      nueva.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void detectarClicks(MouseEvent event) {
    if (event.getClickCount() == 2) verOperacion();
  }

  // ─── Cierre de ventana ────────────────────────────────────────────────────

  public void cierreDeVentana(Event event) {
    event.consume();
    cerrarVentanaActual();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) cerrarVentanaActual();
    if (event.getCode() == KeyCode.ENTER)  verOperacion();
  }

  @FXML
  public void cerrarConBoton() {
    cerrarVentanaActual();
  }

  // ─── Helpers privados ─────────────────────────────────────────────────────

  private void cerrarVentanaActual() {
    ((Stage) btnCancelar.getScene().getWindow()).close();
  }

  /** Devuelve "0001" o "0002" según la empresa seleccionada. */
  private String resolverCodigoEmpresa() {
    return cmbEmpresa.getSelectionModel().getSelectedItem()
            .equals("MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.") ? "0001" : "0002";
  }

  /** Devuelve el tipo_operacion numérico según el ComboBox, o 0 si no se reconoce. */
  private int resolverTipoOperacion() {
    String label = cmbOperacion.getSelectionModel().getSelectedItem();
    return OPERACION_ID.getOrDefault(label, 0);
  }

  private void mostrarError(String titulo, String header, String contenido) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(header);
    alert.setContentText(contenido);
    alert.showAndWait();
  }
}