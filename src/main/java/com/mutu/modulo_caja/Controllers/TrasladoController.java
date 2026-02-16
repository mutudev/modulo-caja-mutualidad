package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Models.ModelAhorro;
import com.mutu.modulo_caja.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
import java.sql.Connection;
import java.sql.DriverManager;
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

  public Validator validator = new Validator();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (cmbTipoTras != null) {
      ObservableList<String> empresas = FXCollections.observableArrayList();

      if (LoginController.rolusuarioLoggeado == 1) {
        empresas.addAll("BOVEDA - CAJA");
      } else {
        empresas.addAll("BOVEDA - CAJA", "CAJA - BOVEDA");
      }

      cmbTipoTras.setItems(empresas);
      cmbTipoTras.getSelectionModel().selectFirst();
    }

    validator
        .createCheck()
        .dependsOn("input", txtCantidad.textProperty())
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
        .decorates(txtCantidad)
        .immediate();

    txtCantidad.setTextFormatter(
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
    if (event.getCode().equals(KeyCode.ENTER)) {
      event.consume();
      trasladar();
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

  @FXML
  public void trasladar() {


    if (validator.validate()) {

      Stage loadingStage = new Stage();
      loadingStage.initModality(Modality.APPLICATION_MODAL);
      loadingStage.initStyle(StageStyle.UNDECORATED);
      loadingStage.setAlwaysOnTop(true);


      VBox loadingPane = new VBox(20);
      loadingPane.setAlignment(Pos.CENTER);
      loadingPane.setPadding(new Insets(30));
      loadingPane.setStyle("-fx-background-color: white; -fx-border-color: #185754; -fx-border-width: 2;");

      ProgressIndicator progressIndicator = new ProgressIndicator();
      progressIndicator.setPrefSize(60, 60);

      Label loadingLabel = new Label("Generando Traslado...");
      loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
      loadingLabel.setTextFill(Color.web("#39577c"));

      loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

      Scene loadingScene = new Scene(loadingPane, 300, 150);
      loadingStage.setScene(loadingScene);

      loadingStage.centerOnScreen();

      String nom_empresa = txtEmpresa.getText().trim();
      String cod_empresa = "";
      String cod_boveda = "";

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
            Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
            alert2.setTitle("CONFIRMAR TRASLADO HACIA BÓVEDA");
            alert2.setHeaderText("AJUSTE DE SOBRANTES Y FALTANTES NO REALIZADO");
            alert2.setContentText(
                    "AÚN NO HA REALIZADO SU AJUSTE DE SOBRANTES Y FALTANTES.\n" +
                            "¿ESTÁ SEGURO DE QUE DESEA SEGUIR TRASLADANDO?"
            );

            Optional<ButtonType> result = alert2.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
              continue;
            } else {
              // Usuario cancela
              return;
            }

          }
        }
      }

      Map<String, Object> res =
          servicio.procesarTraslado(
              LoginController.usuarioLoggeado,
              monto_trasladar,
              opcion,
              cod_empresa,
              horaticket,
              turno,
              0,
              "",
              0);
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
              servicio.traerEmpresa(cod_empresa).getCalle() + " "
                      + servicio.traerEmpresa(cod_empresa).getCruzamiento()
                      + " COL. CENTRO";

      String folio = res.get("transaccion_id").toString();
      if (folio.equals("0")) {
        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
        alert2.setTitle("CONFIRMAR OPERACIÓN");
        alert2.setHeaderText("¿DESEA CONTINUAR CON LA OPERACIÓN?");
        alert2.setContentText(res.get("Resultado").toString().toUpperCase());
        Optional<ButtonType> result = alert2.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
          //Volvemos a ejecutar el traslado
          res =
                  servicio.procesarTraslado(
                          LoginController.usuarioLoggeado,
                          monto_trasladar,
                          opcion,
                          cod_empresa,
                          horaticket,
                          turno,
                          1,
                          "",
                          0);
          procesar = true;
          permitir = false;
          userid = servicio.traerDatosUsuario(LoginController.usuarioLoggeado).getId();
          nomcajero = servicio.traerCajeroPorUsuario(LoginController.usuarioLoggeado);
          alert = new Alert(Alert.AlertType.INFORMATION);
          isLogo = null;
          protesonom = "";
          horaFormateada = hora.format(formatterHora);
          nombreEmpresa = servicio.traerEmpresa(cod_empresa).getRazonSocial();
          rfcEmpresa = servicio.traerEmpresa(cod_empresa).getRfc();
          direcEmpresa =
                  servicio.traerEmpresa(cod_empresa).getCalle() + " "
                          + servicio.traerEmpresa(cod_empresa).getCruzamiento()
                          + " COL. CENTRO";

          folio = res.get("transaccion_id").toString();
        } else {
          return;
        }
      }

      NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
      String montotraslado = formatoMoneda.format(monto_trasladar);
      MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
      String moneyAsWords =
              converter.asWords(BigDecimal.valueOf(monto_trasladar))
                      .toUpperCase() + " MXN";

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

          alert.setTitle("APERTURA EXITOSA");
          alert.setHeaderText("APERTURA REALIZADA CORRECTAMENTE");
          alert.setContentText(
                  "EL CAJERO: " + LoginController.usuarioLoggeado
                          + " AHORA ESTÁ LISTO PARA REALIZAR SUS OPERACIONES EN: "
                          + nom_empresa);

          alert.showAndWait();

          protesonom = cod_empresa.equals("0001")
                  ? "CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL"
                  : "CASIMIRO UITZ VILLANUEVA";

          Map<String, Object> parsApertura =
                  construirParametrosReporte(
                          nombreEmpresa,
                          isLogo,
                          rfcEmpresa,
                          direcEmpresa,
                          "REPORTE DE TRASLADO DE APERTURA",
                          folio,
                          fechaTicket,
                          turno,
                          cod_boveda,
                          "CUENTA DE CAJERO " + LoginController.usuarioLoggeado,
                          montotraslado,
                          moneyAsWords,
                          desc,
                          LoginController.usuarioLoggeado,
                          nomcajero,
                          horaFormateada,
                          protesonom,
                          "TRASLADO DE APERTURA realizado por " + montotraslado
                                  + " (" + moneyAsWords + ") efectuado en la sucursal de UMAN por "
                                  + nomcajero + " y autorizado por " + protesonom
                  );

          generarReporteAsync(
                  "/Reports/traslado_apertura.jasper",
                  parsApertura,
                  loadingStage
          );

          permitir = true;
          break;

        case "CIERRE":

          alert.setTitle("TRASLADO DE CIERRE EXITOSO");
          alert.setHeaderText("TRASLADO DE CIERRE REALIZADO CORRECTAMENTE");
          alert.setContentText(
                  "EL CAJERO: " + LoginController.usuarioLoggeado
                          + " AHORA ESTÁ LISTO PARA CERRAR EN: "
                          + nom_empresa);
          alert.showAndWait();

          String protesonomCierre = cod_empresa.equals("0001")
                  ? "CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL"
                  : "CASIMIRO UITZ VILLANUEVA";

          Map<String, Object> parsCierre =
                  construirParametrosReporte(
                          nombreEmpresa,
                          isLogo,
                          rfcEmpresa,
                          direcEmpresa,
                          "REPORTE DE TRASLADO DE CIERRE",
                          folio,
                          fechaTicket,
                          turno,
                          "CUENTA DE CAJERO " + LoginController.usuarioLoggeado,
                          cod_boveda,
                          montotraslado,
                          moneyAsWords,
                          desc,
                          LoginController.usuarioLoggeado,
                          nomcajero,
                          horaFormateada,
                          protesonomCierre,
                          "TRASLADO DE CIERRE realizado por " + montotraslado
                                  + " (" + moneyAsWords + ") efectuado en la sucursal de UMAN por "
                                  + nomcajero + " y autorizado por " + protesonomCierre
                  );

          generarReporteAsync(
                  "/Reports/traslado_cierre.jasper",
                  parsCierre,
                  loadingStage
          );

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


  private Map<String, Object> construirParametrosReporte(
          String nombreEmpresa,
          InputStream logo,
          String rfcEmpresa,
          String direccionEmpresa,
          String titulo,
          String folio,
          String fechaTicket,
          String turno,
          String origen,
          String destino,
          String monto,
          String montoLetras,
          String descProteso,
          String usuario,
          String nombreCajero,
          String hora,
          String protesonom,
          String descripcion) {

    Map<String, Object> pars = new HashMap<>();

    pars.put("Empresa", nombreEmpresa);
    pars.put("Logoempresa", logo);
    pars.put("Rfc", rfcEmpresa);
    pars.put("Direccion", direccionEmpresa);
    pars.put("Titulo", titulo);
    pars.put("Id", folio);
    pars.put("Fecha", fechaTicket);
    pars.put("Turno", turno);
    pars.put("Origen", origen);
    pars.put("Destino", destino);
    pars.put("Monto", monto);
    pars.put("Montoletras", montoLetras);
    pars.put("DescProteso", descProteso);
    pars.put("Cajerouser", usuario);
    pars.put("Cajeronom", nombreCajero);
    pars.put("Hora", hora);
    pars.put("Protesonom", protesonom);
    pars.put("Descripcion", descripcion);

    return pars;
  }



  private void generarReporteAsync(String rutaReporte,
                                   Map<String, Object> pars,
                                   Stage loadingStage) {

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        try {

          InputStream isRepo =
                  getClass().getResourceAsStream(rutaReporte);
          System.out.println(rutaReporte);
          JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint jpRepo =
                  JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

          Platform.runLater(() -> {
            JasperViewer viewer = new JasperViewer(jpRepo, false);
            viewer.setSize(800, 600);
            viewer.setAlwaysOnTop(true);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("REPORTE DE TRASLADO");
            viewer.setVisible(true);
          });

        } catch (Exception e) {
          e.printStackTrace();
          Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL GENERAR EL REPORTE");
            alert.setContentText("OCURRIÓ UN ERROR: " + e.getMessage());
            alert.showAndWait();
          });
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> loadingStage.close());
    task.setOnFailed(e -> loadingStage.close());

    loadingStage.show();
    new Thread(task).start();
  }





}
