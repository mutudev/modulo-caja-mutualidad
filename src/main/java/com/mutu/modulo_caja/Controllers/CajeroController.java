package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelCaja;
import com.mutu.modulo_caja.Models.ModelUsuario;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
public class CajeroController implements Initializable {

  // Empiezan datos de control

  @FXML private Label lblHora, lblFecha, lblBienvenida, lblAbonoAhorro, lblAbonoCredito;
  @FXML private ImageView imgLogo, imgBusqueda;
  // Acaban datos de control

  // Empiezan declaraciones para funcionalidad
  @FXML private TextField txtNumeroSocio;

  @FXML
  private Label lblDesembolsos,
      lblRetiros,
      lblCapitalSocial,
      lblTraslados,
      lblHistorial;

  @FXML private GridPane gridOpciones;

  @FXML private Label lblCierreCajero, lblPrevisionSocial, lblCancelacion, lblValNumS;

  // Acaban declaraciones para funcionalidad

  // Empiezan datos del socio cargados dinámicamente
  @FXML private TextField txtNombreCargado, txtTipoCargado, txtNumCargado;
  @FXML private Label lblNomCargado, lblTipoCargado, lblNumCargado, lblDatosCargados, lblCambio;
  // Acaban datos del socio cargados dinámicamente

  // Empieza declaración de variables
  public String usuario;
  public int rolUsuario;
  public Validator validator = new Validator();
  private Map<String, Label> labelMap = new HashMap<>();
  public boolean apertura = false;
  public String turno = "";
  public static double bufferOperaciones = 0;

  @Autowired private Servicio servicio;

  // EMPIEZA MÉTODO INICIAL
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    iniciarReloj();
    LocalDateTime fecha = LocalDateTime.now();
    lblFecha.setText(
            "FECHA: " + fecha.getDayOfMonth() + "/"
                    + fecha.getMonthValue() + "/"
                    + fecha.getYear());

    lblBienvenida.setText("¡BIENVENIDO, " + usuario + "!");


