package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Models.ModelAhorro;
import com.mutu.modulo_caja.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
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
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
      String cod_boveda="";

      switch (nom_empresa) {
        case "MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V.":
          cod_empresa = "0001";
          cod_boveda = "BVDA-MUT";
          break;
        case "NUEVA GENERACIÓN DE UMÁN, AC.":
          cod_empresa = "0002";
          cod_boveda = "BVDA-NGU";
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
      LocalDateTime fecha = LocalDateTime.now();
      LocalDateTime fechaVenc = fecha.plusYears(1);
      LocalTime hora = fecha.toLocalTime();
      DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      String fechaTicket = fecha.format(formatter);
      String fechaVencticket = fechaVenc.format(formatter);
      String horaticket = hora.format(formatterHora);
      if (opcion == 7) {


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

      Map<String, Object> res =
          servicio.procesarTraslado(LoginController.usuarioLoggeado, monto_trasladar, opcion, cod_empresa,horaticket, turno, "",0);
      boolean procesar = true;
      boolean permitir = false;
      int userid = servicio.traerDatosUsuario(LoginController.usuarioLoggeado).getId();
      String nomcajero = servicio.traerCajeroPorUsuario(LoginController.usuarioLoggeado);

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      InputStream isLogo = null;
      String protesonom = "";
      String horaFormateada = hora.format(formatterHora);
      String nombreEmpresa = servicio.traerEmpresa(cod_empresa).getRazonSocial();
      String rfcEmpresa = servicio.traerEmpresa(cod_empresa).getRfc();
      String direcEmpresa =
              servicio.traerEmpresa(cod_empresa).getCalle()
                      + " "
                      + servicio.traerEmpresa(cod_empresa).getCruzamiento()
                      + " COL. CENTRO";



      String folio = res.get("transaccion_id").toString();
      if(folio.equals("0")){
        Alert alert2 = new Alert(Alert.AlertType.ERROR);
        alert2.setTitle("ERROR AL PROCESAR EL TRASLADO DEL CAJERO");
        alert2.setHeaderText("ERROR AL INTENTAR LA OPERACIÓN DESEADA");
        alert2.setContentText(res.get("Resultado").toString().toUpperCase());
        alert2.showAndWait();
        return;

      }

      NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
      String montotraslado = formatoMoneda.format(monto_trasladar);
      MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
      String moneyAsWords = converter.asWords(BigDecimal.valueOf(monto_trasladar)).toUpperCase() + " MXN";
      int codcaja = 0;
      String desc = "";
      if (cod_empresa.equals("0001")) {
        isLogo = getClass().getResourceAsStream("/assets/images/logo-mut.png");
        desc = "NOMBRE Y FIRMA DE LA PROTESORERA";
      } else {
        isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
        desc = "NOMBRE Y FIRMA DEL PROTESORERO";
      }
      switch (res.get("Resultado").toString()) {
        case "APERTURA":
          codcaja = servicio.traerDatosCaja(userid,turno,cod_empresa,1).getId();
          alert.setTitle("APERTURA EXITOSA");
          alert.setHeaderText("APERTURA REALIZADA CORRECTAMENTE");
          alert.setContentText(
              "EL CAJERO: "
                  + LoginController.usuarioLoggeado
                  + " AHORA ESTÁ LISTO PARA REALIZAR SUS OPERACIONES EN: "
                  + nom_empresa);
          alert.showAndWait();
          try {

            Map pars = new HashMap<>();
            pars.put("Empresa", nombreEmpresa);
            pars.put("Logoempresa", isLogo);
            pars.put("Rfc", rfcEmpresa);
            pars.put("Direccion", direcEmpresa);
            pars.put("Titulo", "REPORTE DE TRASLADO DE APERTURA");

            pars.put("Id", folio);
            pars.put("Fecha", fechaTicket);
            pars.put("Turno", turno);

            pars.put("Origen", cod_boveda);
            pars.put("Destino", "CUENTA DE CAJERO " + codcaja);
            pars.put("Monto", montotraslado);
            pars.put("Montoletras", moneyAsWords);
            pars.put("DescProteso", desc);


            pars.put("Cajerouser", LoginController.usuarioLoggeado);
            pars.put("Cajeronom", nomcajero);
            pars.put("Hora", horaFormateada);
            if (cod_empresa.equals("0001")) {
              protesonom ="CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL";
            } else {
              protesonom ="CASIMIRO UITZ VILLANUEVA";
            }
            pars.put("Protesonom", protesonom);

            pars.put(
                      "Descripcion",
                      "TRASLADO DE APERTURA realizado por "+ montotraslado+ " (" +moneyAsWords +
                            ") efectuado en la sucursal de " +
                              "UMAN por "+nomcajero+ " y autorizado por " + protesonom);

            InputStream isRepo = getClass().getResourceAsStream("/Reports/traslado_apertura.jasper");
            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(jpRepo, false);

            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("REPORTE DE TRASLADO");
            viewer.setVisible(true);

          } catch (Exception e) {
            e.printStackTrace();
          }



          permitir = true;
          break;
        case "CIERRE":
          alert.setTitle("TRASLADO DE CIERRE EXITOSO");
          alert.setHeaderText("TRASLADO DE CIERRE REALIZADO CORRECTAMENTE");
          alert.setContentText(
              "EL CAJERO: " + LoginController.usuarioLoggeado + " AHORA ESTÁ LISTO PARA CERRAR EN: " + nom_empresa);
          alert.showAndWait();
          codcaja = servicio.traerDatosCaja(userid,turno,cod_empresa,0).getId();
          try {


            Map pars = new HashMap<>();
            pars.put("Empresa", nombreEmpresa);
            pars.put("Logo", isLogo);
            pars.put("Rfc", rfcEmpresa);
            pars.put("Direccion", direcEmpresa);
            pars.put("Titulo", "REPORTE DE TRASLADO DE CIERRE");

            pars.put("Id", folio);
            pars.put("Fecha", fechaTicket);
            pars.put("Turno", turno);

            pars.put("Origen", "CUENTA DE CAJERO " + codcaja);
            pars.put("Destino",  cod_boveda);
            pars.put("Monto", montotraslado);
            pars.put("Montoletras", moneyAsWords);
            pars.put("DescProteso", desc);

            pars.put("Cajerouser", LoginController.usuarioLoggeado);
            pars.put("Cajeronom", nomcajero);
            pars.put("Hora", horaFormateada);
            if (cod_empresa.equals("0001")) {
              protesonom ="CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL";
            } else {
              protesonom ="CASIMIRO UITZ VILLANUEVA";
            }
            pars.put("Protesonom", protesonom);

            pars.put(
                    "Descripcion",
                    "TRASLADO DE CIERRE realizado por "+ montotraslado+ " (" +moneyAsWords +
                            ") efectuado en la sucursal de " +
                            "UMAN por "+nomcajero+ " y autorizado por " + protesonom);

            InputStream isRepo = getClass().getResourceAsStream("/Reports/traslado_cierre.jasper");
            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(jpRepo, false);

            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("REPORTE DE TRASLADO");
            viewer.setVisible(true);

          } catch (Exception e) {
            e.printStackTrace();
          }

          break;
        default:
          Alert alert2 = new Alert(Alert.AlertType.ERROR);
          alert2.setTitle("ERROR AL PROCESAR EL TRASLADO DEL CAJERO");
          alert2.setHeaderText("ERROR AL INTENTAR LA OPERACIÓN DESEADA");
          alert2.setContentText(res.get("Resultado").toString().toUpperCase());
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
