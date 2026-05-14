package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelCapitalSocial;
import com.mutu.modulo_caja.Models.ModelEmpresa;
import com.mutu.modulo_caja.Models.ModelPrevisionSocial;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class ElegirController implements Initializable {


  // ─── Estado ───────────────────────────────────────────────────────────────

  private String titulo, opcion, nombre, usuario, turno;
  private int socio;
  public CajeroController cajeroController;

  // ─── FXML ─────────────────────────────────────────────────────────────────

  @FXML private TableView<Object[]>              tablePrestamos;
  @FXML private TableColumn<Object[], String>    colCredito, colTipo, colFC, colMonto, colEmpresa;
  @FXML private Label                            lblTitulo;
  @FXML private ComboBox<String>                 cmbEmpresa;

  @Autowired public Servicio servicio;

  // ─── Initializable ────────────────────────────────────────────────────────

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (cmbEmpresa != null) {

      List<ModelEmpresa> empresas = servicio.traerEmpresas();
      for (ModelEmpresa empresa : empresas) {
        cmbEmpresa.getItems().add(empresa.getRazonSocial());
      }
      cmbEmpresa.getSelectionModel().selectFirst();
    }
  }

  // ─── Setters públicos ─────────────────────────────────────────────────────

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
    if (tablePrestamos == null) return;

    NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);

    colCredito.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[0])));
    colTipo   .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[1])));
    colFC     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[2].toString()));
    colMonto  .setCellValueFactory(c -> new SimpleStringProperty(fmt.format((BigDecimal) c.getValue()[3])));
    colEmpresa.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[4])));

    traerCoincidencias(socio);
  }

  public void setSocioCapitalSocial(int socio, String nombre, String usuario) {
    this.socio   = socio;
    this.nombre  = nombre;
    this.usuario = usuario;
  }

  public void setUsuarioTraslado(String usuario, String turno, CajeroController controller, String opcion) {
    this.cajeroController = controller;
    this.turno            = turno;
    this.opcion = opcion;
    this.usuario          = usuario;
  }

  // ─── Datos ────────────────────────────────────────────────────────────────

  public void traerCoincidencias(int socio) {
    List<Object[]> resultados = servicio.consultarCreditos(socio, 2);
    tablePrestamos.setItems(FXCollections.observableArrayList(resultados));
  }

  // ─── Eventos de teclado / botones ─────────────────────────────────────────

  @FXML
  public void cambiarConTecla(KeyEvent event) {
    if      (event.getCode() == KeyCode.ENTER)   cambiarConBoton();
    else if (event.getCode() == KeyCode.CONTROL) cerrarVentanaActual();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) cerrarVentanaActual();
    if (event.getCode() == KeyCode.ENTER)  cambiar();
  }

  @FXML
  public void cerrarConBoton() {
    cerrarVentanaActual();
  }

  // ─── Navegación a crédito ─────────────────────────────────────────────────

  @FXML
  public void cambiarConBoton() {
    Object[] fila = (Object[]) tablePrestamos.getSelectionModel().getSelectedItem();

    if (fila == null && !tablePrestamos.getItems().isEmpty()) {
      fila = tablePrestamos.getItems().get(0);
    } else if (fila == null) {
      return;
    }

    try {
      System.out.println(String.valueOf(fila[0]));
      FXMLLoader fxml = cargarFxml("/com/java/fx/abonoCredito.fxml");
      Parent  root = fxml.load();
      CreditoController controlador = fxml.getController();
      controlador.setDatos(Integer.parseInt(String.valueOf(fila[0])));
      abrirVentana(root, "ABONO A CRÉDITO", true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // ─── Navegación por opción ────────────────────────────────────────────────

  @FXML
  public void cambiar() {
    switch (opcion) {
      case "CS"     -> procesarCapitalSocial();
      case "TRAS"   -> procesarTraslado();
      case "PRESOC" -> procesarPrevisionSocial();
    }
  }

  // ─── Opciones ─────────────────────────────────────────────────────────────

  private void procesarCapitalSocial() {
    List<ModelCapitalSocial> cuentas = servicio.traerCuentasCS(socio);

    if (cuentas.isEmpty()) {
      mostrarError("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL",
              "ERROR EN EL PAGO DE CAPITAL SOCIAL",
              "EL SOCIO NO TIENE UNA CUENTA EN NINGUNA EMPRESA.");
      return;
    }

    String empresaSeleccionada = empresaSeleccionada();
    double monto               = 0;

    for (ModelCapitalSocial cuenta : cuentas) {
      String nomEmpresa = codigoANombre(cuenta.getEmpresa_cod());
      if (!nomEmpresa.equals(empresaSeleccionada)) continue;

      if (cuenta.getMonto_cubierto() >= 1000) {
        mostrarError("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL",
                "ERROR EN EL PAGO DE CAPITAL SOCIAL",
                "LA CUENTA A LA QUE INTENTA ACCEDER YA TIENE EL MONTO CUBIERTO EN " + nomEmpresa);
        return;
      }
      monto = cuenta.getMonto_cubierto();
      break;
    }

    // Si ninguna cuenta coincide con la empresa seleccionada
    boolean encontrada = cuentas.stream()
            .anyMatch(c -> codigoANombre(c.getEmpresa_cod()).equals(empresaSeleccionada));
    if (!encontrada) {
      mostrarError("ERROR AL QUERER PAGAR EL CAPITAL SOCIAL",
              "ERROR EN EL PAGO DE CAPITAL SOCIAL",
              "EL SOCIO NO TIENE UNA CUENTA EN LA EMPRESA SELECCIONADA.");
      return;
    }

    try {
      FXMLLoader fxml = cargarFxml("/com/java/fx/capitalSocial.fxml");
      Parent  root = fxml.load();
      CapitalSocialController controlador = fxml.getController();
      controlador.obtenerDatos(empresaSeleccionada, nombre, monto, socio, usuario);
      abrirVentana(root, "PAGO DE CAPITAL SOCIAL", true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void procesarTraslado() {
    try {
      FXMLLoader fxml = cargarFxml("/com/java/fx/traslados.fxml");
      Parent  root = fxml.load();
      TrasladoController controlador = fxml.getController();
      controlador.setEmpresa(empresaSeleccionada().trim(), usuario, turno, cajeroController);
      abrirVentana(root, "TRASLADOS", true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void procesarPrevisionSocial() {
    String empresaCod         = servicio.traerEmpresaConRS(cmbEmpresa.getSelectionModel().getSelectedItem().toString()).getCodigo();
    ModelPrevisionSocial res  = servicio.traerCuentaPS(socio, empresaCod);

    if (res == null) {
      mostrarError("ERROR AL QUERER PAGAR LA PREVISIÓN SOCIAL",
              "ERROR EN EL PAGO DE PREVISIÓN SOCIAL",
              "EL SOCIO NO TIENE UNA CUENTA DE PREVISIÓN SOCIAL EN ESTA EMPRESA.");
      return;
    }

    if (res.getPrevision() == 200 && res.getMontoAsignado() == 3500) {
      mostrarError("ERROR AL QUERER PAGAR LA PREVISIÓN SOCIAL",
              "ERROR EN EL PAGO DE PREVISIÓN SOCIAL",
              "EL SOCIO YA TIENE CUBIERTA SU PREVISIÓN SOCIAL EN ESTA EMPRESA.");
      return;
    }

    try {
      FXMLLoader fxml = cargarFxml("/com/java/fx/previsionSocial.fxml");
      Parent  root = fxml.load();
      PrevisionController controlador = fxml.getController();
      controlador.obtenerDatos(empresaSeleccionada(), usuario, socio, nombre, res.getPrevision());
      abrirVentana(root, "PAGO DE PREVISIÓN SOCIAL", true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // ─── Helpers privados ─────────────────────────────────────────────────────

  /** Cierra la ventana raíz de lblTitulo. */
  private void cerrarVentanaActual() {
    ((Stage) lblTitulo.getScene().getWindow()).close();
  }

  /** Empresa actualmente seleccionada en el ComboBox. */
  private String empresaSeleccionada() {
    return cmbEmpresa.getSelectionModel().getSelectedItem();
  }

  /** Convierte código de empresa a nombre legible. */
  private String codigoANombre(String codigo) {
    return servicio.traerEmpresa(codigo).getRazonSocial();
  }

  /**
   * Carga un FXML con el factory de Spring sin hacer load() todavía,
   * para que el caller pueda obtener el controller antes de cargar la escena.
   */
  private FXMLLoader cargarFxml(String ruta) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
    loader.setControllerFactory(Main.context::getBean);
    return loader;
  }

  /**
   * Muestra una nueva ventana modal con la escena dada.
   * Cierra la ventana actual si {@code cerrarActual} es true.
   */
  private void abrirVentana(Parent root, String titulo, boolean cerrarActual) {
    Scene escena = new Scene(root);
    escena.getStylesheets().add(getClass().getResource("/assets/css/estilos.css").toExternalForm());

//    new JMetro(Style.LIGHT).setScene(escena);

    Stage nueva = new Stage();
    nueva.setTitle(titulo);
    nueva.getIcons().add(new Image(getClass().getResourceAsStream("/assets/images/logo.png")));
    nueva.setScene(escena);
    nueva.setResizable(false);
    nueva.centerOnScreen();
    nueva.initModality(Modality.APPLICATION_MODAL);
    nueva.show();

    if (cerrarActual) cerrarVentanaActual();
  }

  private void mostrarError(String titulo, String header, String contenido) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(header);
    alert.setContentText(contenido);
    alert.showAndWait();
  }
}