    validator
        .createCheck()
        .dependsOn("input", txtNumeroSocio.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.isEmpty()) {
                c.error("El campo no puede estar vacío");
                lblValNumS.setText("El campo no puede estar vacío");
              } else if (!texto.matches("\\d+")) {
                c.error("Solo se permiten números en este campo");
                lblValNumS.setText("Solo se permiten números en este campo");
              } else if (Integer.parseInt(texto) == 0) {
                c.error("El campo no puede ser 0");
                lblValNumS.setText("El campo no puede ser 0");
              } else {
                lblValNumS.setText("");
              }
            })
        .decorates(txtNumeroSocio)
        .immediate();

    txtNumeroSocio.setTextFormatter(
        new TextFormatter<>(
            change -> {
              change.setText(change.getText().replaceAll("[^0-9]", ""));
              if (change.getControlNewText().length() > 5) {
                return null;
              }
              return change;
            }));

    Platform.runLater(
        () -> {
          Stage stage = (Stage) lblHora.getScene().getWindow();
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
      Stage ventanaActual = (Stage) lblHora.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void reestablecerCambio(KeyEvent event) {
    if (event.getCode() == KeyCode.CONTROL) {

      if (CambioController.operacionesAnterior != 0) {
        bufferOperaciones = CambioController.operacionesAnterior;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("SALDO INMEDIATO REESTABLECIDO");
        alert.setHeaderText("SALDO INMEDIATO REESTABLECIDO");
        alert.setContentText("SE HA REESTABLECIDO SU SALDO DE OPERACIONES");
        alert.showAndWait();
      }
      return;
      //txtNumeroSocio.setText(txtNumeroSocio.getText());
    } else if(event.getCode() == KeyCode.ESCAPE){
      if (!apertura) {
        mostrarAlertaError("ERROR AL QUERER PROCESAR LA OPERACIÓN", "EL CAJERO NO TIENE UNA CUENTA ABIERTA PARA HACER OPERACIONES");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        cargarSocio(false);
        txtNumeroSocio.setText("");
      }
    }

    return;

  }

  public void cargarSocio(boolean eleccion) {
    if (eleccion) {

      Map<String, Object> result =
          servicio.buscarSocios(Integer.parseInt(txtNumeroSocio.getText().trim()), "", 0, "");
      if (!result.get("TipoDeSocio").toString().equals("Tipo No Encontrado")) {
        imgLogo.setVisible(false);
        lblNomCargado.setVisible(true);
        lblNumCargado.setVisible(true);
        lblTipoCargado.setVisible(true);
        lblDatosCargados.setVisible(true);
        txtNombreCargado.setText(result.get("NombreFormateado").toString());
        txtNumCargado.setText(result.get("NumSocioEncontrado").toString());
        txtTipoCargado.setText(result.get("TipoDeSocio").toString());
        txtNombreCargado.setVisible(true);
        txtTipoCargado.setVisible(true);
        txtNumCargado.setVisible(true);
        txtNumeroSocio.setEditable(false);
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL INTENTAR BUSCAR AL SOCIO");
        alert.setHeaderText(result.get("NombreFormateado").toString().toUpperCase());
        alert.setContentText(
            "ERROR EN LA LÍNEA: "
                + result.get("NumSocioEncontrado")
                + " DEL PROCEDIMIENTO ALMACENADO.");
        alert.showAndWait();
        txtNumeroSocio.setText("");
      }
    } else {
      imgLogo.setVisible(true);
      lblNomCargado.setVisible(false);
      lblNumCargado.setVisible(false);
      lblTipoCargado.setVisible(false);
      lblDatosCargados.setVisible(false);
      txtNombreCargado.setText("");
      txtTipoCargado.setText("");
      txtNumCargado.setText("");
      txtNombreCargado.setVisible(false);
      txtTipoCargado.setVisible(false);
      txtNumCargado.setVisible(false);
      txtNumeroSocio.setEditable(true);
    }
  }

  public void cargarSocioPorNombre(String nombre, String numero, String tipo) {
    imgLogo.setVisible(false);
    lblNomCargado.setVisible(true);
    lblNumCargado.setVisible(true);
    lblTipoCargado.setVisible(true);
    lblDatosCargados.setVisible(true);
    txtNombreCargado.setText(nombre);
    txtNumCargado.setText(numero);
    txtTipoCargado.setText(tipo);
    txtNumeroSocio.setText(numero);
    txtNombreCargado.setVisible(true);
    txtTipoCargado.setVisible(true);
    txtNumCargado.setVisible(true);
    txtNumeroSocio.setEditable(false);
  }

  // EMPIEZAN MÉTODOS DE CONTROL
  public void setUsuario(String usuario, int Rol) {
    this.usuario = usuario;
    this.rolUsuario = Rol;
    if (lblBienvenida != null && usuario != null) {
      lblBienvenida.setText("¡BIENVENIDO, " + usuario + "!");
    }
    generarModulos();
    LocalDateTime fecha = LocalDateTime.now();
    LocalDate fechaEnviar = servicio.traerFechaHoy();

    turno = "";

    if (fecha.getHour() >= 7 && fecha.getHour() <= 12) {
      turno = "MAT";
    } else if (fecha.getHour() >= 14 && fecha.getHour() <= 20) {
      turno = "VESP";
    }

    if (rolUsuario == 1) {

      int usuarioId = servicio.traerDatosUsuario(usuario).getId();
      List<ModelCaja> verifApertura =
          servicio.traerCajas(usuarioId, fechaEnviar, 1, turno);

      if (verifApertura.size() == 0) {

        ModelCaja cuentaMUT =
            servicio.traerCajasParaCierre(
                usuarioId, fechaEnviar, 0, turno, "0001", 0);
        ModelCaja cuentaNGU =
            servicio.traerCajasParaCierre(
                usuarioId, fechaEnviar, 0, turno, "0002", 0);

        if (cuentaMUT != null && cuentaNGU != null) {
          return;
        } else if (cuentaMUT == null && cuentaNGU != null) {
          return;
        } else if (cuentaMUT != null && cuentaNGU == null) {
          return;
        }

        Platform.runLater(
            () -> {
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("EL CAJERO DEBE APERTURAR");
              alert.setHeaderText("POR FAVOR, REALICE SU APERTURA");
              alert.setContentText("POR FAVOR, REALICE SU APERTURA");
              alert.showAndWait();
              mostrarPantalla(
                  true,
                  "/com/java/fx/elegirEmpresa.fxml",
                  "ELECCIÓN DE EMPRESA",
                  true,
                  "ELECCIÓN DE EMPRESA",
                  "TRAS");
            });
      } else {
        apertura = true;
      }
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

  // ACABAN MÉTODOS DE CONTROL

  // GENERACIÓN DE MÓDULOS
  public void generarModulos() {

    ModelUsuario usuario = servicio.traerDatosUsuario(LoginController.usuarioLoggeado);

    List<Object[]> result = servicio.traerModulos(usuario.getId());
    List<String> modulos = new ArrayList<>();

    for (Object[] row : result) {
      String descripcion = (String) row[2];
      modulos.add(descripcion);
    }

    for (int i = 0; i < modulos.size(); i++) {
      switch (modulos.get(i)) {
        case "ABONO A AHORRO":
          labelMap.put(modulos.get(i), lblAbonoAhorro);
          break;
        case "ABONO A CRÉDITO":
          labelMap.put(modulos.get(i), lblAbonoCredito);
          break;
        case "DESEMBOLSOS":
          labelMap.put(modulos.get(i), lblDesembolsos);
          break;
        case "RETIROS":
          labelMap.put(modulos.get(i), lblRetiros);
          break;
        case "CAPITAL SOCIAL":
          labelMap.put(modulos.get(i), lblCapitalSocial);
          break;
        case "CANCELACIONES":
          labelMap.put(modulos.get(i), lblCancelacion);
          break;
        case "TRASLADOS":
          labelMap.put(modulos.get(i), lblTraslados);
          break;
        case "HISTORIAL":
          labelMap.put(modulos.get(i), lblHistorial);
          break;
        case "PREVISION SOCIAL":
          labelMap.put(modulos.get(i), lblPrevisionSocial);
          break;
        case "CIERRE DE CAJERO":
          labelMap.put(modulos.get(i), lblCierreCajero);
          break;
      }
    }

    ObservableList<String> moduloXrol = FXCollections.observableArrayList();
    moduloXrol.addAll(modulos);

    int fila = 0;
    for (String modulo : moduloXrol) {

      if (moduloXrol.isEmpty()) {
        break;
      }

      if (labelMap.containsKey(modulo)) {
        Label label = labelMap.get(modulo);
        gridOpciones.add(label, 0, fila);
        label.setVisible(true);
        label.setDisable(false);
        label.setCursor(Cursor.HAND);
        label.setStyle("-fx-padding: 0 0 0 20");
        fila++;
      }
    }
  }

  @FXML
  public void cerrarSesion() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE APLICATIVO");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR EL APLICATIVO?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      apertura = false;
      turno = "";
      bufferOperaciones = 0;
      validator = new Validator();
      try {
        Stage ventanaActual = (Stage) lblBienvenida.getScene().getWindow();
        Stage nuevaVentana = new Stage();
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/login.fxml"));
        fxml.setControllerFactory(Main.context::getBean);
        Scene nuevaEscena = new Scene(fxml.load());
        LoginController controlador = fxml.getController();
        controlador.rol = 0;
        LoginController.usuarioLoggeado = "";
        nuevaEscena
            .getStylesheets()
            .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(nuevaEscena);
        nuevaVentana.setTitle("AUTENTICACIÓN DE USUARIO");
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
    }
  }


  //Métodos nuevos
  private void procesarAccion(String accion) {

    if (!apertura && !accion.equals("TRASLADOS") && !accion.equals("CANCELACION") && !accion.equals("CAMBIO")) {
      mostrarAlertaError("ERROR AL QUERER PROCESAR LA OPERACIÓN", "EL CAJERO NO TIENE UNA CUENTA ABIERTA PARA HACER OPERACIONES");
      return;
    }

    switch (accion) {

      case "ABONO_AHORRO" -> {
        if (txtNombreCargado.isVisible())
          mostrarPantalla(false, "/com/java/fx/abonoAhorro.fxml", "ABONO A AHORRO", false, "", "");
        else
          mostrarAlertaError("ERROR AL QUERER ABONAR A AHORRO", "POR FAVOR, BUSQUE A ALGÚN SOCIO PARA REALIZAR LAS OPERACIONES");
      }

      case "ABONO_CREDITO" -> {
        if (txtNombreCargado.isVisible())
          mostrarPantalla(true, "/com/java/fx/elegirPrestamo.fxml", "ELEGIR CRÉDITO", true, "ELECCION DE PRESTAMO", "");
        else
          mostrarAlertaError("ERROR AL QUERER ABONAR A CRÉDITO", "POR FAVOR, BUSQUE A ALGÚN SOCIO PARA REALIZAR LAS OPERACIONES");
      }

      case "DESEMBOLSO" -> {
        if (txtNombreCargado.isVisible())
          mostrarPantalla(true, "/com/java/fx/desembolso.fxml", "DESEMBOLSO DE CRÉDITO", false, "", "");
        else
          mostrarAlertaError("ERROR AL QUERER DESEMBOLSAR", "POR FAVOR, BUSQUE A ALGÚN SOCIO PARA REALIZAR LAS OPERACIONES");
      }

      case "RETIRO" -> {
        if (txtNombreCargado.isVisible())
          mostrarPantalla(true, "/com/java/fx/retiro.fxml", "RETIRO EN EFECTIVO", false, "", "");
        else
          mostrarAlertaError("ERROR AL QUERER RETIRAR", "POR FAVOR, BUSQUE A ALGÚN SOCIO PARA REALIZAR LAS OPERACIONES.");
      }

      case "CAPITAL_SOCIAL" -> {
        if (txtNombreCargado.isVisible())
          mostrarPantalla(true, "/com/java/fx/elegirEmpresa.fxml", "ELECCIÓN DE EMPRESA", true, "ELECCIÓN DE EMPRESA", "CS");
        else
          mostrarAlertaError("ERROR AL QUERER PAGAR A CAPITAL SOCIAL", "POR FAVOR, BUSQUE A ALGÚN SOCIO PARA REALIZAR LAS OPERACIONES");
      }

      case "TRASLADOS" -> {
        if (txtNombreCargado.isVisible())
          mostrarAlertaError("ERROR AL QUERER PROCESAR TRASLADOS", "POR FAVOR, LIMPIE EL CAMPO DEL SOCIO PARA REALIZAR ESTA OPERACIÓN");
        else
          mostrarPantalla(true, "/com/java/fx/elegirEmpresa.fxml", "ELECCIÓN DE EMPRESA", true, "ELECCIÓN DE EMPRESA", "TRAS");
      }

      case "HISTORIAL" -> {
        if (txtNombreCargado.isVisible())
          mostrarAlertaError("ERROR AL QUERER VER EL HISTORIAL", "POR FAVOR, LIMPIE EL CAMPO DEL SOCIO PARA REALIZAR ESTA OPERACIÓN");
        else
          mostrarPantalla(false, "/com/java/fx/historial.fxml", "HISTORIAL DE OPERACIONES", false, "", "");
      }

      case "CIERRE_CAJERO" -> {
        if (txtNombreCargado.isVisible()) {
          mostrarAlertaError("ERROR AL QUERER REGISTRAR LOS FALTANTES Y SOBRANTES", "POR FAVOR, LIMPIE EL CAMPO DEL SOCIO PARA REALIZAR ESTA OPERACIÓN");
        } else {
          if (validarCierreCaja())
            mostrarPantalla(false, "/com/java/fx/cierreCajero.fxml", "AJUSTE DE SOBRANTES Y FALTANTE", false, "", "");
        }
      }

      case "PREVISION_SOCIAL" -> {
        if (txtNombreCargado.isVisible())
          mostrarPantalla(true, "/com/java/fx/elegirEmpresa.fxml", "ELECCIÓN DE EMPRESA", true, "ELECCIÓN DE EMPRESA", "PRESOC");
        else
          mostrarAlertaError("ERROR AL QUERER PAGAR PREVISIÓN SOCIAL", "POR FAVOR, BUSQUE A ALGÚN SOCIO PARA REALIZAR LAS OPERACIONES");
      }

      case "CANCELACION" -> {
        if (txtNombreCargado.isVisible() && (rolUsuario == 2 || rolUsuario == 3))
          mostrarAlertaError("ERROR AL QUERER CANCELAR OPERACIONES", "POR FAVOR, LIMPIE EL CAMPO DEL SOCIO PARA REALIZAR ESTA OPERACIÓN");
        else if (rolUsuario == 2 || rolUsuario == 3)
          mostrarPantalla(true, "/com/java/fx/operacionesCajero.fxml", "ELECCIÓN DE OPERACIÓN", false, "", "");
      }

      case "CAMBIO" -> {
        if (bufferOperaciones == 0)
          mostrarAlertaError("ERROR AL QUERER OTORGAR CAMBIO", "NO HA HECHO NINGUNA OPERACIÓN");
        else
          mostrarPantalla(true, "/com/java/fx/otorgarCambio.fxml", "TOTAL DE OPERACIONES", false, "", "");
      }

      case "BUSCAR_SOCIO" -> {
        if (txtNumeroSocio.getText().isEmpty())
          mostrarPantalla(false, "/com/java/fx/busquedaSocio.fxml", "BUSQUEDA DE SOCIO", false, "", "");
        else
          cargarSocio(true);
      }
    }
  }

  private boolean validarCierreCaja() {
    LocalDate fecha = servicio.traerFechaHoy();
    int usuarioId = servicio.traerDatosUsuario(LoginController.usuarioLoggeado).getId();
    ModelCaja cuentaMUT = servicio.traerCuentaDeCaja(usuarioId, fecha, 1, turno, "0001");
    ModelCaja cuentaNGU = servicio.traerCuentaDeCaja(usuarioId, fecha, 1, turno, "0002");

    if (cuentaMUT == null || cuentaNGU == null) {
      mostrarAlertaError(
              "ERROR AL QUERER REALIZAR SU CIERRE.",
              "NO CUENTA CON CAJAS ABIERTAS EN ALGUNA O EN AMBAS EMPRESAS"
      );
      return false;
    }

    if (cuentaMUT.getCierre() != 1 || cuentaNGU.getCierre() != 1) {
      mostrarAlertaError(
              "ERROR AL QUERER REALIZAR SU CIERRE.",
              "NO CUENTA CON LOS PERMISOS PARA REALIZAR SU CIERRE"
      );
      return false;
    }

    return true;
  }

  private void mostrarAlertaError(String header, String contenido) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(header);
    alert.setHeaderText(header);
    alert.setContentText(contenido);
    alert.showAndWait();
  }

  public void mostrarPantalla(boolean estilo, String ruta, String titulo,
                              boolean elegir, String tituloEleccion, String opcion) {
    try {
      FXMLLoader fxml = new FXMLLoader(getClass().getResource(ruta));
      fxml.setControllerFactory(Main.context::getBean);
      Scene nuevaEscena = new Scene(fxml.load());

      configurarControlador(fxml, ruta, opcion, elegir, tituloEleccion);

      nuevaEscena.getStylesheets()
              .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());

      if (estilo) {
        new JMetro(Style.LIGHT).setScene(nuevaEscena);
      }

      Stage nuevaVentana = new Stage();
      nuevaVentana.setTitle(titulo);
      nuevaVentana.getIcons().add(new Image(getClass().getResourceAsStream("/assets/images/logo.png")));
      nuevaVentana.setScene(nuevaEscena);
      nuevaVentana.setResizable(false);
      nuevaVentana.centerOnScreen();
      nuevaVentana.initModality(Modality.APPLICATION_MODAL);
      nuevaVentana.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void configurarControlador(FXMLLoader fxml, String ruta,
                                     String opcion, boolean elegir, String tituloEleccion) {
    if (elegir) {
      ElegirController controlador = fxml.getController();
      controlador.setTitulo(tituloEleccion.trim());
      controlador.setOpcion(opcion);
    }

    if (ruta.contains("busquedaSocio")) {
      BusquedaController c = fxml.getController();
      c.setCajeroController(this);

    } else if (ruta.contains("abonoAhorro")) {
      AhorroController c = fxml.getController();
      c.setDatos(Integer.parseInt(txtNumeroSocio.getText()), txtNombreCargado.getText(), usuario);

    } else if (ruta.contains("elegirPrestamo")) {
      ElegirController c = fxml.getController();
      c.setSocio(Integer.parseInt(txtNumeroSocio.getText()), turno);

    } else if (ruta.contains("desembolso")) {
      DesembolsoController c = fxml.getController();
      c.setDatos(Integer.parseInt(txtNumeroSocio.getText().trim()), usuario);

    } else if (ruta.contains("retiro")) {
      RetiroController c = fxml.getController();
      c.setDatos(Integer.parseInt(txtNumeroSocio.getText().trim()), usuario, turno);

    } else if (ruta.contains("elegirEmpresa") && opcion.equals("CS")) {
      ElegirController c = fxml.getController();
      c.setSocioCapitalSocial(Integer.parseInt(txtNumeroSocio.getText().trim()),
              txtNombreCargado.getText().trim(), usuario);

    } else if (ruta.contains("elegirEmpresa") && opcion.equals("TRAS")) {
      ElegirController c = fxml.getController();
      c.setUsuarioTraslado(usuario, turno, this, opcion);

    } else if (ruta.contains("elegirEmpresa") && opcion.equals("PRESOC")) {
      ElegirController c = fxml.getController();
      c.setSocioCapitalSocial(Integer.parseInt(txtNumeroSocio.getText().trim()),
              txtNombreCargado.getText().trim(), usuario);

    } else if (ruta.contains("historial")) {
      HistorialController c = fxml.getController();
      c.setDatos(usuario, turno);

    } else if (ruta.contains("cierre")) {
      CierreController c = fxml.getController();
      c.settearDatos(lblCierreCajero, turno);

    } else if (ruta.contains("otorgarCambio")) {
      CambioController c = fxml.getController();
      c.setDatos(bufferOperaciones);

    } else if (ruta.contains("operacionesCajero")) {
      OperacionesController c = fxml.getController();
      c.setDatos(turno);
    }
  }

  @FXML
  public void handlerKeyPressed(KeyEvent event) {
    switch (event.getCode()) {
      case F1     -> procesarAccion("ABONO_AHORRO");
      case F2     -> procesarAccion("ABONO_CREDITO");
      case F3     -> procesarAccion("DESEMBOLSO");
      case F4     -> procesarAccion("RETIRO");
      case F5     -> procesarAccion("CAPITAL_SOCIAL");
      case F6     -> procesarAccion("CANCELACION");
      case F7     -> procesarAccion("TRASLADOS");
      case F8     -> procesarAccion("HISTORIAL");
      case F9     -> procesarAccion("PREVISION_SOCIAL");
      case F10    -> procesarAccion("CIERRE_CAJERO");
      case F12    -> procesarAccion("CAMBIO");
      case ENTER  -> procesarAccion("BUSCAR_SOCIO");
      case ALT    -> procesarAccion("BUSCAR_SOCIO");
    }
  }

  @FXML
  public void handlerDeClicks(MouseEvent event) {
    Object source = event.getSource();

    if      (source == lblAbonoAhorro)    procesarAccion("ABONO_AHORRO");
    else if (source == lblAbonoCredito)   procesarAccion("ABONO_CREDITO");
    else if (source == lblDesembolsos)    procesarAccion("DESEMBOLSO");
    else if (source == lblRetiros)        procesarAccion("RETIRO");
    else if (source == lblCapitalSocial)  procesarAccion("CAPITAL_SOCIAL");
    else if (source == lblCancelacion)    procesarAccion("CANCELACION");
    else if (source == lblTraslados)      procesarAccion("TRASLADOS");
    else if (source == lblHistorial)      procesarAccion("HISTORIAL");
    else if (source == lblPrevisionSocial)procesarAccion("PREVISION_SOCIAL");
    else if (source == lblCierreCajero)   procesarAccion("CIERRE_CAJERO");
    else if (source == lblCambio)         procesarAccion("CAMBIO");
    else if (source == imgBusqueda)       procesarAccion("BUSCAR_SOCIO");
  }


















}
