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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class OperacionesController implements Initializable {

  @FXML private ComboBox cmbEmpresa, cmbOperacion, cmbCajero;
  @Autowired public Servicio servicio;
  @FXML private TableView tablaOperaciones;
  @FXML private TableView tableTraslados;

  @FXML private TableColumn<Object[], String> colTrasID, colTrasUser, colTrasOrigen,
          colTrasDestino, colTrasMonto, colTrasFecha;
  @FXML private TableColumn<Object[], String> colSocio, colNombre, colFecha,
          colHora, colOperacion, colMonto;

  // Mapa centralizado para resolución de tipo de operación
  /** Mapeo operación (label) → tipo_operacion (id). */
    public Map<String, Integer> OPERACIONES_MAP;


  String turno = "";

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {


    OPERACIONES_MAP = servicio.traerOperaciones()
            .stream()
            .collect(Collectors.toMap(
                    op -> op.getOperacion().toUpperCase(),
                    ModelOperaciones::getId
            ));

    // Empresas
    List<ModelEmpresa> empresas = servicio.traerEmpresas();
    ObservableList<String> empresasProcesadas = FXCollections.observableArrayList();
    for (ModelEmpresa empresa : empresas) {
      empresasProcesadas.add(empresa.getRazonSocial());
    }
    cmbEmpresa.setItems(empresasProcesadas);
    cmbEmpresa.getSelectionModel().selectFirst();

    // Operaciones
    List<ModelOperaciones> operaciones = servicio.traerOperacions();
    ObservableList<String> operacionesProcesadas = FXCollections.observableArrayList();
    for (ModelOperaciones operacion : operaciones) {
      operacionesProcesadas.add(operacion.getOperacion().toUpperCase());
    }
    cmbOperacion.setItems(operacionesProcesadas);
    cmbOperacion.getSelectionModel().selectFirst();

    // Cajeros
    List<ModelUsuario> usuarios = servicio.traerCajero(1);
    ObservableList<String> cajeros = FXCollections.observableArrayList();
    for (ModelUsuario usuario : usuarios) {
      cajeros.add(usuario.getUsuario().toUpperCase());
    }
    cmbCajero.setItems(cajeros);
    cmbCajero.getSelectionModel().selectFirst();

    configurarColumnas();

    Platform.runLater(() -> {
      Stage stage = (Stage) cmbEmpresa.getScene().getWindow();
      stage.setOnCloseRequest(event -> cierreDeVentana(event));
    });
  }

  // Extracción: configuración de columnas separada del initialize
  private void configurarColumnas() {
    colSocio.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[0])));
    colNombre.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[1])));
    colFecha.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[2])));
    colHora.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[6]));
    colOperacion.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[4])));
    colMonto.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[3]));

    colTrasID.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[0])));
    colTrasUser.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[1])));
    colTrasOrigen.setCellValueFactory(cd -> resolverCeldaFlexible(cd.getValue()[5]));
    colTrasDestino.setCellValueFactory(cd -> resolverCeldaFlexible(cd.getValue()[6]));
    colTrasMonto.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[3])));
    colTrasFecha.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue()[7])));
  }

  // Extracción: lógica repetida de tipo Number/String en celdas
  private SimpleStringProperty resolverCeldaFlexible(Object valor) {
    if (valor instanceof Number) return new SimpleStringProperty(String.valueOf(valor));
    if (valor instanceof String) return new SimpleStringProperty((String) valor);
    return new SimpleStringProperty("");
  }


  private int getTipoOperacion() {
    return OPERACIONES_MAP.getOrDefault(
            cmbOperacion.getSelectionModel().getSelectedItem().toString(), 0);
  }

  private boolean esTraslado(int tipo_operacion) {
    return tipo_operacion == 6 || tipo_operacion == 7;
  }

  @FXML
  public void traerOperaciones() {
    String fechaEnviar = servicio.traerFechaHoy().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    String empresa = servicio.traerEmpresaConRS(cmbEmpresa.getSelectionModel().getSelectedItem().toString()).getCodigo();
    int usuario_id = servicio.traerDatosUsuario(
            cmbCajero.getSelectionModel().getSelectedItem().toString()).getId();
    int tipo_operacion = getTipoOperacion();

    List<Object[]> operaciones;
    if (esTraslado(tipo_operacion)) {
      operaciones = servicio.historialTraslados(usuario_id, tipo_operacion, 1, fechaEnviar, turno, empresa);
      tablaOperaciones.setVisible(false);
      tableTraslados.setVisible(true);
    } else {
      operaciones = servicio.traerHistorial(fechaEnviar, 1, empresa, turno, usuario_id, tipo_operacion, 1);
      tablaOperaciones.setVisible(true);
      tableTraslados.setVisible(false);
    }

    ObservableList<Object[]> datosOperaciones = FXCollections.observableArrayList();
    for (Object[] resultado : operaciones) {
      if ((int) resultado[4] == tipo_operacion) {
        datosOperaciones.add(resultado);
      }
    }

    (tablaOperaciones.isVisible() ? tablaOperaciones : tableTraslados).setItems(datosOperaciones);
  }

  public void setDatos(String turno) {
    this.turno = turno;
    traerOperaciones();
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    ((Stage) cmbEmpresa.getScene().getWindow()).close();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      ((Stage) cmbEmpresa.getScene().getWindow()).close();
    }
  }

  @FXML
  public void cerrarConBoton() {
    ((Stage) cmbEmpresa.getScene().getWindow()).close();
  }

  @FXML
  public void verOperacion() {
    int tipo_operacion = getTipoOperacion();
    String empresa = servicio.traerEmpresaConRS(cmbEmpresa.getSelectionModel().getSelectedItem().toString()).getCodigo();
    boolean esTraslado = esTraslado(tipo_operacion);

    boolean historialVisible  = tablaOperaciones.isVisible();
    Object[] fila             = (Object[]) (historialVisible
                ? tablaOperaciones.getSelectionModel().getSelectedItem()
                : tableTraslados.getSelectionModel().getSelectedItem());

    System.out.println(historialVisible);
    if (fila == null) {
      mostrarError("NO HA SELECCIONADO NINGUNA OPERACIÓN",
              "SIN OPERACIÓN SELECCIONADA",
              "POR FAVOR, SELECCIONE UNA OPERACIÓN.");
      return;
    }
    // Variables de traslado
    String id_traslado = "", cuenta_origen = " ", cuenta_destino = " ",
            nombre_usuario = "", monto = "", fecha = "";

    // Variables de operación normal
    String numero_socio = "", id_transaccion = "", nombre_socio = "", hora = "";
    String capital_pagado = "", interes_pagado = "", mora_pagada = "",
            iva_pagado = "", saldo_credito = "", tipo_credito = "",
            cuota_afectada = "", bonif_aplicada = "";

    if (esTraslado) {
      Object[] selectedRow = (Object[]) tableTraslados.getSelectionModel().getSelectedItem();
      if (selectedRow != null) {
        cuenta_origen = (String) selectedRow[5];
        cuenta_destino = (String) selectedRow[6];
        id_traslado = String.valueOf(selectedRow[0]);
        monto = (String) selectedRow[3];
        fecha = (String) selectedRow[7];
        nombre_usuario = (String) selectedRow[1];
      }
    } else {
      Object[] selectedRow = (Object[]) tablaOperaciones.getSelectionModel().getSelectedItem();
      if (selectedRow != null) {
        nombre_usuario = cmbCajero.getSelectionModel().getSelectedItem().toString();
        numero_socio = String.valueOf(selectedRow[0]);
        id_transaccion = String.valueOf(selectedRow[11]);
        monto = (String) selectedRow[3];
        fecha = (String) selectedRow[2];
        nombre_socio = (String) selectedRow[1];
        hora = (String) selectedRow[6];

        if (tipo_operacion == 2) {
          capital_pagado  = (String) selectedRow[13];
          interes_pagado  = (String) selectedRow[14];
          mora_pagada     = (String) selectedRow[15];
          iva_pagado      = (String) selectedRow[16];
          saldo_credito   = (String) selectedRow[18];
          tipo_credito    = (String) selectedRow[19];
          cuota_afectada  = String.valueOf(Integer.parseInt(selectedRow[20].toString()));
          bonif_aplicada  = (String) selectedRow[17];
        }
      }
    }

    try {
      Stage nuevaVentana = new Stage();
      FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/verOperacion.fxml"));
      fxml.setControllerFactory(Main.context::getBean);
      Scene nuevaEscena = new Scene(fxml.load());
      ReimpresionController controller = fxml.getController();

      if (!esTraslado) {
        controller.setDatosCancelacion(
                numero_socio, id_transaccion,
                cmbOperacion.getSelectionModel().getSelectedItem().toString(),
                cmbEmpresa.getSelectionModel().getSelectedItem().toString(),
                fecha, monto, nombre_socio, tipo_operacion, turno, nombre_usuario,
                this, hora, capital_pagado, interes_pagado, mora_pagada,
                iva_pagado, bonif_aplicada, saldo_credito, tipo_credito, cuota_afectada);
      } else {
        controller.setDatosTraslados(
                id_traslado, nombre_usuario, monto,
                cmbOperacion.getSelectionModel().getSelectedItem().toString(),
                cuenta_origen, cuenta_destino, fecha, tipo_operacion, this, turno);
      }

      nuevaEscena.getStylesheets().add(
              getClass().getResource("/assets/css/estilos.css").toExternalForm());
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

  private void mostrarError(String titulo, String header, String contenido) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(header);
    alert.setContentText(contenido);
    alert.showAndWait();
  }

  @FXML
  public void detectarClicks(MouseEvent event) {
    if (event.getClickCount() == 2) {
      verOperacion();
    }
  }
}