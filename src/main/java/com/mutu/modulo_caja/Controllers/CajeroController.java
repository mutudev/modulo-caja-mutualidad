package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.view.JasperViewer;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import win.zqxu.jrviewer.JRViewerFX;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
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
      lblFaltantesSobrantes,
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
  public List<Object[]> CierreMUT = null;
  public List<Object[]> CierreNGU = null;
  public static double bufferOperaciones = 100;

  @Autowired private Servicio servicio;

  // EMPIEZA MÉTODO INICIAL
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    iniciarReloj();
    LocalDateTime fecha = LocalDateTime.now();
    lblFecha.setText(
        "FECHA: " + fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear());
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
  public void handlerKeyPressed(KeyEvent event) {
    String ruta = "";
    String titulo = "";
    boolean estilo = false;
    boolean cargar = false;
    boolean elegir = false;
    String tituloEleccion = "";
    String opcion = "";

    switch (event.getCode()) {
      case KeyCode.F1:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblAbonoAhorro.isVisible()) {
          ruta = "/com/java/fx/abonoAhorro.fxml";
          titulo = "ABONO A AHORRO";
          estilo = true;
          cargar = true;
        } else if (!txtNombreCargado.isVisible() && lblAbonoAhorro.isVisible()) {
          mostrarError("ABONAR A AHORRO");
        }
        break;

      case KeyCode.ENTER:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (!txtNumeroSocio.getText().isEmpty()) {
          cargarSocio(true);
        } else {
          mostrarError("BUSCAR AL SOCIO");
        }
        break;

      case KeyCode.CONTROL:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible()) {
          cargarSocio(false);
          txtNumeroSocio.setText("");
        }
        break;

      case KeyCode.ALT:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (!txtNombreCargado.isVisible() && txtNumeroSocio.getText().isEmpty()) {
          ruta = "/com/java/fx/busquedaSocio.fxml";
          titulo = "BUSQUEDA DE SOCIO";
          cargar = true;
        } else if (!txtNombreCargado.isVisible() && !txtNumeroSocio.getText().isEmpty()) {
          cargarSocio(true);
        }
        break;

      case KeyCode.F2:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblAbonoCredito.isVisible()) {
          ruta = "/com/java/fx/elegirPrestamo.fxml";
          titulo = "ELEGIR CRÉDITO";
          estilo = true;
          cargar = true;
          elegir = true;
          tituloEleccion = "ELECCION DE PRESTAMO";
        } else if (!txtNombreCargado.isVisible() && lblAbonoCredito.isVisible()) {
          mostrarError("ABONAR A CRÉDITO");
        }
        break;

      case KeyCode.F3:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblDesembolsos.isVisible()) {
          ruta = "/com/java/fx/desembolso.fxml";
          titulo = "DESEMBOLSO DE CRÉDITO";
          estilo = true;
          cargar = true;
        } else if (!txtNombreCargado.isVisible() && lblDesembolsos.isVisible()) {
          mostrarError("DESEMBOLSAR");
        }
        break;

      case KeyCode.F4:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblRetiros.isVisible()) {
          ruta = "/com/java/fx/retiro.fxml";
          titulo = "RETIRO EN EFECTIVO";
          estilo = true;
          cargar = true;
        } else if (!txtNombreCargado.isVisible() && lblRetiros.isVisible()) {
          mostrarError("RETIRAR");
        }
        break;

      case KeyCode.F5:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblCapitalSocial.isVisible()) {
          ruta = "/com/java/fx/elegirEmpresa.fxml";
          titulo = "ELECCIÓN DE EMPRESA";
          estilo = true;
          cargar = true;
          elegir = true;
          tituloEleccion = "ELECCIÓN DE EMPRESA";
          opcion = "CS";
        } else if (!txtNombreCargado.isVisible() && lblCapitalSocial.isVisible()) {
          mostrarError("PAGAR A CAPITAL SOCIAL");
        }
        break;

      case KeyCode.F7:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblFaltantesSobrantes.isVisible()) {
          mostrarError("REGISTRAR LOS FALTANTES Y SOBRANTES");
        } else if (!txtNombreCargado.isVisible() && lblFaltantesSobrantes.isVisible()) {
          ruta = "/com/java/fx/elegirEmpresa.fxml";
          titulo = "ELECCIÓN DE EMPRESA";
          estilo = true;
          cargar = true;
          elegir = true;
          tituloEleccion = "ELECCIÓN DE EMPRESA";
          opcion = "AJSF";
        }
        break;

      case KeyCode.F8:
        if (txtNombreCargado.isVisible() && lblTraslados.isVisible()) {
          mostrarError("PROCESAR TRASLADOS");
        } else if (!txtNombreCargado.isVisible() && lblTraslados.isVisible()) {
          ruta = "/com/java/fx/elegirEmpresa.fxml";
          titulo = "ELECCIÓN DE EMPRESA";
          estilo = true;
          cargar = true;
          elegir = true;
          tituloEleccion = "ELECCIÓN DE EMPRESA";
          opcion = "TRAS";
        }
        break;

      case KeyCode.F9:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblHistorial.isVisible()) {
          mostrarError("VER EL HISTORIAL");
        } else if (!txtNombreCargado.isVisible() && lblHistorial.isVisible()) {
          ruta = "/com/java/fx/historial.fxml";
          titulo = "HISTORIAL DE OPERACIONES";
          estilo = true;
          cargar = true;
        }
        break;

      case KeyCode.F10:
        if (txtNombreCargado.isVisible() && lblCierreCajero.isVisible()) {
          mostrarError("CERRAR");
        } else if (!txtNombreCargado.isVisible() && lblCierreCajero.isVisible()) {

          String empresa_cod = "0001";
          String empresa_cod2 = "0002";
          LocalDate fecha = LocalDate.now();
          List<Object[]> cuentaMUT =
              servicio.CuentasdeCierre(
                  LoginController.usuarioLoggeado, fecha.toString(), 0, turno, empresa_cod, 0);
          List<Object[]> cuentaNGU =
              servicio.CuentasdeCierre(
                  LoginController.usuarioLoggeado, fecha.toString(), 0, turno, empresa_cod2, 0);

          if (cuentaMUT.isEmpty() && cuentaNGU.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
            alert.setHeaderText("NO TIENE CUENTAS DE CAJA ABIERTAS");
            alert.setContentText("POR FAVOR, REALICE SU PROCESO DE APERTURA, AJUSTE Y TRASLADO.");
            alert.showAndWait();
            return;
          }

          if (cuentaMUT.size() == 1 && cuentaNGU.size() == 1) {
            if (Integer.parseInt(cuentaMUT.getFirst()[9].toString()) == 0) {
              Alert alert = new Alert(Alert.AlertType.ERROR);
              alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
              alert.setHeaderText("NO TIENE PERMITIDO CERRAR");
              alert.setContentText(
                  "POR FAVOR, REALICE SU PROCESO DE AJUSTE Y TRASLADO PARA LA EMPRESA: "
                      + empresa_cod);
              alert.showAndWait();
              return;
            }

            if (Integer.parseInt(cuentaNGU.getFirst()[9].toString()) == 0) {
              Alert alert = new Alert(Alert.AlertType.ERROR);
              alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
              alert.setHeaderText("NO TIENE PERMITIDO CERRAR");
              alert.setContentText(
                  "POR FAVOR, REALICE SU PROCESO DE AJUSTE Y TRASLADO PARA LA EMPRESA: "
                      + empresa_cod2);
              alert.showAndWait();
              return;
            }
          } else if (cuentaMUT.size() == 1 && cuentaNGU.size() == 0) {
            if (Integer.parseInt(cuentaMUT.getFirst()[9].toString()) == 0) {
              Alert alert = new Alert(Alert.AlertType.ERROR);
              alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
              alert.setHeaderText("NO TIENE PERMITIDO CERRAR");
              alert.setContentText(
                  "POR FAVOR, REALICE SU PROCESO DE AJUSTE Y TRASLADO PARA LA EMPRESA: "
                      + empresa_cod);
              alert.showAndWait();
              return;
            }
          } else {
            if (Integer.parseInt(cuentaNGU.getFirst()[9].toString()) == 0) {
              Alert alert = new Alert(Alert.AlertType.ERROR);
              alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
              alert.setHeaderText("NO TIENE PERMITIDO CERRAR");
              alert.setContentText(
                  "POR FAVOR, REALICE SU PROCESO DE AJUSTE Y TRASLADO PARA LA EMPRESA: "
                      + empresa_cod2);
              alert.showAndWait();
              return;
            }
          }

          CierreMUT = cuentaMUT;
          CierreNGU = cuentaNGU;
          ruta = "/com/java/fx/cierreCajero.fxml";
          titulo = "CIERRE DE CAJERO";
          estilo = true;
          cargar = true;
        }
        break;

      case KeyCode.F11:
        if (!apertura) {
          mostrarError("APERTURA");
          return;
        }

        if (txtNombreCargado.isVisible() && lblPrevisionSocial.isVisible()) {
          ruta = "/com/java/fx/elegirEmpresa.fxml";
          titulo = "ELECCIÓN DE EMPRESA";
          estilo = true;
          cargar = true;
          elegir = true;
          tituloEleccion = "ELECCIÓN DE EMPRESA";
          opcion = "PRESOC";

        } else if (!txtNombreCargado.isVisible() && lblPrevisionSocial.isVisible()) {
          mostrarError("PAGAR PREVISIÓN SOCIAL");
        }
        break;

      case KeyCode.F6:
        if (txtNombreCargado.isVisible() && (rolUsuario == 2 || rolUsuario == 3)) {
          mostrarError("CANCELAR OPERACIONES");
        } else if (rolUsuario == 2 || rolUsuario == 3) {
          ruta = "/com/java/fx/operacionesCajero.fxml";
          titulo = "ELECCIÓN DE OPERACIÓN";
          estilo = true;
          cargar = true;
        }
        break;

      case KeyCode.F12:
        if (bufferOperaciones == 0) {
          mostrarError("SIN OPERACIONES");
        } else {
          ruta = "/com/java/fx/otorgarCambio.fxml";
          titulo = "TOTAL DE OPERACIONES";
          estilo = true;
          cargar = true;
        }
        break;
    }

    if (cargar) {
      mostrarPantalla(estilo, ruta, titulo, elegir, tituloEleccion, opcion);
    }
  }

  @FXML
  public void handlerDeClicks(MouseEvent event) {
    Object source = event.getSource();
    String ruta = "";
    String titulo = "";
    String tituloEleccion = "";
    String opcion = "";
    boolean estilo = false;
    boolean cargar = false;
    boolean elegir = false;

    if (source == lblAbonoAhorro) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        ruta = "/com/java/fx/abonoAhorro.fxml";
        titulo = "ABONO A AHORRO";
        estilo = true;
        cargar = true;
      } else mostrarError("ABONAR");

    } else if (source == lblAbonoCredito) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        ruta = "/com/java/fx/elegirPrestamo.fxml";
        titulo = "ELEGIR CRÉDITO";
        estilo = true;
        cargar = true;
        elegir = true;
        tituloEleccion = "ELECCION DE PRESTAMO";
      } else mostrarError("ABONAR");

    } else if (source == lblDesembolsos) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        ruta = "/com/java/fx/desembolso.fxml";
        titulo = "DESEMBOLSO DE CRÉDITO";
        estilo = true;
        cargar = true;
      } else mostrarError("DESEMBOLSAR");

    } else if (source == lblRetiros) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        ruta = "/com/java/fx/retiro.fxml";
        titulo = "RETIRO EN EFECTIVO";
        estilo = true;
        cargar = true;
      } else mostrarError("RETIRAR");

    } else if (source == lblCapitalSocial) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        ruta = "/com/java/fx/elegirEmpresa.fxml";
        titulo = "ELECCIÓN DE EMPRESA";
        estilo = true;
        cargar = true;
        elegir = true;
        tituloEleccion = "ELECCIÓN DE EMPRESA";
        opcion = "CS";
      } else mostrarError("ACCEDER A CAPITAL SOCIAL");

    } else if (source == imgBusqueda) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNumeroSocio.getText().isEmpty()) {
        ruta = "/com/java/fx/busquedaSocio.fxml";
        titulo = "BUSCAR SOCIO";
        cargar = true;
      } else {
        cargarSocio(true);
      }

    } else if (source == lblFaltantesSobrantes) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        mostrarError("REGISTRAR LOS FALTANTES Y SOBRANTES");
      } else {
        ruta = "/com/java/fx/elegirEmpresa.fxml";
        titulo = "ELECCIÓN DE EMPRESA";
        estilo = true;
        cargar = true;
        elegir = true;
        tituloEleccion = "ELECCIÓN DE EMPRESA";
        opcion = "AJSF";
      }

    } else if (source == lblTraslados) {
      if (txtNombreCargado.isVisible()) {
        mostrarError("PROCESAR TRASLADOS");
      } else {
        ruta = "/com/java/fx/elegirEmpresa.fxml";
        titulo = "ELECCIÓN DE EMPRESA";
        estilo = true;
        cargar = true;
        elegir = true;
        tituloEleccion = "ELECCIÓN DE EMPRESA";
        opcion = "TRAS";
      }

    } else if (source == lblHistorial) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible()) {
        mostrarError("VER EL HISTORIAL");
      } else {
        ruta = "/com/java/fx/historial.fxml";
        titulo = "HISTORIAL DE OPERACIONES";
        estilo = true;
        cargar = true;
      }

    } else if (source == lblCierreCajero) {

      if (txtNombreCargado.isVisible()) {
        mostrarError("CERRAR");
      } else {
        String empresa_cod = "0001";
        String empresa_cod2 = "0002";
        LocalDate fecha = LocalDate.now();
        List<Object[]> cuentaMUT =
            servicio.CuentasdeCierre(
                LoginController.usuarioLoggeado, fecha.toString(), 1, turno, empresa_cod, 2);
        List<Object[]> cuentaNGU =
            servicio.CuentasdeCierre(
                LoginController.usuarioLoggeado, fecha.toString(), 1, turno, empresa_cod2, 2);

        if (cuentaMUT.isEmpty() && cuentaNGU.isEmpty()) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
          alert.setHeaderText("NO TIENE CUENTAS DE CAJA ABIERTAS");
          alert.setContentText("POR FAVOR, REALICE SU PROCESO DE APERTURA, AJUSTE Y TRASLADO.");
          alert.showAndWait();
          return;
        }

        if (cuentaMUT.size() == 1 && cuentaNGU.size() == 1) {
          if (Integer.parseInt(cuentaMUT.getFirst()[9].toString()) == 0
              || Integer.parseInt(cuentaNGU.getFirst()[9].toString()) == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
            alert.setHeaderText("NO TIENE PERMITIDO CERRAR");
            alert.setContentText(
                "POR FAVOR, REALICE SU PROCESO DE AJUSTE Y TRASLADO PARA AMBAS EMPRESAS");
            alert.showAndWait();
            return;
          }

        } else if (cuentaMUT.size() == 1 && cuentaNGU.size() == 0) {
          if (Integer.parseInt(cuentaMUT.getFirst()[9].toString()) == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
            alert.setHeaderText("NO TIENE PERMITIDO CERRAR");
            alert.setContentText(
                "POR FAVOR, REALICE SU PROCESO DE AJUSTE Y TRASLADO PARA LA EMPRESA: "
                    + empresa_cod);
            alert.showAndWait();
            return;
          }
        } else {
          if (Integer.parseInt(cuentaNGU.getFirst()[9].toString()) == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL QUERER REALIZAR SU CIERRE");
            alert.setHeaderText("NO TIENE PERMITIDO CERRAR");
            alert.setContentText(
                "POR FAVOR, REALICE SU PROCESO DE AJUSTE Y TRASLADO PARA LA EMPRESA: "
                    + empresa_cod2);
            alert.showAndWait();
            return;
          }
        }

        CierreMUT = cuentaMUT;
        CierreNGU = cuentaNGU;
        ruta = "/com/java/fx/cierreCajero.fxml";
        titulo = "CIERRE DE CAJERO";
        estilo = true;
        cargar = true;
      }

    } else if (source == lblPrevisionSocial) {

      if (!apertura) {
        mostrarError("APERTURA");
        return;
      }

      if (txtNombreCargado.isVisible() && apertura) {
        ruta = "/com/java/fx/elegirEmpresa.fxml";
        titulo = "ELECCIÓN DE EMPRESA";
        estilo = true;
        cargar = true;
        elegir = true;
        tituloEleccion = "ELECCIÓN DE EMPRESA";
        opcion = "PRESOC";
      } else {
        mostrarError("PAGAR PREVISIÓN SOCIAL");
      }

    } else if (source == lblCancelacion) {
      if (txtNombreCargado.isVisible() && (rolUsuario == 1 || rolUsuario == 2)) {
        mostrarError("CANCELAR OPERACIONES");
      } else if (rolUsuario == 1 || rolUsuario == 2) {
        ruta = "/com/java/fx/operacionesCajero.fxml";
        titulo = "OPERACIONES DE CAJERO";
        estilo = true;
        cargar = true;
      }
    } else if (source == lblCambio) {
      if (bufferOperaciones == 0) {
        mostrarError("SIN OPERACIONES");
      } else {
        ruta = "/com/java/fx/otorgarCambio.fxml";
        titulo = "TOTAL DE OPERACIONES";
        estilo = true;
        cargar = true;
      }
    }

    if (cargar) {
      mostrarPantalla(estilo, ruta, titulo, elegir, tituloEleccion, opcion);
    }

    ruta = "";
    cargar = false;
  }

  public void mostrarError(String accion) {
    if (accion.equals("REGISTRAR LOS FALTANTES Y SOBRANTES")
        || accion.equals("PROCESAR TRASLADOS")
        || accion.equals("VER EL HISTORIAL")
        || accion.equals("CERRAR")
        || accion.equals("CANCELAR OPERACIONES")) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL QUERER " + accion);
      alert.setHeaderText("HAY UN SOCIO CARGADO");
      alert.setContentText("POR FAVOR, LIMPIE EL CAMPO DEL SOCIO PARA REALIZAR ESTA OPERACIÓN.");
      alert.showAndWait();
    } else if (accion.equals("APERTURA")) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL QUERER PROCESAR LA OPERACIÓN");
      alert.setHeaderText("EL CAJERO NO TIENE UNA CUENTA ABIERTA PARA HACER OPERACIONES");
      alert.setContentText("POR FAVOR, HAGA SU TRASLADO DE APERTURA.");
      alert.showAndWait();
    } else if (accion.equals("SIN OPERACIONES")) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL QUERER OTORGAR CAMBIO");
      alert.setHeaderText("NO HA HECHO NINGUNA OPERACIÓN");
      alert.setContentText("POR FAVOR, HAGA ALGUNA OPERACIÓN.");
      alert.showAndWait();
    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL QUERER " + accion);
      alert.setHeaderText("NO HA CARGADO A NINGÚN SOCIO");
      alert.setContentText("POR FAVOR, BUSQUE A ALGÚN SOCIO PARA REALIZAR LAS OPERACIONES.");
      alert.showAndWait();
    }
  }

  public void mostrarPantalla(
      boolean estilo,
      String ruta,
      String titulo,
      boolean elegir,
      String tituloEleccion,
      String opcion) {
    try {
      Stage nuevaVentana = new Stage();
      FXMLLoader fxml = new FXMLLoader(getClass().getResource(ruta));
      fxml.setControllerFactory(Main.context::getBean);
      Scene nuevaEscena = new Scene(fxml.load());

      if (elegir) {
        ElegirController controlador = fxml.getController();
        controlador.setTitulo(tituloEleccion.trim());
        controlador.setOpcion(opcion);
      }

      if (ruta.contains("busquedaSocio") || ruta.endsWith("busquedaSocio.fxml")) {
        BusquedaController controlador = fxml.getController();
        controlador.setCajeroController(this);
      } else if (ruta.contains("abonoAhorro") || ruta.endsWith("abonoAhorro.fxml")) {
        AhorroController controller = fxml.getController();
        controller.setDatos(
            Integer.parseInt(txtNumeroSocio.getText()), txtNombreCargado.getText(), usuario);
      } else if (ruta.contains("elegirPrestamo") || ruta.endsWith("elegirPrestamo.fxml")) {
        ElegirController controller = fxml.getController();
        controller.setSocio(Integer.parseInt(txtNumeroSocio.getText().toString()));
      } else if (ruta.contains("desembolso") || ruta.endsWith("desembolso.fxml")) {
        DesembolsoController controller = fxml.getController();
        controller.setDatos(Integer.parseInt(txtNumeroSocio.getText().trim()), usuario);
      } else if (ruta.contains("retiro") || ruta.endsWith("retiro.fxml")) {
        RetiroController controller = fxml.getController();
        controller.setDatos(Integer.parseInt(txtNumeroSocio.getText().trim()), usuario, turno);
      } else if (ruta.contains("elegirEmpresa")
          && ruta.endsWith("elegirEmpresa.fxml")
          && opcion.equals("CS")) {
        ElegirController controller = fxml.getController();
        controller.setSocioCapitalSocial(
            Integer.parseInt(txtNumeroSocio.getText().trim()),
            txtNombreCargado.getText().trim(),
            usuario);
      } else if (ruta.contains("elegirEmpresa")
          && ruta.endsWith("elegirEmpresa.fxml")
          && opcion.equals("TRAS")) {
        ElegirController controller = fxml.getController();
        controller.setUsuarioTralado(usuario, turno, this);
      } else if (ruta.contains("historial") || ruta.endsWith("historial.fxml")) {
        HistorialController controller = fxml.getController();
        controller.setDatos(usuario, turno);
      } else if (ruta.contains("elegirEmpresa")
          && ruta.endsWith("elegirEmpresa.fxml")
          && opcion.equals("PRESOC")) {
        ElegirController controller = fxml.getController();
        controller.setSocioCapitalSocial(
            Integer.parseInt(txtNumeroSocio.getText().trim()),
            txtNombreCargado.getText().trim(),
            usuario);
      } else if (ruta.contains("elegirEmpresa")
          && ruta.endsWith("elegirEmpresa.fxml")
          && opcion.equals("AJSF")) {
        ElegirController controller = fxml.getController();
        controller.setDatosAJSF(turno);
      } else if (ruta.contains("cierreCajero") && ruta.endsWith("cierreCajero.fxml")) {
        CierreController controller = fxml.getController();
        controller.setDatos(CierreMUT, CierreNGU, lblBienvenida);
      } else if (ruta.contains("otorgarCambio") && ruta.endsWith("otorgarCambio.fxml")) {
        CambioController controller = fxml.getController();
        controller.setDatos(bufferOperaciones);
      } else if (ruta.contains("operacionesCajero") && ruta.endsWith("operacionesCajero.fxml")) {
        OperacionesController controller = fxml.getController();
        controller.setDatos(turno);
      }

      nuevaEscena
          .getStylesheets()
          .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());

      if (estilo) {
        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(nuevaEscena);
      }
      nuevaVentana.setTitle(titulo);
      Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
      nuevaVentana.getIcons().add(icon);
      nuevaVentana.setScene(nuevaEscena);
      nuevaVentana.setResizable(false);
      nuevaVentana.centerOnScreen();
      nuevaVentana.initModality(Modality.APPLICATION_MODAL);
      nuevaVentana.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void cargarSocio(boolean eleccion) {
    if (eleccion) {

      Map<String, Object> result =
          servicio.buscarSocios(Integer.parseInt(txtNumeroSocio.getText().trim()), "", 0, "");
      if (validator.validate()
          && !result.get("TipoDeSocio").toString().equals("Tipo No Encontrado")) {
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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String fechaEnviar = fecha.format(formatter);

    turno = "";

    if (fecha.getHour() >= 7 && fecha.getHour() <= 12) {
      turno = "MAT";
    } else if (fecha.getHour() >= 14 && fecha.getHour() <= 20) {
      turno = "VESP";
    }

    if (rolUsuario == 1) {
      List<Object[]> verifApertura =
          servicio.verificarAperturaCajero(usuario, fechaEnviar, 1, turno);

      if (verifApertura.size() == 0) {

        String empresa_cod = "0001";
        String empresa_cod2 = "0002";
        List<Object[]> cuentaMUT =
            servicio.CuentasdeCierre(
                LoginController.usuarioLoggeado, fecha.toString(), 0, turno, empresa_cod, 0);
        List<Object[]> cuentaNGU =
            servicio.CuentasdeCierre(
                LoginController.usuarioLoggeado, fecha.toString(), 0, turno, empresa_cod2, 0);

        if (cuentaMUT.size() != 0 && cuentaNGU.size() != 0) {
          return;
        } else if (cuentaMUT.isEmpty() && cuentaNGU.size() != 0) {
          return;
        } else if (cuentaMUT.size() != 0 && cuentaNGU.isEmpty()) {
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

    List<Object[]> result = servicio.traerModulos(rolUsuario);
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
        case "FALTANTES Y SOBRANTES":
          labelMap.put(modulos.get(i), lblFaltantesSobrantes);
          break;
        case "TRASLADOS":
          labelMap.put(modulos.get(i), lblTraslados);
          break;
        case "HISTORIAL":
          labelMap.put(modulos.get(i), lblHistorial);
          break;
        case "CIERRE DE CAJERO":
          labelMap.put(modulos.get(i), lblCierreCajero);
          break;
        case "PREVISION SOCIAL":
          labelMap.put(modulos.get(i), lblPrevisionSocial);
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

  @FXML
  public void pruebaCierre() {
    Object[] datos = servicio.traerCierreCajero(1);
    for (Object filaObj : datos) {
      if (filaObj instanceof Object[]) {
        Object[] fila = (Object[]) filaObj;

        System.out.println("ID: " + fila[0]);
        System.out.println("USUARIO_ID: " + fila[1]);
        System.out.println("ABONO_AHORRO: " + fila[2]);
        System.out.println("ABONO_CREDITO: " + fila[3]);
        System.out.println("RETIROS: " + fila[4]);
        System.out.println("DESEMBOLSOS: " + fila[5]);
        System.out.println("CAPITAL_SOCIAL: " + fila[6]);
        System.out.println("PREVISION_SOCIAL: " + fila[7]);
        System.out.println("TRAS_APERTURA: " + fila[8]);
        System.out.println("TRAS_CIERRE: " + fila[9]);
        System.out.println("SOBRANTE: " + fila[10]);
        System.out.println("FALTANTE: " + fila[11]);
        System.out.println("EMPRESA_COD: " + fila[12]);
        System.out.println("FECHA (FC): " + fila[13]);
        System.out.println("HORA (HR): " + fila[14]);
        System.out.println("----------------------------");
      }
    }
  }


}
